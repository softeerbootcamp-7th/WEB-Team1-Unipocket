package com.genesis.unipocket.tempexpense.command.application.parsing;

import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.global.infrastructure.gemini.GeminiService;
import com.genesis.unipocket.tempexpense.command.application.parsing.command.ExchangeRateLookupCommand;
import com.genesis.unipocket.tempexpense.command.application.parsing.result.AccountBookRateContext;
import com.genesis.unipocket.tempexpense.command.application.parsing.result.NormalizedParsedExpenseItem;
import com.genesis.unipocket.tempexpense.command.application.result.BatchParsingResult;
import com.genesis.unipocket.tempexpense.command.application.result.BatchParsingResult.FileParsingOutcome;
import com.genesis.unipocket.tempexpense.command.application.result.ParseStartResult;
import com.genesis.unipocket.tempexpense.command.application.result.ParsingResult;
import com.genesis.unipocket.tempexpense.command.facade.port.AccountBookRateInfoProvider;
import com.genesis.unipocket.tempexpense.command.facade.port.ExchangeRateProvider;
import com.genesis.unipocket.tempexpense.command.facade.port.dto.AccountBookRateInfo;
import com.genesis.unipocket.tempexpense.command.persistence.entity.File;
import com.genesis.unipocket.tempexpense.command.persistence.entity.TempExpenseMeta;
import com.genesis.unipocket.tempexpense.command.persistence.repository.FileRepository;
import com.genesis.unipocket.tempexpense.command.persistence.repository.TempExpenseMetaRepository;
import com.genesis.unipocket.tempexpense.common.infrastructure.ParsingProgressPublisher;
import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * <b>임시지출내역 파싱 서비스</b>
 *
 * @author 김동균
 * @since 2026-02-08
 */
@Slf4j
@Service
@AllArgsConstructor
public class TemporaryExpenseParsingService {

	private static final int MAX_DOCUMENT_PARSE_FILES = 1;
	private static final int MAX_IMAGE_PARSE_FILES = 3;

	private final FileRepository fileRepository;
	private final TempExpenseMetaRepository tempExpenseMetaRepository;
	private final AccountBookRateInfoProvider accountBookRateInfoProvider;
	private final ExchangeRateProvider exchangeRateProvider;
	private final TemporaryExpenseFieldParser fieldParser;
	private final TemporaryExpenseParseClient temporaryExpenseParseClient;
	private final TemporaryExpensePersistenceService temporaryExpensePersistenceService;
	private final ParsingProgressPublisher progressPublisher;

	// AsyncConfig 에서 지정해준 이미지 파싱 작업을 지정된 비동기 스레드풀에서 돌려줌.
	@Qualifier("parsingExecutor") private final Executor parsingExecutor;

