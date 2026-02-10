package com.genesis.unipocket.expense.command.presentation;

import com.genesis.unipocket.auth.annotation.LoginUser;
import com.genesis.unipocket.expense.command.application.FileUploadService;
import com.genesis.unipocket.expense.command.application.TemporaryExpenseConversionService;
import com.genesis.unipocket.expense.command.application.TemporaryExpenseParsingService;
import com.genesis.unipocket.expense.command.application.result.TemporaryExpenseResult;
import com.genesis.unipocket.expense.command.facade.TemporaryExpenseCommandFacade;
import com.genesis.unipocket.expense.command.presentation.request.BatchConvertRequest;
import com.genesis.unipocket.expense.command.presentation.request.BatchParseRequest;
import com.genesis.unipocket.expense.command.presentation.request.BatchPresignedUrlRequest;
import com.genesis.unipocket.expense.command.presentation.request.ParseFileRequest;
import com.genesis.unipocket.expense.command.presentation.request.PresignedUrlRequest;
import com.genesis.unipocket.expense.command.presentation.request.RegisterUploadedFileRequest;
import com.genesis.unipocket.expense.command.presentation.request.TemporaryExpenseUpdateRequest;
import com.genesis.unipocket.expense.command.presentation.response.BatchConvertResponse;
import com.genesis.unipocket.expense.command.presentation.response.BatchParseResponse;
import com.genesis.unipocket.expense.command.presentation.response.BatchPresignedUrlResponse;
import com.genesis.unipocket.expense.command.presentation.response.ConvertTemporaryExpenseResponse;
import com.genesis.unipocket.expense.command.presentation.response.ParseFileResponse;
import com.genesis.unipocket.expense.command.presentation.response.PresignedUrlResponse;
import com.genesis.unipocket.expense.command.presentation.response.RegisterUploadedFileResponse;
import com.genesis.unipocket.expense.command.presentation.response.TemporaryExpenseUpdateResponse;
import com.genesis.unipocket.expense.common.validator.AccountBookOwnershipValidator;
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
public class TemporaryExpenseCommandController {

	private final TemporaryExpenseCommandFacade orchestrator;
	private final FileUploadService fileUploadService;
	private final TemporaryExpenseParsingService parsingService;
	private final TemporaryExpenseConversionService conversionService;
	private final AccountBookOwnershipValidator accountBookOwnershipValidator;

	/**
	 * 단일 임시지출내역 변환
	 */
	@PostMapping("/api/temporary-expenses/{tempExpenseId}/convert")
	public ResponseEntity<ApiResponse<ConvertTemporaryExpenseResponse>> convertToExpense(
			@PathVariable Long tempExpenseId) {
		com.genesis.unipocket.expense.command.persistence.entity.expense.ExpenseEntity expense =
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
	 * Presigned URL 발급
	 */
	@PostMapping("/api/temporary-expenses/upload/presigned-url")
	public ResponseEntity<ApiResponse<PresignedUrlResponse>> createPresignedUrl(
			@RequestBody @Valid PresignedUrlRequest request) {
		FileUploadService.FileUploadResponse result =
				fileUploadService.createPresignedUrl(
						request.accountBookId(), request.fileName(), request.fileType());

		PresignedUrlResponse response =
				new PresignedUrlResponse(
						result.fileId(), result.presignedUrl(), result.s3Key(), result.expiresIn());

		return ResponseEntity.ok(ApiResponse.success(response));
	}

	/**
	 * S3 업로드 파일 등록 (s3Key 기반)
	 */
	@PostMapping("/api/temporary-expenses/upload/register")
	public ResponseEntity<ApiResponse<RegisterUploadedFileResponse>> registerUploadedFile(
			@RequestBody @Valid RegisterUploadedFileRequest request, @LoginUser UUID userId) {
		accountBookOwnershipValidator.validateOwnership(request.accountBookId(), userId.toString());

		FileUploadService.FileRegisterResponse result =
				fileUploadService.registerUploadedFile(
						request.accountBookId(), request.s3Key(), request.fileType());

		RegisterUploadedFileResponse response =
				new RegisterUploadedFileResponse(result.fileId(), result.metaId(), result.s3Key());

		return ResponseEntity.ok(ApiResponse.success(response));
	}

	/**
	 * 파일 파싱 실행
	 */
	@PostMapping("/api/temporary-expenses/parse")
	public ResponseEntity<ApiResponse<ParseFileResponse>> parseFile(
			@RequestBody @Valid ParseFileRequest request) {
		TemporaryExpenseParsingService.ParsingResult result =
				parsingService.parseFile(request.fileId());

		// Response 생성
		List<ParseFileResponse.ParsedItemSummary> items = new java.util.ArrayList<>();
		for (com.genesis.unipocket.expense.command.persistence.entity.expense.TemporaryExpense
				expense : result.expenses()) {
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
	 * 임시지출내역 수정
	 */
	@PutMapping("/api/temporary-expenses/{tempExpenseId}")
	public ResponseEntity<ApiResponse<TemporaryExpenseUpdateResponse>> updateTemporaryExpense(
			@PathVariable Long tempExpenseId,
			@RequestBody @Valid TemporaryExpenseUpdateRequest request,
			@LoginUser UUID userId) {
		TemporaryExpenseResult result =
				orchestrator.updateTemporaryExpense(tempExpenseId, request, userId);
		TemporaryExpenseUpdateResponse response = TemporaryExpenseUpdateResponse.from(result);
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
