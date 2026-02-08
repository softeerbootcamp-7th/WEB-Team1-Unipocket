package com.genesis.unipocket.expense.presentation;

import com.genesis.unipocket.expense.facade.TemporaryExpenseOrchestrator;
import com.genesis.unipocket.expense.presentation.dto.TemporaryExpenseListResponse;
import com.genesis.unipocket.expense.presentation.dto.TemporaryExpenseResponse;
import com.genesis.unipocket.expense.presentation.dto.TemporaryExpenseUpdateRequest;
import com.genesis.unipocket.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * <b>임시지출내역 컨트롤러</b>
 *
 * @author 김동균
 * @since 2026-02-08
 */
@Tag(name = "임시지출내역 기능")
@RestController
@AllArgsConstructor
public class TemporaryExpenseController {

	private final TemporaryExpenseOrchestrator orchestrator;
	private final com.genesis.unipocket.expense.application.FileUploadService fileUploadService;
	private final com.genesis.unipocket.expense.application.TemporaryExpenseParsingService
			parsingService;
	private final com.genesis.unipocket.expense.infrastructure.ParsingProgressPublisher
			progressPublisher;
	private final com.genesis.unipocket.expense.application.TemporaryExpenseConversionService
			conversionService;
	private final Long USER_ID_TEMP = 1L;

	/**
	 * 단일 임시지출내역 변환
	 */
	@PostMapping("/api/temporary-expenses/{tempExpenseId}/convert")
	public ResponseEntity<
					ApiResponse<
							com.genesis.unipocket.expense.presentation.dto
									.ConvertTemporaryExpenseResponse>>
			convertToExpense(@PathVariable Long tempExpenseId) {
		com.genesis.unipocket.expense.persistence.entity.expense.Expense expense =
				conversionService.convertToExpense(tempExpenseId);

		com.genesis.unipocket.expense.presentation.dto.ConvertTemporaryExpenseResponse response =
				new com.genesis.unipocket.expense.presentation.dto.ConvertTemporaryExpenseResponse(
						expense.getExpenseId(), java.time.LocalDateTime.now());

		return ResponseEntity.ok(ApiResponse.success(response));
	}

	/**
	 * Batch 변환
	 */
	@PostMapping("/api/temporary-expenses/convert-batch")
	public ResponseEntity<
					ApiResponse<
							com.genesis.unipocket.expense.presentation.dto.BatchConvertResponse>>
			convertBatch(
					@RequestBody @Valid com.genesis.unipocket.expense.presentation.dto.BatchConvertRequest
									request) {
		com.genesis.unipocket.expense.application.TemporaryExpenseConversionService
						.BatchConversionResult
				result = conversionService.convertBatch(request.tempExpenseIds());

		java.util.List<
						com.genesis.unipocket.expense.presentation.dto.BatchConvertResponse
								.ConversionResult>
				responseResults =
						result.results().stream()
								.map(
										r ->
												new com.genesis.unipocket.expense.presentation.dto
														.BatchConvertResponse.ConversionResult(
														r.tempExpenseId(),
														r.expenseId(),
														r.status(),
														r.reason()))
								.toList();

		com.genesis.unipocket.expense.presentation.dto.BatchConvertResponse response =
				new com.genesis.unipocket.expense.presentation.dto.BatchConvertResponse(
						result.totalRequested(),
						result.successCount(),
						result.failedCount(),
						responseResults);

		return ResponseEntity.ok(ApiResponse.success(response));
	}

	/**
	 * Batch Presigned URL 발급
	 */
	@PostMapping("/api/temporary-expenses/upload/presigned-urls")
	public ResponseEntity<
					ApiResponse<
							com.genesis.unipocket.expense.presentation.dto
									.BatchPresignedUrlResponse>>
			createBatchPresignedUrls(
					@RequestBody @Valid com.genesis.unipocket.expense.presentation.dto.BatchPresignedUrlRequest
									request) {
		java.util.List<
						com.genesis.unipocket.expense.application.FileUploadService
								.FileUploadResponse>
				results =
						fileUploadService.createBatchPresignedUrls(
								request.accountBookId(), request.files());

		java.util.List<
						com.genesis.unipocket.expense.presentation.dto.BatchPresignedUrlResponse
								.FileUploadInfo>
				files = new java.util.ArrayList<>();
		for (com.genesis.unipocket.expense.application.FileUploadService.FileUploadResponse r :
				results) {
			files.add(
					new com.genesis.unipocket.expense.presentation.dto.BatchPresignedUrlResponse
							.FileUploadInfo(r.fileId(), r.presignedUrl(), r.s3Key()));
		}

		com.genesis.unipocket.expense.presentation.dto.BatchPresignedUrlResponse response =
				new com.genesis.unipocket.expense.presentation.dto.BatchPresignedUrlResponse(files);

		return ResponseEntity.ok(ApiResponse.success(response));
	}

	/**
	 * 비동기 파싱 시작
	 */
	@PostMapping("/api/temporary-expenses/parse-async")
	public ResponseEntity<
					ApiResponse<com.genesis.unipocket.expense.presentation.dto.BatchParseResponse>>
			parseAsync(
					@RequestBody @Valid com.genesis.unipocket.expense.presentation.dto.BatchParseRequest
									request) {
		String taskId = java.util.UUID.randomUUID().toString();

		// 비동기 파싱 시작
		parsingService.parseBatchFilesAsync(request.fileIds(), taskId);

		com.genesis.unipocket.expense.presentation.dto.BatchParseResponse response =
				new com.genesis.unipocket.expense.presentation.dto.BatchParseResponse(
						taskId,
						request.fileIds().size(),
						"/api/temporary-expenses/parse-status/" + taskId);

		return ResponseEntity.accepted().body(ApiResponse.success(response));
	}

