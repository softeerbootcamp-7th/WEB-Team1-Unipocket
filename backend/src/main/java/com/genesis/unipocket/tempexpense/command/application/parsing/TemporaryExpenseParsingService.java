package com.genesis.unipocket.tempexpense.command.application.parsing;

import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.global.infrastructure.gemini.GeminiService;
import com.genesis.unipocket.tempexpense.command.application.parsing.result.AccountBookRateContext;
import com.genesis.unipocket.tempexpense.command.application.parsing.result.NormalizedParsedExpenseItem;
import com.genesis.unipocket.tempexpense.command.application.result.ParseStartResult;
import com.genesis.unipocket.tempexpense.command.facade.port.AccountBookRateInfoProvider;
import com.genesis.unipocket.tempexpense.command.facade.port.dto.AccountBookRateInfo;
import com.genesis.unipocket.tempexpense.command.facade.provide.TemporaryExpenseScopeValidationProvider;
import com.genesis.unipocket.tempexpense.command.persistence.entity.File;
import com.genesis.unipocket.tempexpense.command.persistence.entity.TempExpenseMeta;
import com.genesis.unipocket.tempexpense.command.persistence.repository.FileRepository;
import com.genesis.unipocket.tempexpense.common.infrastructure.sse.ParsingProgressPublisher;
import com.genesis.unipocket.tempexpense.common.util.TemporaryExpenseTaskSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class TemporaryExpenseParsingService {

	private static final int MAX_DOCUMENT_PARSE_FILES = 1;
	private static final int MAX_IMAGE_PARSE_FILES = 3;
	private static final String FILE_PROGRESS_CODE_SUCCESS = "SUCCESS";
	private static final String FILE_PROGRESS_CODE_FAILED_TOO_MANY_REQUEST =
			"FAILED_TOO_MANY_REQUEST";
	private static final String FILE_PROGRESS_CODE_INTERNAL_SERVER_ERROR = "INTERNAL_SERVER_ERROR";

	private final FileRepository fileRepository;
	private final AccountBookRateInfoProvider accountBookRateInfoProvider;
	private final TemporaryExpenseFieldParser fieldParser;
	private final TemporaryExpenseParseClient temporaryExpenseParseClient;
	private final TemporaryExpensePersistenceService temporaryExpensePersistenceService;
	private final ParsingProgressPublisher progressPublisher;
	private final TemporaryExpenseScopeValidationProvider temporaryExpenseScopeValidator;

	@Qualifier("parsingExecutor") private final Executor parsingExecutor;

	@Qualifier("fileParsingExecutor") private final Executor fileParsingExecutor;

	public ParseStartResult startParseAsync(
			Long accountBookId, Long tempExpenseMetaId, List<String> s3Keys) {
		List<String> requestedS3Keys = s3Keys == null ? List.of() : s3Keys;

		if (tempExpenseMetaId == null) {
			throw new BusinessException(ErrorCode.TEMP_EXPENSE_META_NOT_FOUND);
		}

		TempExpenseMeta meta =
				temporaryExpenseScopeValidator.validateMetaScope(accountBookId, tempExpenseMetaId);

		List<File> metaFiles = fileRepository.findByTempExpenseMetaId(tempExpenseMetaId);

		if (metaFiles.isEmpty()) {
			throw new BusinessException(ErrorCode.TEMP_EXPENSE_PARSE_FILES_REQUIRED);
		}

		List<File> files;
		if (requestedS3Keys.isEmpty()) {
			files = metaFiles;
		} else {
			Map<String, File> metaFileByKey =
					metaFiles.stream()
							.collect(Collectors.toMap(File::getS3Key, Function.identity()));
			Set<String> distinctKeys = new HashSet<>(requestedS3Keys);
			files = distinctKeys.stream().map(metaFileByKey::get).filter(Objects::nonNull).toList();
			if (files.size() != distinctKeys.size()) {
				throw new BusinessException(ErrorCode.TEMP_EXPENSE_FILE_NOT_FOUND);
			}
		}

		validateParseFileLimit(files);

		String taskId = UUID.randomUUID().toString();
		progressPublisher.registerTask(taskId, accountBookId);

		try {
			parsingExecutor.execute(() -> parseBatchFiles(meta, files, taskId));
		} catch (RejectedExecutionException e) {
			throw new BusinessException(ErrorCode.TEMP_EXPENSE_PARSE_RATE_LIMIT);
		}
		return new ParseStartResult(taskId, files.size());
	}

	private void validateParseFileLimit(List<File> files) {
		boolean hasDocument =
				files.stream()
						.anyMatch(
								f ->
										f.getFileType() == File.FileType.CSV
												|| f.getFileType() == File.FileType.EXCEL);
		if (hasDocument && files.size() > MAX_DOCUMENT_PARSE_FILES) {
			throw new BusinessException(ErrorCode.TEMP_EXPENSE_PARSE_FILE_LIMIT_EXCEEDED);
		}
		if (!hasDocument && files.size() > MAX_IMAGE_PARSE_FILES) {
			throw new BusinessException(ErrorCode.TEMP_EXPENSE_PARSE_FILE_LIMIT_EXCEEDED);
		}
	}

	void parseBatchFiles(TempExpenseMeta meta, List<File> files, String taskId) {
		log.info("Starting async batch parsing for task: {}, files: {}", taskId, files.size());

		AccountBookRateContext rateContext;
		try {
			rateContext = resolveRateContext(meta.getAccountBookId());
		} catch (Exception e) {
			ErrorCode errorCode =
					TemporaryExpenseTaskSupport.resolveErrorCode(
							e, ErrorCode.TEMP_EXPENSE_PARSE_FAILED);
			progressPublisher.publishError(taskId, errorCode);
			throw TemporaryExpenseTaskSupport.rethrow(e);
		}

		int total = files.size();
		AtomicInteger completed = new AtomicInteger(0);
		List<String> succeededFileKeys = Collections.synchronizedList(new ArrayList<>());
		List<FailedFile> failedFiles = Collections.synchronizedList(new ArrayList<>());
		progressPublisher.publishProgress(taskId, 0);

		final AccountBookRateContext finalRateContext = rateContext;
		List<CompletableFuture<Void>> futures =
				files.stream()
						.map(
								file ->
										CompletableFuture.runAsync(
												() -> {
													String progressMessage =
															"PROCESSING: " + file.getS3Key();
													String progressCode = null;
													try {
														parseAndPersistExpenses(
																file, meta, finalRateContext);
														succeededFileKeys.add(file.getS3Key());
														progressMessage =
																buildPerFileProgressMessage(
																		file.getS3Key(),
																		true,
																		null);
														progressCode =
																buildPerFileProgressCode(
																		true, null);
													} catch (Exception e) {
														ErrorCode errorCode =
																TemporaryExpenseTaskSupport
																		.resolveErrorCode(
																				e,
																				ErrorCode
																						.TEMP_EXPENSE_PARSE_FAILED);
														failedFiles.add(
																new FailedFile(
																		file.getS3Key(),
																		errorCode));
														progressMessage =
																buildPerFileProgressMessage(
																		file.getS3Key(),
																		false,
																		errorCode);
														progressCode =
																buildPerFileProgressCode(
																		false, errorCode);
														log.error(
																"Failed to parse file: {}",
																file.getS3Key(),
																e);
													} finally {
														int done = completed.incrementAndGet();
														progressPublisher.publishProgress(
																taskId,
																TemporaryExpenseTaskSupport
																		.toPercent(done, total),
																progressMessage,
																progressCode,
																file.getS3Key());
													}
												},
												fileParsingExecutor))
						.toList();

		CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();

		if (failedFiles.isEmpty()) {
			progressPublisher.complete(taskId);
			return;
		}

		ErrorCode terminalCode =
				failedFiles.stream()
								.allMatch(
										failedFile ->
												failedFile.errorCode()
														== ErrorCode.TEMP_EXPENSE_PARSE_RATE_LIMIT)
						? ErrorCode.TEMP_EXPENSE_PARSE_RATE_LIMIT
						: ErrorCode.TEMP_EXPENSE_PARSE_FAILED;
		String summaryMessage = buildTerminalSummaryMessage(succeededFileKeys, failedFiles);
		progressPublisher.publishError(taskId, terminalCode, summaryMessage);
	}

	private AccountBookRateContext resolveRateContext(Long accountBookId) {
		AccountBookRateInfo accountBook = accountBookRateInfoProvider.getRateInfo(accountBookId);
		return new AccountBookRateContext(
				accountBook.baseCurrencyCode(), accountBook.localCurrencyCode());
	}

	private void parseAndPersistExpenses(
			File file, TempExpenseMeta meta, AccountBookRateContext rateContext) {
		var geminiResponse = temporaryExpenseParseClient.parse(file);
		if (!geminiResponse.success()) {
			if (geminiResponse.isRateLimited()) {
				throw new BusinessException(ErrorCode.TEMP_EXPENSE_PARSE_RATE_LIMIT);
			}
			throw new BusinessException(ErrorCode.TEMP_EXPENSE_PARSE_FAILED);
		}

		List<NormalizedParsedExpenseItem> normalizedItems =
				geminiResponse.items().stream()
						.map(item -> normalizeParsedItem(item, rateContext))
						.toList();

		temporaryExpensePersistenceService.persist(file, meta, normalizedItems, rateContext);
	}

	private NormalizedParsedExpenseItem normalizeParsedItem(
			GeminiService.ParsedExpenseItem item, AccountBookRateContext rateContext) {
		CurrencyCode parsedLocalCurrency =
				fieldParser.parseCurrencyCode(
						item.localCurrency(), rateContext.defaultLocalCurrencyCode());
		CurrencyCode parsedBaseCurrency = fieldParser.parseCurrencyCode(item.baseCurrency(), null);

		boolean isForeignTravelContext =
				rateContext.baseCurrencyCode() != rateContext.defaultLocalCurrencyCode();
		boolean shouldSwap =
				isForeignTravelContext
						&& parsedLocalCurrency != null
						&& parsedBaseCurrency != null
						&& parsedLocalCurrency == rateContext.baseCurrencyCode()
						&& parsedBaseCurrency == rateContext.defaultLocalCurrencyCode();

		if (shouldSwap) {
			return new NormalizedParsedExpenseItem(
					item.merchantName(),
					fieldParser.parseCategory(item.category()),
					parsedBaseCurrency,
					item.baseAmount(),
					parsedLocalCurrency,
					item.localAmount(),
					item.memo(),
					item.occurredAt(),
					item.cardLastFourDigits(),
					item.approvalNumber());
		}

		return new NormalizedParsedExpenseItem(
				item.merchantName(),
				fieldParser.parseCategory(item.category()),
				parsedLocalCurrency,
				item.localAmount(),
				parsedBaseCurrency,
				item.baseAmount(),
				item.memo(),
				item.occurredAt(),
				item.cardLastFourDigits(),
				item.approvalNumber());
	}

	private String buildPerFileProgressMessage(
			String s3Key, boolean success, ErrorCode errorCodeOrNull) {
		if (success) {
			return "SUCCESS: " + s3Key;
		}
		return "FAILED: " + s3Key + " (" + errorCodeOrNull.getCode() + ")";
	}

	private String buildPerFileProgressCode(boolean success, ErrorCode errorCodeOrNull) {
		if (success) {
			return FILE_PROGRESS_CODE_SUCCESS;
		}
		if (errorCodeOrNull == ErrorCode.TEMP_EXPENSE_PARSE_RATE_LIMIT) {
			return FILE_PROGRESS_CODE_FAILED_TOO_MANY_REQUEST;
		}
		return FILE_PROGRESS_CODE_INTERNAL_SERVER_ERROR;
	}

	private String buildTerminalSummaryMessage(
			List<String> succeededFileKeys, List<FailedFile> failedFiles) {
		String succeeded = succeededFileKeys.isEmpty() ? "-" : String.join(", ", succeededFileKeys);
		String failed =
				failedFiles.stream()
						.map(file -> file.s3Key() + " [" + file.errorCode().getCode() + "]")
						.collect(Collectors.joining(", "));
		return "TEMP_EXPENSE_PARSE_RESULT success="
				+ succeededFileKeys.size()
				+ " ("
				+ succeeded
				+ "), failed="
				+ failedFiles.size()
				+ " ("
				+ failed
				+ ")";
	}

	private record FailedFile(String s3Key, ErrorCode errorCode) {}
}
