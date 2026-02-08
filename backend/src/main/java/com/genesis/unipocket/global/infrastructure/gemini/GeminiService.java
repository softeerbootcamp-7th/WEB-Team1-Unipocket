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
	public GeminiParseResponse parseCsv(String csvContent) {
		log.info("Parsing CSV content (length: {})", csvContent.length());

		String prompt = buildCsvParsingPrompt() + "\n\nCSV Content:\n" + csvContent;

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
			log.error("Unexpected error during CSV parsing", e);
			return new GeminiParseResponse(false, List.of(), e.getMessage());
		}
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
								item.has("occurredAt")
										? LocalDateTime.parse(
												item.path("occurredAt").asText(),
												DateTimeFormatter.ISO_DATE_TIME)
										: null,
								item.path("cardLastFourDigits").asText(null),
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
					"occurredAt": "결제 일시 (ISO 8601 format: YYYY-MM-DDTHH:mm:ss)",
					"cardLastFourDigits": "카드 뒷 4자리 (없으면 null)",
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
	 * CSV 파싱 프롬프트 생성
	 */
	private String buildCsvParsingPrompt() {
		return """
				You are an expert at parsing expense CSV files.

				Parse the provided CSV content and convert each row to the following JSON format:

				{
				"items": [
					{
					"merchantName": "가맹점명",
					"category": "FOOD, TRANSPORTATION, ACCOMMODATION, SHOPPING, ENTERTAINMENT, UNCLASSIFIED",
					"localAmount": "금액",
					"localCurrency": "통화 코드",
					"occurredAt": "거래일시 (ISO 8601)",
					"cardLastFourDigits": "카드번호 뒷자리",
					"memo": "메모"
					}
				]
				}

				Rules:
				- Auto-detect CSV headers
				- Map columns to appropriate fields intelligently
				- Handle different date/currency formats
				- Default missing fields to null
				- Return ONLY the JSON, no additional text
				""";
	}

	/**
	 * Gemini API 파싱 응답
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
			LocalDateTime occurredAt,
			String cardLastFourDigits,
			String memo) {}
}
