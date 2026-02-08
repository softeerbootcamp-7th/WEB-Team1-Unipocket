package com.genesis.unipocket.expense.application;

import com.genesis.unipocket.global.infrastructure.gemini.GeminiService.ParsedExpenseItem;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <b>CSV 파싱 서비스</b>
 *
 * @author 김동균
 * @since 2026-02-08
 */
@Slf4j
@Service
public class CsvParsingService {

	private static final DateTimeFormatter[] DATE_FORMATTERS = {
		DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
		DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
		DateTimeFormatter.ofPattern("yyyy-MM-dd"),
		DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"),
		DateTimeFormatter.ofPattern("yyyy/MM/dd"),
		DateTimeFormatter.ISO_LOCAL_DATE_TIME
	};

	/**
	 * CSV 텍스트를 파싱하여 ParsedExpenseItem 목록으로 변환
	 *
	 * @param csvContent CSV 텍스트
	 * @return 파싱된 지출내역 목록
	 */
	public List<ParsedExpenseItem> parseCsv(String csvContent) {
		log.info("Parsing CSV content, length: {}", csvContent.length());

		try {
			List<String[]> rows = parseCsvContent(csvContent);
			if (rows.isEmpty()) {
				log.warn("CSV is empty");
				return Collections.emptyList();
			}

			// 헤더 감지
			Map<String, Integer> headerMap = detectHeaders(rows.get(0));
			log.info("Detected headers: {}", headerMap);

			// 데이터 변환
			List<ParsedExpenseItem> items = new ArrayList<>();
			for (int i = 1; i < rows.size(); i++) {
				String[] row = rows.get(i);
				try {
					ParsedExpenseItem item = mapRowToExpenseItem(row, headerMap);
					if (item != null) {
						items.add(item);
					}
				} catch (Exception e) {
					log.warn("Failed to parse row {}: {}", i, e.getMessage());
				}
			}

			log.info("Successfully parsed {} items from CSV", items.size());
			return items;

		} catch (IOException e) {
			log.error("Failed to parse CSV", e);
			return Collections.emptyList();
		}
	}

