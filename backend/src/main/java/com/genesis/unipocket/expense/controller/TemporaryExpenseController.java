package com.genesis.unipocket.expense.controller;

import com.genesis.unipocket.auth.annotation.LoginUser;
import com.genesis.unipocket.expense.dto.request.BatchConvertRequest;
import com.genesis.unipocket.expense.dto.request.BatchParseRequest;
import com.genesis.unipocket.expense.dto.request.BatchPresignedUrlRequest;
import com.genesis.unipocket.expense.dto.request.TemporaryExpenseUpdateRequest;
import com.genesis.unipocket.expense.dto.response.BatchConvertResponse;
import com.genesis.unipocket.expense.dto.response.BatchParseResponse;
import com.genesis.unipocket.expense.dto.response.BatchPresignedUrlResponse;
import com.genesis.unipocket.expense.dto.response.ConvertTemporaryExpenseResponse;
import com.genesis.unipocket.expense.dto.response.ParseFileResponse;
import com.genesis.unipocket.expense.dto.response.PresignedUrlResponse;
import com.genesis.unipocket.expense.dto.response.TemporaryExpenseListResponse;
import com.genesis.unipocket.expense.dto.response.TemporaryExpenseResponse;
import com.genesis.unipocket.expense.facade.TemporaryExpenseOrchestrator;
import com.genesis.unipocket.expense.persistence.entity.expense.TemporaryExpense;
import com.genesis.unipocket.expense.service.FileUploadService;
import com.genesis.unipocket.expense.service.TemporaryExpenseConversionService;
import com.genesis.unipocket.expense.service.TemporaryExpenseParsingService;
import com.genesis.unipocket.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
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
	private final FileUploadService fileUploadService;
	private final TemporaryExpenseParsingService parsingService;
	private final com.genesis.unipocket.expense.infrastructure.ParsingProgressPublisher
			progressPublisher;
	private final TemporaryExpenseConversionService conversionService;

	/**
	 * 단일 임시지출내역 변환
	 */
	@PostMapping("/api/temporary-expenses/{tempExpenseId}/convert")
	public ResponseEntity<ApiResponse<ConvertTemporaryExpenseResponse>> convertToExpense(
			@PathVariable Long tempExpenseId) {
		com.genesis.unipocket.expense.persistence.entity.expense.ExpenseEntity expense =
				conversionService.convertToExpense(tempExpenseId);

		ConvertTemporaryExpenseResponse response =
				new ConvertTemporaryExpenseResponse(
						expense.getExpenseId(), java.time.LocalDateTime.now());

		return ResponseEntity.ok(ApiResponse.success(response));
	}

	/**
	 * Batch 변환
	 */
	@PostMapping("/api/temporary-expenses/convert-batch")
	public ResponseEntity<ApiResponse<BatchConvertResponse>> convertBatch(
			@RequestBody @Valid BatchConvertRequest request) {
		TemporaryExpenseConversionService.BatchConversionResult result =
				conversionService.convertBatch(request.tempExpenseIds());

		java.util.List<BatchConvertResponse.ConversionResult> responseResults =
				result.results().stream()
						.map(
								r ->
										new BatchConvertResponse.ConversionResult(
												r.tempExpenseId(),
												r.expenseId(),
												r.status(),
												r.reason()))
						.toList();

		BatchConvertResponse response =
				new BatchConvertResponse(
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
	public ResponseEntity<ApiResponse<BatchPresignedUrlResponse>> createBatchPresignedUrls(
			@RequestBody @Valid BatchPresignedUrlRequest request) {
		java.util.List<FileUploadService.FileUploadResponse> results =
				fileUploadService.createBatchPresignedUrls(
						request.accountBookId(), request.files());

		java.util.List<BatchPresignedUrlResponse.FileUploadInfo> files =
				new java.util.ArrayList<>();
		for (FileUploadService.FileUploadResponse r : results) {
			files.add(
					new BatchPresignedUrlResponse.FileUploadInfo(
							r.fileId(), r.presignedUrl(), r.s3Key()));
		}

		BatchPresignedUrlResponse response = new BatchPresignedUrlResponse(files);

		return ResponseEntity.ok(ApiResponse.success(response));
	}

	/**
	 * 비동기 파싱 시작
	 */
	@PostMapping("/api/temporary-expenses/parse-async")
	public ResponseEntity<ApiResponse<BatchParseResponse>> parseAsync(
			@RequestBody @Valid BatchParseRequest request) {
		String taskId = java.util.UUID.randomUUID().toString();

		// 비동기 파싱 시작
		parsingService.parseBatchFilesAsync(request.fileIds(), taskId);

		BatchParseResponse response =
				new BatchParseResponse(
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
	public ResponseEntity<ApiResponse<PresignedUrlResponse>> createPresignedUrl(
			@RequestBody @Valid com.genesis.unipocket.expense.dto.request.PresignedUrlRequest request) {
		FileUploadService.FileUploadResponse result =
				fileUploadService.createPresignedUrl(
						request.accountBookId(), request.fileName(), request.fileType());

		PresignedUrlResponse response =
				new PresignedUrlResponse(
						result.fileId(), result.presignedUrl(), result.s3Key(), result.expiresIn());

		return ResponseEntity.ok(ApiResponse.success(response));
	}

	/**
	 * 파일 파싱 실행
	 */
	@PostMapping("/api/temporary-expenses/parse")
	public ResponseEntity<ApiResponse<ParseFileResponse>> parseFile(
			@RequestBody @Valid com.genesis.unipocket.expense.dto.request.ParseFileRequest request) {
		TemporaryExpenseParsingService.ParsingResult result =
				parsingService.parseFile(request.fileId());

		// Response 생성
		List<ParseFileResponse.ParsedItemSummary> items = new java.util.ArrayList<>();
		for (TemporaryExpense expense : result.expenses()) {
			items.add(
					new ParseFileResponse.ParsedItemSummary(
							expense.getTempExpenseId(),
							expense.getMerchantName(),
							expense.getStatus() != null ? expense.getStatus().name() : null));
		}

		ParseFileResponse response =
				new ParseFileResponse(
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
			@RequestParam(required = false) TemporaryExpense.TemporaryExpenseStatus status,
			@LoginUser UUID userId) {
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
			@PathVariable Long tempExpenseId, @LoginUser UUID userId) {
		TemporaryExpenseResponse response = orchestrator.getTemporaryExpense(tempExpenseId, userId);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	/**
	 * 임시지출내역 수정
	 */
	@PutMapping("/api/temporary-expenses/{tempExpenseId}")
	public ResponseEntity<ApiResponse<TemporaryExpenseResponse>> updateTemporaryExpense(
			@PathVariable Long tempExpenseId,
			@RequestBody @Valid TemporaryExpenseUpdateRequest request,
			@LoginUser UUID userId) {
		TemporaryExpenseResponse response =
				orchestrator.updateTemporaryExpense(tempExpenseId, request, userId);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	/**
	 * 임시지출내역 삭제
	 */
	@DeleteMapping("/api/temporary-expenses/{tempExpenseId}")
	public ResponseEntity<Void> deleteTemporaryExpense(
			@PathVariable Long tempExpenseId, @LoginUser UUID userId) {
		orchestrator.deleteTemporaryExpense(tempExpenseId, userId);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}
}
