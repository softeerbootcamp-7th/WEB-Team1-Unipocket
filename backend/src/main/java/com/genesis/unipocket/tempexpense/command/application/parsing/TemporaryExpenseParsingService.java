package com.genesis.unipocket.tempexpense.command.application.parsing;

import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.global.infrastructure.gemini.GeminiService;
import com.genesis.unipocket.tempexpense.command.application.parsing.command.ExchangeRateLookupCommand;
import com.genesis.unipocket.tempexpense.command.application.parsing.result.AccountBookRateContext;
import com.genesis.unipocket.tempexpense.command.application.parsing.result.NormalizedParsedExpenseItem;
import com.genesis.unipocket.tempexpense.command.application.result.BatchParsingResult;
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

	private final FileRepository fileRepository;
	private final TempExpenseMetaRepository tempExpenseMetaRepository;
	private final AccountBookRateInfoProvider accountBookRateInfoProvider;
	private final ExchangeRateProvider exchangeRateProvider;
	private final TemporaryExpenseFieldParser fieldParser;
	private final TemporaryExpenseParseClient temporaryExpenseParseClient;
	private final TemporaryExpensePersistenceService temporaryExpensePersistenceService;
	private final ParsingProgressPublisher progressPublisher;

	/**
	 * 비동기 파싱 시작 전 입력 검증 및 taskId 생성
	 */
	public String startParseAsync(Long accountBookId, List<String> s3Keys) {
		if (s3Keys == null || s3Keys.isEmpty()) {
			throw new BusinessException(ErrorCode.TEMP_EXPENSE_PARSE_FILES_REQUIRED);
		}

		List<File> files =
				s3Keys.stream()
						.map(
								key ->
										fileRepository
												.findByS3Key(key)
												.orElseThrow(
														() ->
																new BusinessException(
																		ErrorCode
																				.TEMP_EXPENSE_FILE_NOT_FOUND)))
						.toList();

		boolean hasDocument =
				files.stream()
						.anyMatch(
								f ->
										f.getFileType() == File.FileType.CSV
												|| f.getFileType() == File.FileType.EXCEL);

		if (hasDocument) {
			if (s3Keys.size() > 1) {
				throw new BusinessException(ErrorCode.TEMP_EXPENSE_PARSE_FILE_LIMIT_EXCEEDED);
			}
		} else {
			if (s3Keys.size() > 20) {
				throw new BusinessException(ErrorCode.TEMP_EXPENSE_PARSE_FILE_LIMIT_EXCEEDED);
			}
		}

		String taskId = UUID.randomUUID().toString();
		progressPublisher.registerTask(taskId, accountBookId);
		parseBatchFilesAsync(accountBookId, s3Keys, taskId);
		return taskId;
	}

	/**
	 * 파일 파싱 및 TemporaryExpense 생성
	 */
	@Transactional
	public ParsingResult parseFile(Long accountBookId, String s3Key) {
		File file =
				fileRepository
						.findByS3Key(s3Key)
						.orElseThrow(
								() -> new BusinessException(ErrorCode.TEMP_EXPENSE_FILE_NOT_FOUND));

		Long tempExpenseMetaId = file.getTempExpenseMetaId();
		TempExpenseMeta meta =
				tempExpenseMetaRepository
						.findById(tempExpenseMetaId)
						.orElseThrow(
								() -> new BusinessException(ErrorCode.TEMP_EXPENSE_META_NOT_FOUND));
		if (!meta.getAccountBookId().equals(accountBookId)) {
			throw new BusinessException(ErrorCode.TEMP_EXPENSE_SCOPE_MISMATCH);
		}

		AccountBookRateContext rateContext = resolveRateContext(accountBookId);
		return parseAndPersistExpenses(file, meta, rateContext);
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
				meta, normalizedItems, rateContext, exchangeRateMap);
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
												&& item.baseAmount()
														== null) // baseAmount가 있으면 환율 조회 불필요
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
	@Async("parsingExecutor")
	public CompletableFuture<BatchParsingResult> parseBatchFilesAsync(
			Long accountBookId, List<String> s3Keys, String taskId) {
		log.info("Starting async batch parsing for task: {}, files: {}", taskId, s3Keys.size());

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
		Long firstMetaId = null;

		for (String s3Key : s3Keys) {
			try {
				File file = filesByS3Key.get(s3Key);
				if (file == null) {
					log.warn("File not found: {}", s3Key);
					completedFiles++;
					continue;
				}
				TempExpenseMeta meta = metaById.get(file.getTempExpenseMetaId());
				if (meta == null) {
					log.warn("Meta not found for file: {}", s3Key);
					completedFiles++;
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

				ParsingResult result = parseAndPersistExpenses(file, meta, rateContext);
				if (firstMetaId == null) {
					firstMetaId = result.metaId();
				}
				totalParsed += result.totalCount();
				totalNormal += result.normalCount();
				totalIncomplete += result.incompleteCount();

				completedFiles++;

			} catch (Exception e) {
				log.error("Failed to parse file: {}", s3Key, e);
				completedFiles++;
			}
		}

		BatchParsingResult finalResult =
				new BatchParsingResult(firstMetaId, totalParsed, totalNormal, totalIncomplete, 0);

		// 완료 이벤트 publish
		progressPublisher.complete(taskId, finalResult);

		return java.util.concurrent.CompletableFuture.completedFuture(finalResult);
	}
}