	/**
	 * CSV 텍스트를 행 배열로 파싱
	 */
	private List<String[]> parseCsvContent(String csvContent) throws IOException {
		List<String[]> rows = new ArrayList<>();
		try (BufferedReader reader = new BufferedReader(new StringReader(csvContent))) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.trim().isEmpty()) {
					continue;
				}
				String[] fields = splitCsvLine(line);
				rows.add(fields);
			}
		}
		return rows;
	}

	/**
	 * CSV 라인을 필드로 분할 (쉼표 구분, 따옴표 처리)
	 */
	private String[] splitCsvLine(String line) {
		List<String> fields = new ArrayList<>();
		StringBuilder currentField = new StringBuilder();
		boolean inQuotes = false;

		for (int i = 0; i < line.length(); i++) {
			char c = line.charAt(i);

			if (c == '"') {
				inQuotes = !inQuotes;
			} else if (c == ',' && !inQuotes) {
				fields.add(currentField.toString().trim());
				currentField = new StringBuilder();
			} else {
				currentField.append(c);
			}
		}
		fields.add(currentField.toString().trim());

		return fields.toArray(new String[0]);
	}

	/**
	 * 헤더 행에서 컬럼 이름 → 인덱스 매핑
	 */
	private Map<String, Integer> detectHeaders(String[] headers) {
		Map<String, Integer> map = new HashMap<>();

		for (int i = 0; i < headers.length; i++) {
			String header = headers[i].toLowerCase().trim();

			// 가맹점명
			if (header.contains("merchant")
					|| header.contains("가맹점")
					|| header.contains("상호")
					|| header.contains("store")) {
				map.put("merchantName", i);
			}
			// 카테고리
			else if (header.contains("category")
					|| header.contains("카테고리")
					|| header.contains("분류")) {
				map.put("category", i);
			}
			// 금액
			else if (header.contains("amount")
					|| header.contains("금액")
					|| header.contains("price")
					|| header.contains("cost")) {
				map.put("amount", i);
			}
			// 통화
			else if (header.contains("currency") || header.contains("통화")) {
				map.put("currency", i);
			}
			// 날짜/시간
			else if (header.contains("date")
					|| header.contains("날짜")
					|| header.contains("일시")
					|| header.contains("time")) {
				map.put("date", i);
			}
			// 메모
			else if (header.contains("memo")
					|| header.contains("메모")
					|| header.contains("description")
					|| header.contains("비고")) {
				map.put("memo", i);
			}
			// 카드 번호
			else if (header.contains("card") || header.contains("카드")) {
				map.put("card", i);
			}
		}

		return map;
	}

	/**
	 * CSV 행을 ParsedExpenseItem으로 변환
	 */
	private ParsedExpenseItem mapRowToExpenseItem(String[] row, Map<String, Integer> headerMap) {
		String merchantName = getField(row, headerMap, "merchantName");
		String amountStr = getField(row, headerMap, "amount");
		String dateStr = getField(row, headerMap, "date");

		// 필수 필드 확인
		if (merchantName == null || merchantName.isBlank()) {
			return null;
		}

		// 금액 파싱
		BigDecimal amount = parseAmount(amountStr);
		if (amount == null) {
			return null;
		}

		// 날짜 파싱
		LocalDateTime occurredAt = parseDate(dateStr);

		// 카테고리
		String categoryStr = getField(row, headerMap, "category");
		com.genesis.unipocket.expense.common.enums.Category category = parseCategory(categoryStr);

		// 통화
		String currencyStr = getField(row, headerMap, "currency");
		com.genesis.unipocket.global.common.enums.CurrencyCode currency =
				parseCurrencyCode(currencyStr);

		// 메모
		String memo = getField(row, headerMap, "memo");

		// 카드 번호
		String card = getField(row, headerMap, "card");

		return new ParsedExpenseItem(
				merchantName,
				category != null ? category.name() : null,
				amount,
				currency != null ? currency.name() : null,
				occurredAt,
				card,
				memo);
	}

	private String getField(String[] row, Map<String, Integer> headerMap, String key) {
		Integer idx = headerMap.get(key);
		if (idx == null || idx >= row.length) {
			return null;
		}
		String value = row[idx];
		return (value == null || value.isBlank()) ? null : value.trim();
	}

	private BigDecimal parseAmount(String amountStr) {
		if (amountStr == null || amountStr.isBlank()) {
			return null;
		}

		try {
			// 통화 기호, 쉼표 제거
			String cleaned = amountStr.replaceAll("[^0-9.]", "");
			return new BigDecimal(cleaned);
		} catch (NumberFormatException e) {
			log.warn("Failed to parse amount: {}", amountStr);
			return null;
		}
	}

	private LocalDateTime parseDate(String dateStr) {
		if (dateStr == null || dateStr.isBlank()) {
			return LocalDateTime.now(); // 기본값
		}

		for (DateTimeFormatter formatter : DATE_FORMATTERS) {
			try {
				return LocalDateTime.parse(dateStr, formatter);
			} catch (DateTimeParseException ignored) {
				// Try next formatter
			}
		}

		log.warn("Failed to parse date: {}, using current time", dateStr);
		return LocalDateTime.now();
	}

	private com.genesis.unipocket.expense.common.enums.Category parseCategory(String categoryStr) {
		// TODO: 카테고리 매핑 로직
		return null;
	}

	private com.genesis.unipocket.global.common.enums.CurrencyCode parseCurrencyCode(
			String currencyStr) {
		if (currencyStr == null || currencyStr.isBlank()) {
			return com.genesis.unipocket.global.common.enums.CurrencyCode.KRW;
		}

		try {
			return com.genesis.unipocket.global.common.enums.CurrencyCode.valueOf(
					currencyStr.toUpperCase());
		} catch (IllegalArgumentException e) {
			return com.genesis.unipocket.global.common.enums.CurrencyCode.KRW;
		}
	}
}
