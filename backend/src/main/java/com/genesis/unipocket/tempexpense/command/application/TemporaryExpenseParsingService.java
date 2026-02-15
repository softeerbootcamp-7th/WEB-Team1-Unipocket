package com.genesis.unipocket.tempexpense.command.application;

import com.genesis.unipocket.accountbook.command.persistence.entity.AccountBookEntity;
import com.genesis.unipocket.accountbook.command.persistence.repository.AccountBookCommandRepository;
import com.genesis.unipocket.exchange.query.application.ExchangeRateService;
import com.genesis.unipocket.global.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.global.infrastructure.gemini.GeminiService;
import com.genesis.unipocket.global.infrastructure.storage.s3.S3Service;
import com.genesis.unipocket.tempexpense.command.application.result.BatchParsingResult;
import com.genesis.unipocket.tempexpense.command.application.result.ParsingResult;
import com.genesis.unipocket.tempexpense.command.persistence.entity.File;
import com.genesis.unipocket.tempexpense.command.persistence.entity.TempExpenseMeta;
import com.genesis.unipocket.tempexpense.command.persistence.entity.TemporaryExpense;
import com.genesis.unipocket.tempexpense.command.persistence.entity.TemporaryExpense.TemporaryExpenseStatus;
import com.genesis.unipocket.tempexpense.command.persistence.repository.FileRepository;
import com.genesis.unipocket.tempexpense.command.persistence.repository.TempExpenseMetaRepository;
import com.genesis.unipocket.tempexpense.command.persistence.repository.TemporaryExpenseRepository;
import com.genesis.unipocket.tempexpense.common.infrastructure.ParsingProgressPublisher;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
	// ... (lines omitted)

	private final FileRepository fileRepository;
	private final TempExpenseMetaRepository tempExpenseMetaRepository;
	private final TemporaryExpenseRepository temporaryExpenseRepository;
	private final AccountBookCommandRepository accountBookRepository;
	private final ExchangeRateService exchangeRateService;
	private final GeminiService geminiService;
	private final S3Service s3Service;
	private final ParsingProgressPublisher progressPublisher;

	/**
	 * 파일 파싱 및 TemporaryExpense 생성
	 */
	@Transactional
	public ParsingResult parseFile(Long accountBookId, String s3Key) {
		File file =
				fileRepository
						.findByS3Key(s3Key)
						.orElseThrow(
								() ->
										new IllegalArgumentException(
												"파일을 찾을 수 없습니다. s3Key: " + s3Key));

		Long tempExpenseMetaId = file.getTempExpenseMetaId();
		TempExpenseMeta meta =
				tempExpenseMetaRepository
						.findById(tempExpenseMetaId)
						.orElseThrow(() -> new IllegalArgumentException("메타데이터를 찾을 수 없습니다."));
		if (!meta.getAccountBookId().equals(accountBookId)) {
			throw new IllegalArgumentException("가계부와 파일 메타가 일치하지 않습니다.");
		}

		AccountBookRateContext rateContext = resolveRateContext(accountBookId);
		return parseAndPersistExpenses(file, meta, rateContext);
	}

	private ParsingResult parseAndPersistExpenses(
			File file, TempExpenseMeta meta, AccountBookRateContext rateContext) {
		GeminiService.GeminiParseResponse geminiResponse = parseWithGemini(file);
		if (!geminiResponse.success()) {
			throw new RuntimeException("파싱 실패: " + geminiResponse.errorMessage());
		}

		List<NormalizedParsedExpenseItem> normalizedItems =
				geminiResponse.items().stream()
						.map(
								item ->
										normalizeParsedItem(
												item, rateContext.defaultLocalCurrencyCode()))
						.toList();

		Map<ExchangeRateKey, BigDecimal> exchangeRateMap =
				buildExchangeRateMap(normalizedItems, rateContext.baseCurrencyCode());

		List<TemporaryExpense> createdExpenses = new ArrayList<>();
		int normalCount = 0;
		int incompleteCount = 0;

		for (NormalizedParsedExpenseItem item : normalizedItems) {
			TemporaryExpenseStatus status = determineStatus(item);
			if (status == TemporaryExpenseStatus.NORMAL) {
				normalCount++;
			} else if (status == TemporaryExpenseStatus.INCOMPLETE) {
				incompleteCount++;
			}

			TemporaryExpense expense =
					TemporaryExpense.builder()
							.tempExpenseMetaId(meta.getTempExpenseMetaId())
							.merchantName(item.merchantName())
							.category(item.category())
							.localCountryCode(item.localCurrencyCode())
							.localCurrencyAmount(item.localAmount())
							.baseCountryCode(rateContext.baseCurrencyCode())
							.baseCurrencyAmount(
									calculateBaseAmount(
											item, rateContext.baseCurrencyCode(), exchangeRateMap))
							.paymentsMethod("카드")
							.memo(item.memo())
							.occurredAt(item.occurredAt())
							.status(status)
							.cardLastFourDigits(item.cardLastFourDigits())
							.approvalNumber(item.approvalNumber())
							.build();
			createdExpenses.add(expense);
		}

		List<TemporaryExpense> savedExpenses = temporaryExpenseRepository.saveAll(createdExpenses);
		return new ParsingResult(
				meta.getTempExpenseMetaId(),
				savedExpenses.size(),
				normalCount,
				incompleteCount,
				0,
				savedExpenses);
	}

	private GeminiService.GeminiParseResponse parseWithGemini(File file) {
		if (file.getFileType() == File.FileType.IMAGE) {
			String s3Url =
					s3Service.getPresignedGetUrl(file.getS3Key(), java.time.Duration.ofMinutes(10));
			return geminiService.parseReceiptImage(s3Url);
		}
		String content = extractContent(file);
		return geminiService.parseDocument(content);
	}

	private AccountBookRateContext resolveRateContext(Long accountBookId) {
		AccountBookEntity accountBook =
				accountBookRepository
						.findById(accountBookId)
						.orElseThrow(() -> new IllegalArgumentException("가계부를 찾을 수 없습니다."));
		return new AccountBookRateContext(
				accountBook.getBaseCountryCode().getCurrencyCode(),
				accountBook.getLocalCountryCode().getCurrencyCode());
	}

	private NormalizedParsedExpenseItem normalizeParsedItem(
			GeminiService.ParsedExpenseItem item, CurrencyCode defaultLocalCurrencyCode) {
		return new NormalizedParsedExpenseItem(
				item.merchantName(),
				parseCategory(item.category()),
				parseCurrencyCode(item.localCurrency(), defaultLocalCurrencyCode),
				item.localAmount(),
				parseCurrencyCode(item.baseCurrency(), null),
				item.baseAmount(),
				item.memo(),
				item.occurredAt(),
				item.cardLastFourDigits(),
				item.approvalNumber());
	}

	private Map<ExchangeRateKey, BigDecimal> buildExchangeRateMap(
			List<NormalizedParsedExpenseItem> items, CurrencyCode baseCurrencyCode) {
		Set<ExchangeRateKey> lookupKeys =
				items.stream()
						.filter(
								item ->
										item.localAmount() != null
												&& item.occurredAt() != null
												&& item.baseAmount()
														== null) // baseAmount가 있으면 환율 조회 불필요
						.map(
								item ->
										new ExchangeRateKey(
												item.localCurrencyCode(),
												baseCurrencyCode,
												item.occurredAt().toLocalDate()))
						.collect(Collectors.toSet());

		Map<ExchangeRateKey, BigDecimal> rateMap = new HashMap<>();
		for (ExchangeRateKey key : lookupKeys) {
			if (key.fromCurrencyCode() == key.toCurrencyCode()) {
				rateMap.put(key, BigDecimal.ONE);
				continue;
			}
			BigDecimal rate =
					exchangeRateService.getExchangeRate(
							key.fromCurrencyCode(),
							key.toCurrencyCode(),
							key.date().atStartOfDay());
			rateMap.put(key, rate);
		}
		return rateMap;
	}

	private BigDecimal calculateBaseAmount(
			NormalizedParsedExpenseItem item,
			CurrencyCode baseCurrencyCode,
			Map<ExchangeRateKey, BigDecimal> exchangeRateMap) {
		if (item.baseAmount() != null
				&& item.baseCurrencyCode() != null
				&& item.baseCurrencyCode() == baseCurrencyCode) {
			return item.baseAmount();
		}

		if (item.localAmount() == null || item.occurredAt() == null) {
			return null;
		}
		ExchangeRateKey key =
				new ExchangeRateKey(
						item.localCurrencyCode(),
						baseCurrencyCode,
						item.occurredAt().toLocalDate());
		BigDecimal rate = exchangeRateMap.get(key);
		if (rate == null) {
			return null;
		}
		return item.localAmount().multiply(rate).setScale(2, RoundingMode.HALF_UP);
	}

	private String extractContent(File file) {
		byte[] fileBytes = s3Service.downloadFile(file.getS3Key());

		if (file.getFileType() == File.FileType.CSV) {
			return new String(fileBytes, java.nio.charset.StandardCharsets.UTF_8);
		} else if (file.getFileType() == File.FileType.EXCEL) {
			return extractExcelContent(fileBytes);
		} else {
			throw new IllegalArgumentException("지원하지 않는 파일 타입입니다: " + file.getFileType());
		}
	}

	private String extractExcelContent(byte[] fileBytes) {
		try (java.io.InputStream is = new java.io.ByteArrayInputStream(fileBytes);
				org.apache.poi.ss.usermodel.Workbook workbook =
						org.apache.poi.ss.usermodel.WorkbookFactory.create(is)) {

			StringBuilder sb = new StringBuilder();
			org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheetAt(0); // 첫 번째 시트만 처리
			org.apache.poi.ss.usermodel.FormulaEvaluator evaluator =
					workbook.getCreationHelper().createFormulaEvaluator();

			for (org.apache.poi.ss.usermodel.Row row : sheet) {
				List<String> cells = new ArrayList<>();
				int lastColumn = row.getLastCellNum();
				for (int cn = 0; cn < lastColumn; cn++) {
					org.apache.poi.ss.usermodel.Cell cell =
							row.getCell(
									cn,
									org.apache.poi.ss.usermodel.Row.MissingCellPolicy
											.RETURN_BLANK_AS_NULL);
					cells.add(getCellValue(cell, evaluator));
				}
				sb.append(String.join(",", cells)).append("\n");
			}
			return sb.toString();

		} catch (java.io.IOException e) {
			throw new RuntimeException("Excel 파일 읽기 실패", e);
		}
	}

	private String getCellValue(
			org.apache.poi.ss.usermodel.Cell cell,
			org.apache.poi.ss.usermodel.FormulaEvaluator evaluator) {
		if (cell == null) return "";
		switch (cell.getCellType()) {
			case STRING:
				return cell.getStringCellValue();
			case NUMERIC:
				if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
					return cell.getLocalDateTimeCellValue().toString();
				}
				return String.valueOf(cell.getNumericCellValue());
			case BOOLEAN:
				return String.valueOf(cell.getBooleanCellValue());
			case FORMULA:
				return getFormulaCellValue(cell, evaluator);
			default:
				return "";
		}
	}

	private String getFormulaCellValue(
			org.apache.poi.ss.usermodel.Cell cell,
			org.apache.poi.ss.usermodel.FormulaEvaluator evaluator) {
		try {
			org.apache.poi.ss.usermodel.CellValue evaluated = evaluator.evaluate(cell);
			if (evaluated == null) {
				return "";
			}

			switch (evaluated.getCellType()) {
				case STRING:
					return evaluated.getStringValue();
				case NUMERIC:
					if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
						return org.apache.poi.ss.usermodel.DateUtil.getLocalDateTime(
										evaluated.getNumberValue())
								.toString();
					}
					return String.valueOf(evaluated.getNumberValue());
				case BOOLEAN:
					return String.valueOf(evaluated.getBooleanValue());
				case BLANK:
					return "";
				case ERROR:
					log.warn(
							"Formula evaluated to error for cell {}: {}",
							cell.getAddress(),
							evaluated.getErrorValue());
					return "";
				default:
					return "";
			}
		} catch (Exception e) {
			log.warn("Failed to evaluate formula for cell {}", cell.getAddress(), e);
			return "";
		}
	}

	/**
	 * 파싱 항목의 상태 결정
	 */
	private TemporaryExpenseStatus determineStatus(NormalizedParsedExpenseItem item) {
		// 필수 필드 체크
		if (item.merchantName() == null || item.localAmount() == null) {
			return TemporaryExpenseStatus.INCOMPLETE;
		}

		// 선택 필드 누락 체크 (Category is now often null from document, so maybe rely less on
		// it or default it?)
		if (item.occurredAt() == null) {
			return TemporaryExpenseStatus.INCOMPLETE;
		}

		return TemporaryExpenseStatus.NORMAL;
	}

	/**
	 * 카테고리 문자열 → Enum 변환
	 */
	private Category parseCategory(String categoryStr) {
		if (categoryStr == null || categoryStr.isBlank()) return Category.UNCLASSIFIED;
		String trimmed = categoryStr.trim();
		if (trimmed.matches("\\d+")) {
			int ordinal;
			try {
				ordinal = Integer.parseInt(trimmed);
			} catch (NumberFormatException e) {
				return Category.UNCLASSIFIED;
			}
			try {
				return Category.fromOrdinal(ordinal);
			} catch (IllegalArgumentException e) {
				return Category.UNCLASSIFIED;
			}
		}
		String normalized = trimmed.replaceAll("[\\s_-]", "").toUpperCase(Locale.ROOT);
		switch (normalized) {
			case "TRANSPORTATION":
				return Category.TRANSPORT;
			case "ACCOMMODATION":
			case "HOUSING":
			case "RESIDENCE":
				return Category.RESIDENCE;
			case "ENTERTAINMENT":
				return Category.LEISURE;
			case "EDUCATION":
			case "SCHOOL":
				return Category.ACADEMIC;
			default:
				break;
		}
		try {
			return Category.valueOf(normalized);
		} catch (IllegalArgumentException e) {
			return Category.UNCLASSIFIED;
		}
	}

	/**
	 * 통화 코드 문자열 → Enum 변환
	 */
	private CurrencyCode parseCurrencyCode(String currencyStr) {
		return parseCurrencyCode(currencyStr, CurrencyCode.KRW);
	}

	private CurrencyCode parseCurrencyCode(String currencyStr, CurrencyCode defaultCurrency) {
		if (currencyStr == null || currencyStr.isBlank()) return defaultCurrency;
		try {
			String clean = currencyStr.replaceAll("[^A-Za-z]", "").toUpperCase();
			if (clean.isBlank()) {
				return defaultCurrency;
			}
			return CurrencyCode.valueOf(clean);
		} catch (IllegalArgumentException e) {
			return defaultCurrency;
		}
	}

	/**
	 * 여러 파일 비동기 파싱 (SSE 진행 상황 알림)
	 */
	@org.springframework.scheduling.annotation.Async("parsingExecutor")
	public java.util.concurrent.CompletableFuture<BatchParsingResult> parseBatchFilesAsync(
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
					throw new IllegalArgumentException("가계부와 파일 메타가 일치하지 않습니다.");
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

	private record AccountBookRateContext(
			CurrencyCode baseCurrencyCode, CurrencyCode defaultLocalCurrencyCode) {}

	private record ExchangeRateKey(
			CurrencyCode fromCurrencyCode, CurrencyCode toCurrencyCode, LocalDate date) {}

	private record NormalizedParsedExpenseItem(
			String merchantName,
			Category category,
			CurrencyCode localCurrencyCode,
			BigDecimal localAmount,
			CurrencyCode baseCurrencyCode,
			BigDecimal baseAmount,
			String memo,
			java.time.LocalDateTime occurredAt,
			String cardLastFourDigits,
			String approvalNumber) {}
}