	/**
	 * 비동기 파싱 시작 전 입력 검증 및 taskId 생성
	 */
	public ParseStartResult startParseAsync(
			Long accountBookId, Long tempExpenseMetaId, List<String> s3Keys) {
		// s3Keys 가 null 인 경우에 대한 방어 코드
		List<String> requestedS3Keys = s3Keys == null ? List.of() : s3Keys;

		if (tempExpenseMetaId == null) {
			throw new BusinessException(ErrorCode.TEMP_EXPENSE_META_NOT_FOUND);
		}

		TempExpenseMeta meta =
				tempExpenseMetaRepository
						.findById(tempExpenseMetaId)
						.orElseThrow(
								() -> new BusinessException(ErrorCode.TEMP_EXPENSE_META_NOT_FOUND));

		// 메타가 해당 accountBook 내의 메타가 아니면 스코프가 맞지 않으므로 방어
		if (!meta.getAccountBookId().equals(accountBookId)) {
			throw new BusinessException(ErrorCode.TEMP_EXPENSE_SCOPE_MISMATCH);
		}

		// 메타 내 전체 파일 조회 -> 왜? 일부면 메타 내 파일이 맞는지 확인필요
		// 전체면 어차피 전체 가져와야함
		List<File> metaFiles = fileRepository.findByTempExpenseMetaId(tempExpenseMetaId);

		if (metaFiles.isEmpty()) {
			throw new BusinessException(ErrorCode.TEMP_EXPENSE_PARSE_FILES_REQUIRED);
		}

		// 일부분 요청인 경우를 위하여 파싱할 파일들을 거르는 작업
		List<File> files;
		if (requestedS3Keys.isEmpty()) {
			files = metaFiles;
		} else {
			Map<String, File> metaFileByKey =
					metaFiles.stream()
							.collect(Collectors.toMap(File::getS3Key, Function.identity()));
			Set<String> distinctKeys = new HashSet<>(requestedS3Keys);
			files =
					distinctKeys.stream()
							.map(metaFileByKey::get)
							.filter(Objects::nonNull)
							.toList();
			// 요청된 s3Keys를 중복 제거한 뒤(distinctKeys)
			// 실제 메타 목록에서 매칭된 파일 수(files.size())와 비교하는 로직
			// 매칭된 파일 수 == (중복제거된) distinctKeys 수 로 비교하는 로직입니다.
			// 즉, 실제로 전부 존재하는지 확인하는 로직입니다.
			if (files.size() != distinctKeys.size()) {
				throw new BusinessException(ErrorCode.TEMP_EXPENSE_FILE_NOT_FOUND);
			}
		}

		// 파일 파싱하는지 여부 -> true 면 파일
		boolean hasDocument =
				files.stream()
						.anyMatch(
								f ->
										f.getFileType() == File.FileType.CSV
												|| f.getFileType() == File.FileType.EXCEL);

		if (hasDocument) {
			if (files.size() > MAX_DOCUMENT_PARSE_FILES) {
				throw new BusinessException(ErrorCode.TEMP_EXPENSE_PARSE_FILE_LIMIT_EXCEEDED);
			}
		} else {
			if (files.size() > MAX_IMAGE_PARSE_FILES) {
				throw new BusinessException(ErrorCode.TEMP_EXPENSE_PARSE_FILE_LIMIT_EXCEEDED);
			}
		}

		List<String> targetS3Keys = files.stream().map(File::getS3Key).toList();

		// SSE 비동기 파싱 시작
		String taskId = UUID.randomUUID().toString();
		progressPublisher.registerTask(taskId, accountBookId);

		CompletableFuture.runAsync(
				() -> parseBatchFilesAsync(accountBookId, targetS3Keys, taskId), parsingExecutor);
		return new ParseStartResult(taskId, targetS3Keys.size());
	}

	private ParsingResult parseAndPersistExpenses(
			File file, TempExpenseMeta meta, AccountBookRateContext rateContext) {
		var geminiResponse = temporaryExpenseParseClient.parse(file);
		if (!geminiResponse.success()) {
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
		return temporaryExpensePersistenceService.persist(
				file, meta, normalizedItems, rateContext, exchangeRateMap);
	}

	private AccountBookRateContext resolveRateContext(Long accountBookId) {
		AccountBookRateInfo accountBook = accountBookRateInfoProvider.getRateInfo(accountBookId);
		return new AccountBookRateContext(
				accountBook.baseCurrencyCode(), accountBook.localCurrencyCode());
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
																	== baseCurrencyCode)) // baseAmount/baseCurrencyCode
							// 쌍이 기준 통화와 일치할 때만 환율 조회 생략
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

