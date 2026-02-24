package com.genesis.unipocket.global.infrastructure.gemini;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * <b>Gemini API 서비스</b>
 * <p>
 * 영수증 이미지 및 CSV 파일 파싱
 *
 * @author 김동균
 * @since 2026-02-08
 */
@Slf4j
@Service
public class GeminiService {

	private final RestTemplate restTemplate;
	private final ObjectMapper objectMapper;

	private static final String FILE_FORMAT_PROMPT =
			"""
				{
					"items": [
						{
						"merchantName": "상호명",
						"category": "one of: FOOD, TRANSPORTATION, ACCOMMODATION, SHOPPING, ENTERTAINMENT, UNCLASSIFIED",
						"localAmount": "해외 현지 결제 금액 (해외 거래: 현지 통화 금액, 국내 거래: 결제 금액. 숫자만)",
						"localCurrency": "현지 결제 통화 코드 (해외 거래: 현지 통화 USD/JPY/EUR 등, 국내 거래: KRW)",
						"baseAmount": "자국 통화 환산 금액 (해외 거래에서 카드사가 원화 청구한 금액. 숫자만)",
						"baseCurrency": "자국 통화 코드 (해외 거래 원화 청구 금액이 있는 경우 KRW 등)",
						"occurredAt": "결제 일시 (ISO 8601 format: YYYY-MM-DDTHH:mm:ss)",
						"cardLastFourDigits": "카드 뒷 4자리 (없으면 null)",
						"approvalNumber": "승인번호 (있는 경우 작성)",
						"memo": "추가 메모 (없으면 null)"
						}
					]
				}

				Rules:
				- If multiple items are found, include all in the items array
				- If information is unclear or missing, use null
				- Always provide merchantName and localAmount
				- Default category to "UNCLASSIFIED" if unclear
				- If payment time is not available, use today's date with 12:00:00
				- Use JSON null literal for missing values (do NOT use "null" string)
				- Use JSON number for numeric amounts (do NOT use commas or currency symbols)
				- Return a single valid JSON object only (no markdown/code fence/explanations)
				- For foreign credit card statements: the foreign currency amount (해외이용금액) goes in localAmount/localCurrency, and the home currency billed amount (원화 청구금액/금액) goes in baseAmount/baseCurrency
				- If nothing is parseable, return {"items":[]}
				""";

	public GeminiService(
			@Qualifier("geminiRestTemplate") RestTemplate restTemplate, ObjectMapper objectMapper) {
		this.restTemplate = restTemplate;
		this.objectMapper = objectMapper;
	}

	@Value("${gemini.api.key}")
	private String apiKey;

	@Value("${gemini.api.model}")
	private String model;

	@Value("${gemini.api.endpoint}")
	private String endpoint;

	/**
	 * 영수증 이미지 파싱 (S3 URL 전달)
	 *
	 * @param imageUrl  S3 presigned GET URL
	 * @param mimeType  이미지 MIME 타입
	 * @return 파싱 결과
	 */
	public GeminiParseResponse parseReceiptImage(String imageUrl, String mimeType) {
		log.info("Parsing receipt image from URL: {}", imageUrl);

		String prompt = buildReceiptParsingPrompt();

		try {
			String url = endpoint + "/models/" + model + ":generateContent?key=" + apiKey;

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			Map<String, Object> requestBody =
					Map.of(
							"contents",
							List.of(
									Map.of(
											"parts",
											List.of(
													Map.of("text", prompt),
													Map.of(
															"fileData",
															Map.of(
																	"mimeType", mimeType,
																	"fileUri", imageUrl))))),
							"generationConfig",
							Map.of("responseMimeType", "application/json"));

			return callGeminiApi(url, headers, requestBody);

		} catch (HttpStatusCodeException e) {
			log.error("Gemini API returned non-2xx status: {}", e.getStatusCode(), e);
			return new GeminiParseResponse(
					false, List.of(), e.getMessage(), e.getStatusCode().value());
		} catch (RestClientException e) {
			log.error("Failed to call Gemini API", e);
			return new GeminiParseResponse(false, List.of(), e.getMessage());
		} catch (Exception e) {
			log.error("Unexpected error during parsing", e);
			return new GeminiParseResponse(false, List.of(), e.getMessage());
		}
	}

	/**
	 * 문서 내용 파싱 (CSV, Excel 변환 텍스트 등)
	 *
	 * @param content 문서 텍스트 내용
	 * @return 파싱 결과
	 */
	public GeminiParseResponse parseDocument(String content) {
		log.info("Parsing document content (length: {})", content.length());

		String prompt = buildDocumentParsingPrompt() + "\n\nDocument Content:\n" + content;

		try {
			String url = endpoint + "/models/" + model + ":generateContent?key=" + apiKey;

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			Map<String, Object> requestBody =
					Map.of(
							"contents",
							List.of(Map.of("parts", List.of(Map.of("text", prompt)))),
							"generationConfig",
							Map.of("responseMimeType", "application/json"));

			return callGeminiApi(url, headers, requestBody);

		} catch (HttpStatusCodeException e) {
			log.error("Gemini API returned non-2xx status: {}", e.getStatusCode(), e);
			return new GeminiParseResponse(
					false, List.of(), e.getMessage(), e.getStatusCode().value());
		} catch (RestClientException e) {
			log.error("Failed to call Gemini API", e);
			return new GeminiParseResponse(false, List.of(), e.getMessage());
		} catch (Exception e) {
			log.error("Unexpected error during document parsing", e);
			return new GeminiParseResponse(false, List.of(), e.getMessage());
		}
	}

