package com.genesis.unipocket.tempexpense.command.application.parsing;

import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.global.infrastructure.gemini.GeminiService;
import com.genesis.unipocket.tempexpense.command.application.parsing.command.ExchangeRateLookupCommand;
import com.genesis.unipocket.tempexpense.command.application.parsing.result.AccountBookRateContext;
import com.genesis.unipocket.tempexpense.command.application.parsing.result.NormalizedParsedExpenseItem;
import com.genesis.unipocket.tempexpense.command.application.result.ParseStartResult;
import com.genesis.unipocket.tempexpense.command.facade.port.AccountBookRateInfoProvider;
import com.genesis.unipocket.tempexpense.command.facade.port.ExchangeRateProvider;
import com.genesis.unipocket.tempexpense.command.facade.port.dto.AccountBookRateInfo;
import com.genesis.unipocket.tempexpense.command.facade.provide.TemporaryExpenseScopeValidationProvider;
import com.genesis.unipocket.tempexpense.command.persistence.entity.File;
import com.genesis.unipocket.tempexpense.command.persistence.entity.TempExpenseMeta;
import com.genesis.unipocket.tempexpense.command.persistence.repository.FileRepository;
import com.genesis.unipocket.tempexpense.common.infrastructure.sse.ParsingProgressPublisher;
import com.genesis.unipocket.tempexpense.common.util.TemporaryExpenseBatchTaskRunner;
import com.genesis.unipocket.tempexpense.common.util.TemporaryExpenseTaskSupport;
import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executor;
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

	private final FileRepository fileRepository;
	private final AccountBookRateInfoProvider accountBookRateInfoProvider;
	private final ExchangeRateProvider exchangeRateProvider;
	private final TemporaryExpenseFieldParser fieldParser;
	private final TemporaryExpenseParseClient temporaryExpenseParseClient;
	private final TemporaryExpensePersistenceService temporaryExpensePersistenceService;
	private final ParsingProgressPublisher progressPublisher;
	private final TemporaryExpenseScopeValidationProvider temporaryExpenseScopeValidator;

	@Qualifier("parsingExecutor") private final Executor parsingExecutor;

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

		parsingExecutor.execute(() -> parseBatchFiles(meta, files, taskId));
		return new ParseStartResult(taskId, files.size());
	}

	private void parseAndPersistExpenses(
			File file, TempExpenseMeta meta, AccountBookRateContext rateContext) {
		var geminiResponse = temporaryExpenseParseClient.parse(file);
		if (!geminiResponse.success()) {
			if (geminiResponse.isRateLimited()) {
				log.error("Gemini rate limited. fileId={}", file.getFileId());
				throw new GeminiRateLimitException();
			}
			log.error(
					"Gemini parsing failed. fileId={}, reason={}",
					file.getFileId(),
					geminiResponse.errorMessage());
			throw new BusinessException(ErrorCode.TEMP_EXPENSE_PARSE_FAILED);
		}

		List<NormalizedParsedExpenseItem> normalizedItems =
				geminiResponse.items().stream()
						.map(
								item ->
										normalizeParsedItem(
												item, rateContext.defaultLocalCurrencyCode()))
						.toList();

		Map<ExchangeRateLookupCommand, BigDecimal> exchangeRateMap =
				buildExchangeRateMap(normalizedItems, rateContext.baseCurrencyCode());
		temporaryExpensePersistenceService.persist(
				file, meta, normalizedItems, rateContext, exchangeRateMap);
	}

	private AccountBookRateContext resolveRateContext(Long accountBookId) {
		AccountBookRateInfo accountBook = accountBookRateInfoProvider.getRateInfo(accountBookId);
		return new AccountBookRateContext(
				accountBook.baseCurrencyCode(), accountBook.localCurrencyCode());
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

	private NormalizedParsedExpenseItem normalizeParsedItem(
			GeminiService.ParsedExpenseItem item, CurrencyCode defaultLocalCurrencyCode) {
		return new NormalizedParsedExpenseItem(
				item.merchantName(),
				fieldParser.parseCategory(item.category()),
				fieldParser.parseCurrencyCode(item.localCurrency(), defaultLocalCurrencyCode),
				item.localAmount(),
				fieldParser.parseCurrencyCode(item.baseCurrency(), null),
				item.baseAmount(),
				item.memo(),
				item.occurredAt(),
				item.cardLastFourDigits(),
				item.approvalNumber());
	}

	private Map<ExchangeRateLookupCommand, BigDecimal> buildExchangeRateMap(
			List<NormalizedParsedExpenseItem> items, CurrencyCode baseCurrencyCode) {
		Set<ExchangeRateLookupCommand> lookupKeys =
				items.stream()
						.filter(
								item ->
										item.localAmount() != null
												&& item.occurredAt() != null
												&& !(item.baseAmount() != null
														&& item.baseCurrencyCode() != null
														&& item.baseCurrencyCode()
																== baseCurrencyCode))
						.map(
								item ->
										new ExchangeRateLookupCommand(
												item.localCurrencyCode(),
												baseCurrencyCode,
												item.occurredAt().toLocalDate()))
						.collect(Collectors.toSet());

		Map<ExchangeRateLookupCommand, BigDecimal> rateMap = new HashMap<>();
		for (ExchangeRateLookupCommand key : lookupKeys) {
			if (key.fromCurrencyCode() == key.toCurrencyCode()) {
				rateMap.put(key, BigDecimal.ONE);
				continue;
			}
			BigDecimal rate =
					exchangeRateProvider.getExchangeRate(
							key.fromCurrencyCode(),
							key.toCurrencyCode(),
							key.date().atStartOfDay().atOffset(ZoneOffset.UTC));
			rateMap.put(key, rate);
		}
		return rateMap;
	}

	void parseBatchFiles(TempExpenseMeta meta, List<File> files, String taskId) {
		log.info("Starting async batch parsing for task: {}, files: {}", taskId, files.size());

		AccountBookRateContext rateContext;
		try {
			rateContext = resolveRateContext(meta.getAccountBookId());
		} catch (Exception e) {
			String errorMessage =
					TemporaryExpenseTaskSupport.resolveClientErrorMessage(
							e, ErrorCode.TEMP_EXPENSE_PARSE_FAILED);
			progressPublisher.publishError(taskId, errorMessage);
			throw TemporaryExpenseTaskSupport.rethrow(e);
		}

		TemporaryExpenseBatchTaskRunner.run(
				taskId,
				files,
				progressPublisher,
				ErrorCode.TEMP_EXPENSE_PARSE_FAILED,
				file -> {
					if (!Objects.equals(file.getTempExpenseMetaId(), meta.getTempExpenseMetaId())) {
						log.warn(
								"Skipping out-of-meta file. fileId={}, expectedMetaId={},"
										+ " actualMetaId={}",
								file.getFileId(),
								meta.getTempExpenseMetaId(),
								file.getTempExpenseMetaId());
						return;
					}
					parseAndPersistExpenses(file, meta, rateContext);
				},
				(file, e) -> {
					if (e instanceof GeminiRateLimitException) {
						log.error("Gemini 429 encountered. aborting parse task. taskId={}", taskId);
						return true;
					}
					log.error("Failed to parse file: {}", file.getS3Key(), e);
					return false;
				});
	}

	private static final class GeminiRateLimitException extends RuntimeException {}
}