	/**
	 * 여러 파일 비동기 파싱 (SSE 진행 상황 알림)
	 */
	public CompletableFuture<BatchParsingResult> parseBatchFilesAsync(
			Long accountBookId, List<String> s3Keys, String taskId) {
		log.info("Starting async batch parsing for task: {}, files: {}", taskId, s3Keys.size());

		try {
			AccountBookRateContext rateContext = resolveRateContext(accountBookId);

			Map<String, File> filesByS3Key =
					fileRepository.findByS3KeyIn(s3Keys).stream()
							.collect(Collectors.toMap(File::getS3Key, Function.identity()));

			Map<Long, TempExpenseMeta> metaById =
					tempExpenseMetaRepository
							.findAllById(
									filesByS3Key.values().stream()
											.map(File::getTempExpenseMetaId)
											.distinct()
											.toList())
							.stream()
							.collect(
									Collectors.toMap(
											TempExpenseMeta::getTempExpenseMetaId,
											Function.identity()));

			int totalFiles = s3Keys.size();
			int completedFiles = 0;
			int totalParsed = 0;
			int totalNormal = 0;
			int totalIncomplete = 0;
			int failedFiles = 0;
			Long firstMetaId = null;
			List<FileParsingOutcome> fileResults = new LinkedList<>();

			for (String s3Key : s3Keys) {
				try {
					File file = filesByS3Key.get(s3Key);
					if (file == null) {
						log.warn("File not found: {}", s3Key);
						completedFiles++;
						failedFiles++;
						String message = ErrorCode.TEMP_EXPENSE_FILE_NOT_FOUND.getMessage();
						fileResults.add(new FileParsingOutcome(null, s3Key, "FAILED", message));
						progressPublisher.publishFileError(
								taskId,
								new ParsingProgressPublisher.FileErrorEvent(
										completedFiles,
										totalFiles,
										s3Key,
										(completedFiles * 100) / totalFiles,
										message));
						continue;
					}
					TempExpenseMeta meta = metaById.get(file.getTempExpenseMetaId());
					if (meta == null) {
						log.warn("Meta not found for file: {}", s3Key);
						completedFiles++;
						failedFiles++;
						String message = ErrorCode.TEMP_EXPENSE_META_NOT_FOUND.getMessage();
						fileResults.add(
								new FileParsingOutcome(file.getFileId(), s3Key, "FAILED", message));
						progressPublisher.publishFileError(
								taskId,
								new ParsingProgressPublisher.FileErrorEvent(
										completedFiles,
										totalFiles,
										s3Key,
										(completedFiles * 100) / totalFiles,
										message));
						continue;
					}
					if (!meta.getAccountBookId().equals(accountBookId)) {
						throw new BusinessException(ErrorCode.TEMP_EXPENSE_SCOPE_MISMATCH);
					}

					progressPublisher.publishProgress(
							taskId,
							new ParsingProgressPublisher.ParsingProgressEvent(
									completedFiles,
									totalFiles,
									file.getS3Key(),
									(completedFiles * 100) / totalFiles));

					// 제미나이 API 호출 코드
					ParsingResult result = parseAndPersistExpenses(file, meta, rateContext);

					if (firstMetaId == null) {
						firstMetaId = result.metaId();
					}
					totalParsed += result.totalCount();
					totalNormal += result.normalCount();
					totalIncomplete += result.incompleteCount();
					completedFiles++;
					fileResults.add(
							new FileParsingOutcome(file.getFileId(), s3Key, "SUCCESS", null));

				} catch (Exception e) {
					log.error("Failed to parse file: {}", s3Key, e);
					completedFiles++;
					failedFiles++;
					String errorMessage = e.getMessage();
					File failedFile = filesByS3Key.get(s3Key);
					fileResults.add(
							new FileParsingOutcome(
									failedFile != null ? failedFile.getFileId() : null,
									s3Key,
									"FAILED",
									errorMessage));
					progressPublisher.publishFileError(
							taskId,
							new ParsingProgressPublisher.FileErrorEvent(
									completedFiles,
									totalFiles,
									s3Key,
									(completedFiles * 100) / totalFiles,
									errorMessage));
				}
			}

			BatchParsingResult finalResult =
					new BatchParsingResult(
							firstMetaId,
							totalParsed,
							totalNormal,
							totalIncomplete,
							failedFiles,
							List.copyOf(fileResults));

			// 배치 파싱 결과를 SSE 로 전달
			progressPublisher.complete(taskId, finalResult);
			return CompletableFuture.completedFuture(finalResult);

		} catch (Exception e) {
			log.error("Batch parsing failed before completion. taskId={}", taskId, e);
			String errorMessage =
					(e instanceof BusinessException businessException)
							? businessException.getMessage()
							: ErrorCode.TEMP_EXPENSE_PARSE_FAILED.getMessage();
			progressPublisher.publishError(taskId, errorMessage);
			return CompletableFuture.failedFuture(e);
		}
	}
}