	private GeminiParseResponse callGeminiApi(
			String url, HttpHeaders headers, Map<String, Object> requestBody) {
		HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

		log.debug("Calling Gemini API: {}", url);
		ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

		if (!response.getStatusCode().is2xxSuccessful()) {
			log.error("Gemini API call failed with status: {}", response.getStatusCode());
			return new GeminiParseResponse(
					false, List.of(), "API call failed", response.getStatusCode().value());
		}

		return parseGeminiResponse(response.getBody());
	}

	/**
	 * 문서(CSV/Excel 텍스트) 파싱 프롬프트 생성
	 */
	private String buildDocumentParsingPrompt() {
		return """
				You are an expert at extracting structured expense data from expense documents.

				Analyze the provided CSV-like text and extract the following information in JSON format:
				"""
				+ FILE_FORMAT_PROMPT;
	}

	/**
	 * Gemini API 응답 파싱
	 */
	private GeminiParseResponse parseGeminiResponse(String responseBody) {
		try {
			JsonNode root = objectMapper.readTree(responseBody);

			String text =
					root.path("candidates")
							.get(0)
							.path("content")
							.path("parts")
							.get(0)
							.path("text")
							.asText();

			log.debug("Gemini response text: {}", text);

			String jsonText = text;
			if (text.contains("```json")) {
				int start = text.indexOf("```json") + 7;
				int end = text.indexOf("```", start);
				jsonText = text.substring(start, end).trim();
			} else if (text.contains("```")) {
				int start = text.indexOf("```") + 3;
				int end = text.indexOf("```", start);
				jsonText = text.substring(start, end).trim();
			}

			JsonNode parsedData = objectMapper.readTree(sanitizeJsonText(jsonText));
			JsonNode items = parsedData.path("items");

			List<ParsedExpenseItem> expenseItems = new ArrayList<>();

			for (JsonNode item : items) {
				ParsedExpenseItem expenseItem =
						new ParsedExpenseItem(
								item.path("merchantName").asText(null),
								item.path("category").asText(null),
								parseBigDecimal(item, "localAmount"),
								item.path("localCurrency").asText(null),
								parseBigDecimal(item, "baseAmount"),
								item.path("baseCurrency").asText(null),
								parseOccurredAt(item),
								item.path("cardLastFourDigits").asText(null),
								item.path("approvalNumber").asText(null),
								item.path("memo").asText(null));

				expenseItems.add(expenseItem);
			}

			return new GeminiParseResponse(true, expenseItems, null);

		} catch (Exception e) {
			log.error("Failed to parse Gemini response", e);
			return new GeminiParseResponse(
					false, List.of(), "Response parsing failed: " + e.getMessage());
		}
	}

	private BigDecimal parseBigDecimal(JsonNode item, String fieldName) {
		JsonNode valueNode = item.path(fieldName);
		if (valueNode.isMissingNode() || valueNode.isNull()) {
			return null;
		}

		String raw = valueNode.asText(null);
		if (raw == null) {
			return null;
		}

		String normalized = raw.trim();
		if (normalized.isEmpty() || "null".equalsIgnoreCase(normalized)) {
			return null;
		}

		normalized = normalized.replace(",", "");
		return new BigDecimal(normalized);
	}

	private LocalDateTime parseOccurredAt(JsonNode item) {
		JsonNode occurredAtNode = item.path("occurredAt");
		if (occurredAtNode.isMissingNode() || occurredAtNode.isNull()) {
			return null;
		}

		String raw = occurredAtNode.asText(null);
		if (raw == null || raw.isBlank() || "null".equalsIgnoreCase(raw.trim())) {
			return null;
		}

		String normalized = raw.trim();
		try {
			return LocalDateTime.parse(normalized, DateTimeFormatter.ISO_DATE_TIME);
		} catch (DateTimeParseException ignored) {
			try {
				return LocalDate.parse(normalized, DateTimeFormatter.ISO_DATE).atTime(12, 0);
			} catch (DateTimeParseException e) {
				log.warn("Failed to parse occurredAt value: {}", normalized);
				return null;
			}
		}
	}

	private String sanitizeJsonText(String text) {
		String trimmed = text == null ? "" : text.trim();
		if (trimmed.isEmpty()) {
			return "{\"items\":[]}";
		}

		int firstBrace = trimmed.indexOf('{');
		int lastBrace = trimmed.lastIndexOf('}');
		if (firstBrace >= 0 && lastBrace > firstBrace) {
			return trimmed.substring(firstBrace, lastBrace + 1);
		}
		return trimmed;
	}

	/**
	 * 영수증 파싱 프롬프트 생성
	 */
	private String buildReceiptParsingPrompt() {
		return """
				You are an expert at extracting structured expense data from receipt images.

				Analyze the provided receipt image and extract the following information in JSON format:
				"""
				+ FILE_FORMAT_PROMPT;
	}

	/**
	 * Gemini API 파싱 응답 (영수증)
	 */
	public record GeminiParseResponse(
			boolean success,
			List<ParsedExpenseItem> items,
			String errorMessage,
			Integer statusCode) {
		public GeminiParseResponse(
				boolean success, List<ParsedExpenseItem> items, String errorMessage) {
			this(success, items, errorMessage, null);
		}

		public boolean isRateLimited() {
			return Integer.valueOf(429).equals(statusCode);
		}
	}

	/**
	 * 파싱된 지출 항목
	 */
	public record ParsedExpenseItem(
			String merchantName,
			String category,
			BigDecimal localAmount,
			String localCurrency,
			BigDecimal baseAmount,
			String baseCurrency,
			LocalDateTime occurredAt,
			String cardLastFourDigits,
			String approvalNumber,
			String memo) {}
}
