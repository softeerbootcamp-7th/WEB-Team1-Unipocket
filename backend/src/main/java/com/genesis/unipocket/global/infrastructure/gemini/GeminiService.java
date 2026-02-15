package com.genesis.unipocket.global.infrastructure.gemini;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
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
@RequiredArgsConstructor
public class GeminiService {

	private final RestTemplate restTemplate;
	private final ObjectMapper objectMapper;

	@Value("${gemini.api.key}")
	private String apiKey;

	@Value("${gemini.api.model:gemini-1.5-flash}")
	private String model;

	@Value("${gemini.api.endpoint}")
	private String endpoint;

	/**
	 * 영수증 이미지 파싱 (S3 URL 전달)
	 *
	 * @param imageUrl S3 presigned GET URL
	 * @return 파싱 결과
	 */
	public GeminiParseResponse parseReceiptImage(String imageUrl) {
		log.info("Parsing receipt image from URL: {}", imageUrl);

		String prompt = buildReceiptParsingPrompt();

		try {
			String url = endpoint + "/models/" + model + ":generateContent?key=" + apiKey;

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			// Gemini API request body
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
																	"mimeType",
																	"image/jpeg",
																	"fileUri",
																	imageUrl))))));

			HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

			log.debug("Calling Gemini API: {}", url);
			ResponseEntity<String> response =
					restTemplate.postForEntity(url, request, String.class);

			if (!response.getStatusCode().is2xxSuccessful()) {
				log.error("Gemini API call failed with status: {}", response.getStatusCode());
				return new GeminiParseResponse(false, List.of(), "API call failed");
			}

			return parseGeminiResponse(response.getBody());

		} catch (RestClientException e) {
			log.error("Failed to call Gemini API", e);
			return new GeminiParseResponse(false, List.of(), e.getMessage());
		} catch (Exception e) {
			log.error("Unexpected error during parsing", e);
			return new GeminiParseResponse(false, List.of(), e.getMessage());
		}
	}

	/**
	 * CSV 파일 파싱
	 *
	 * @param csvContent CSV 문자열
	 * @return 파싱 결과
	 */
	/**
	 * 문서 샘플을 기반으로 스키마 추론
	 *
	 * @param sampleContent 문서 샘플 (헤더 + 3개 행)
	 * @return 추론된 스키마
	 */
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
					Map.of("contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))));

			HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

			log.debug("Calling Gemini API: {}", url);
			ResponseEntity<String> response =
					restTemplate.postForEntity(url, request, String.class);

			if (!response.getStatusCode().is2xxSuccessful()) {
				log.error("Gemini API call failed with status: {}", response.getStatusCode());
				return new GeminiParseResponse(false, List.of(), "API call failed");
			}

			return parseGeminiResponse(response.getBody());

		} catch (RestClientException e) {
			log.error("Failed to call Gemini API", e);
			return new GeminiParseResponse(false, List.of(), e.getMessage());
		} catch (Exception e) {
			log.error("Unexpected error during document parsing", e);
			return new GeminiParseResponse(false, List.of(), e.getMessage());
		}
	}

	/**
	 * 문서(CSV/Excel 텍스트) 파싱 프롬프트 생성
	 */
	private String buildDocumentParsingPrompt() {
		return """
				You are an expert at parsing expense documents (CSV, Excel text).

				Parse the provided text content and convert each row/item to the following JSON format:

				{
				"items": [
					{
					"merchantName": "가맹점명 (필수)",
					"category": "FOOD, TRANSPORTATION, ACCOMMODATION, SHOPPING, ENTERTAINMENT, UNCLASSIFIED",
					"localAmount": "현지 금액 (필수, 숫자만)",
					"localCurrency": "현지 통화 코드 (필수, KRW, USD, etc)",
					"baseAmount": "청구(본국) 금액 (있는 경우 작성, 숫자만)",
					"baseCurrency": "청구(본국) 통화 코드 (있는 경우 작성, KRW, USD, etc)",
					"occurredAt": "거래일시 (ISO 8601: YYYY-MM-DDTHH:mm:ss)",
					"cardLastFourDigits": "카드번호 뒷4자리",
					"approvalNumber": "승인번호 (있는 경우 작성)",
					"memo": "메모/적요"
					}
				]
				}

				Rules:
				- Auto-detect headers/columns from the text
				- Map columns intelligently (e.g., 'Store' -> merchantName, 'Cost' -> localAmount)
				- Handle various date formats and convert to ISO 8601
				- If currency is missing, infer from context or default to KRW
				- Ignore header rows or summary rows (total, etc.)
				- Return ONLY the JSON, no additional text
				""";
	}

	/**
	 * Gemini API 응답 파싱
	 */
	private GeminiParseResponse parseGeminiResponse(String responseBody) {
		try {
			JsonNode root = objectMapper.readTree(responseBody);

			// Extract text from response
			String text =
					root.path("candidates")
							.get(0)
							.path("content")
							.path("parts")
							.get(0)
							.path("text")
							.asText();

			log.debug("Gemini response text: {}", text);

			// Extract JSON from markdown code block if present
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

			// Parse JSON response
			JsonNode parsedData = objectMapper.readTree(jsonText);
			JsonNode items = parsedData.path("items");

			List<ParsedExpenseItem> expenseItems = new ArrayList<>();

			for (JsonNode item : items) {
				ParsedExpenseItem expenseItem =
						new ParsedExpenseItem(
								item.path("merchantName").asText(null),
								item.path("category").asText(null),
								item.has("localAmount")
										? new BigDecimal(item.path("localAmount").asText())
										: null,
								item.path("localCurrency").asText(null),
								item.has("baseAmount")
										? new BigDecimal(item.path("baseAmount").asText())
										: null,
								item.path("baseCurrency").asText(null),
								item.has("occurredAt")
										? LocalDateTime.parse(
												item.path("occurredAt").asText(),
												DateTimeFormatter.ISO_DATE_TIME)
										: null,
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

	/**
	 * 영수증 파싱 프롬프트 생성
	 */
	private String buildReceiptParsingPrompt() {
		return """
				You are an expert at extracting structured expense data from receipt images.

				Analyze the provided receipt image and extract the following information in JSON format:

				{
				"items": [
					{
					"merchantName": "상호명",
					"category": "one of: FOOD, TRANSPORTATION, ACCOMMODATION, SHOPPING, ENTERTAINMENT, UNCLASSIFIED",
					"localAmount": "결제 금액 (숫자만)",
					"localCurrency": "통화 코드 (KRW, USD, JPY 등)",
					"baseAmount": "청구 금액 (있는 경우 작성, 숫자만)",
					"baseCurrency": "청구 통화 코드 (있는 경우 작성, KRW 등)",
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
				- Return ONLY the JSON, no additional text
				""";
	}

	/**
	 * Gemini API 파싱 응답 (영수증)
	 */
	public record GeminiParseResponse(
			boolean success, List<ParsedExpenseItem> items, String errorMessage) {}

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