	/**
	 * SSE 진행 상황 스트림
	 */
	@GetMapping(
			value = "/api/temporary-expenses/parse-status/{taskId}",
			produces = org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE)
	public org.springframework.web.servlet.mvc.method.annotation.SseEmitter streamParsingProgress(
			@PathVariable String taskId) {
		org.springframework.web.servlet.mvc.method.annotation.SseEmitter emitter =
				new org.springframework.web.servlet.mvc.method.annotation.SseEmitter(60000L); // 60초

		progressPublisher.addEmitter(taskId, emitter);

		emitter.onCompletion(() -> progressPublisher.removeEmitter(taskId));
		emitter.onTimeout(() -> progressPublisher.removeEmitter(taskId));
		emitter.onError((e) -> progressPublisher.removeEmitter(taskId));

		return emitter;
	}

	/**
	 * Presigned URL 발급
	 */
	@PostMapping("/api/temporary-expenses/upload/presigned-url")
	public ResponseEntity<
					ApiResponse<
							com.genesis.unipocket.expense.presentation.dto.PresignedUrlResponse>>
			createPresignedUrl(
					@RequestBody @Valid com.genesis.unipocket.expense.presentation.dto.PresignedUrlRequest
									request) {
		com.genesis.unipocket.expense.application.FileUploadService.FileUploadResponse result =
				fileUploadService.createPresignedUrl(
						request.accountBookId(), request.fileName(), request.fileType());

		com.genesis.unipocket.expense.presentation.dto.PresignedUrlResponse response =
				new com.genesis.unipocket.expense.presentation.dto.PresignedUrlResponse(
						result.fileId(), result.presignedUrl(), result.s3Key(), result.expiresIn());

		return ResponseEntity.ok(ApiResponse.success(response));
	}

	/**
	 * 파일 파싱 실행
	 */
	@PostMapping("/api/temporary-expenses/parse")
	public ResponseEntity<
					ApiResponse<com.genesis.unipocket.expense.presentation.dto.ParseFileResponse>>
			parseFile(
					@RequestBody @Valid com.genesis.unipocket.expense.presentation.dto.ParseFileRequest
									request) {
		com.genesis.unipocket.expense.application.TemporaryExpenseParsingService.ParsingResult
				result = parsingService.parseFile(request.fileId());

		// Response 생성
		List<com.genesis.unipocket.expense.presentation.dto.ParseFileResponse.ParsedItemSummary>
				items = new java.util.ArrayList<>();
		for (com.genesis.unipocket.expense.persistence.entity.expense.TemporaryExpense expense :
				result.expenses()) {
			items.add(
					new com.genesis.unipocket.expense.presentation.dto.ParseFileResponse
							.ParsedItemSummary(
							expense.getTempExpenseId(),
							expense.getMerchantName(),
							expense.getStatus() != null ? expense.getStatus().name() : null));
		}

		com.genesis.unipocket.expense.presentation.dto.ParseFileResponse response =
				new com.genesis.unipocket.expense.presentation.dto.ParseFileResponse(
						result.metaId(),
						result.totalCount(),
						result.normalCount(),
						result.incompleteCount(),
						result.abnormalCount(),
						items);

		return ResponseEntity.ok(ApiResponse.success(response));
	}

	/**
	 * 가계부별 임시지출내역 목록 조회 (상태 필터 선택)
	 *
	 * @param accountBookId 가계부 ID
	 * @param status        필터링할 상태 (선택, 없으면 전체 조회)
	 * @return 임시지출내역 목록
	 */
	@GetMapping("/api/account-books/{accountBookId}/temporary-expenses")
	public ResponseEntity<ApiResponse<TemporaryExpenseListResponse>> getTemporaryExpenses(
			@PathVariable Long accountBookId,
			@RequestParam(required = false)
					com.genesis.unipocket.expense.persistence.entity.expense.TemporaryExpense
									.TemporaryExpenseStatus
							status) {
		Long userId = USER_ID_TEMP;
		// TODO: User 객체, 혹은 User ID를 받아올 수 있는 필터 구현

		List<TemporaryExpenseResponse> items =
				orchestrator.getTemporaryExpenses(accountBookId, status, userId);
		TemporaryExpenseListResponse response = new TemporaryExpenseListResponse(items);

		return ResponseEntity.ok(ApiResponse.success(response));
	}

	/**
	 * 임시지출내역 단건 조회
	 */
	@GetMapping("/api/temporary-expenses/{tempExpenseId}")
	public ResponseEntity<ApiResponse<TemporaryExpenseResponse>> getTemporaryExpense(
			@PathVariable Long tempExpenseId) {
		Long userId = USER_ID_TEMP;

		TemporaryExpenseResponse response = orchestrator.getTemporaryExpense(tempExpenseId, userId);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	/**
	 * 임시지출내역 수정
	 */
	@PutMapping("/api/temporary-expenses/{tempExpenseId}")
	public ResponseEntity<ApiResponse<TemporaryExpenseResponse>> updateTemporaryExpense(
			@PathVariable Long tempExpenseId,
			@RequestBody @Valid TemporaryExpenseUpdateRequest request) {
		Long userId = USER_ID_TEMP;

		TemporaryExpenseResponse response =
				orchestrator.updateTemporaryExpense(tempExpenseId, request, userId);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	/**
	 * 임시지출내역 삭제
	 */
	@DeleteMapping("/api/temporary-expenses/{tempExpenseId}")
	public ResponseEntity<Void> deleteTemporaryExpense(@PathVariable Long tempExpenseId) {
		Long userId = USER_ID_TEMP;

		orchestrator.deleteTemporaryExpense(tempExpenseId, userId);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}
}
