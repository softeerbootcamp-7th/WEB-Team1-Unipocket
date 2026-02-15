package com.genesis.unipocket.tempexpense.command.presentation;

import com.genesis.unipocket.auth.common.annotation.LoginUser;
import com.genesis.unipocket.expense.command.persistence.entity.ExpenseEntity;
import com.genesis.unipocket.global.response.ApiResponse;
import com.genesis.unipocket.tempexpense.command.application.result.BatchConversionResult;
import com.genesis.unipocket.tempexpense.command.application.result.FileRegisterResult;
import com.genesis.unipocket.tempexpense.command.application.result.FileUploadResult;
import com.genesis.unipocket.tempexpense.command.application.result.ParsingResult;
import com.genesis.unipocket.tempexpense.command.application.result.TemporaryExpenseResult;
import com.genesis.unipocket.tempexpense.command.facade.TemporaryExpenseCommandFacade;
import com.genesis.unipocket.tempexpense.command.persistence.entity.TemporaryExpense;
import com.genesis.unipocket.tempexpense.command.presentation.request.BatchConvertRequest;
import com.genesis.unipocket.tempexpense.command.presentation.request.BatchParseRequest;
import com.genesis.unipocket.tempexpense.command.presentation.request.ParseFileRequest;
import com.genesis.unipocket.tempexpense.command.presentation.request.PresignedUrlRequest;
import com.genesis.unipocket.tempexpense.command.presentation.request.RegisterUploadedFileRequest;
import com.genesis.unipocket.tempexpense.command.presentation.request.TemporaryExpenseUpdateRequest;
import com.genesis.unipocket.tempexpense.command.presentation.response.BatchConvertResponse;
import com.genesis.unipocket.tempexpense.command.presentation.response.BatchParseResponse;
import com.genesis.unipocket.tempexpense.command.presentation.response.ConvertTemporaryExpenseResponse;
import com.genesis.unipocket.tempexpense.command.presentation.response.ParseFileResponse;
import com.genesis.unipocket.tempexpense.command.presentation.response.PresignedUrlResponse;
import com.genesis.unipocket.tempexpense.command.presentation.response.RegisterUploadedFileResponse;
import com.genesis.unipocket.tempexpense.query.presentation.response.TemporaryExpenseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * <b>임시지출내역 컨트롤러</b>
 *
 * @author 김동균
 * @since 2026-02-08
 */
@Tag(name = "임시지출내역 기능")
@RestController
@RequiredArgsConstructor
public class TemporaryExpenseCommandController {

	private final TemporaryExpenseCommandFacade temporaryExpenseCommandFacade;

	/**
	 * 단일 업로드 Presigned URL 발급
	 *
	 * TODO: media 도메인 포트 연동 구현 시 controller -> facade 호출로 교체
	 */
	@Operation(
			summary = "임시지출 업로드 URL 발급",
			description = "임시지출 파일 업로드를 위한 presigned URL과 메타 정보를 발급합니다.")
	@PostMapping("/account-books/{accountBookId}/temporary-expenses/uploads/presigned-url")
	public ResponseEntity<ApiResponse<PresignedUrlResponse>> createPresignedUrl(
			@PathVariable Long accountBookId,
			@RequestBody @Valid PresignedUrlRequest request,
			@LoginUser UUID userId) {

		FileUploadResult result =
				temporaryExpenseCommandFacade.createPresignedUrl(
						accountBookId, request.fileName(), request.fileType(), userId);

		return ResponseEntity.ok(ApiResponse.success(toPresignedUrlResponse(result)));
	}

	/**
	 * S3 업로드 파일 등록 (s3Key 기반)
	 */
	@Operation(summary = "임시지출 업로드 파일 등록", description = "업로드된 파일의 s3Key를 등록하고 파싱 대상 메타를 생성합니다.")
	@PostMapping("/account-books/{accountBookId}/temporary-expenses/uploads/register")
	public ResponseEntity<ApiResponse<RegisterUploadedFileResponse>> registerUploadedFile(
			@PathVariable Long accountBookId,
			@RequestBody @Valid RegisterUploadedFileRequest request,
			@LoginUser UUID userId) {
		FileRegisterResult result =
				temporaryExpenseCommandFacade.registerUploadedFile(
						accountBookId, request.s3Key(), request.fileType(), userId);

		RegisterUploadedFileResponse response =
				new RegisterUploadedFileResponse(result.tempExpenseMetaId(), result.s3Key());

		return ResponseEntity.ok(ApiResponse.success(response));
	}

	/**
	 * 단일 임시지출내역 변환
	 */
	@Operation(summary = "임시지출 단건 변환", description = "임시지출 1건을 검증 후 expense 도메인의 정식 지출내역으로 변환합니다.")
	@PostMapping("/account-books/{accountBookId}/temporary-expenses/{tempExpenseId}/convert")
	public ResponseEntity<ApiResponse<ConvertTemporaryExpenseResponse>> convertToExpense(
			@PathVariable Long accountBookId,
			@PathVariable Long tempExpenseId,
			@LoginUser UUID userId) {
		ExpenseEntity expense =
				temporaryExpenseCommandFacade.convertToExpense(
						accountBookId, tempExpenseId, userId);

		ConvertTemporaryExpenseResponse response =
				new ConvertTemporaryExpenseResponse(
						expense.getExpenseId(), java.time.LocalDateTime.now());

		return ResponseEntity.ok(ApiResponse.success(response));
	}

	/**
	 * Batch 변환
	 */
	@Operation(
			summary = "(중복/AI 삭제 추천) 임시지출 일괄 변환",
			description = "여러 임시지출을 일괄 변환합니다. 메타 confirm API와 역할이 중복되어 삭제를 권장합니다.")
	@PostMapping("/account-books/{accountBookId}/temporary-expenses/convert-batch")
	public ResponseEntity<ApiResponse<BatchConvertResponse>> convertBatch(
			@PathVariable Long accountBookId,
			@RequestBody @Valid BatchConvertRequest request,
			@LoginUser UUID userId) {
		BatchConversionResult result =
				temporaryExpenseCommandFacade.convertBatch(
						accountBookId, request.tempExpenseIds(), userId);

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

	@Operation(
			summary = "메타 기준 변환 확정",
			description = "파일 메타 단위로 임시지출을 확정 변환합니다. tempExpenseIds가 있으면 부분 확정이 가능합니다.")
	@PostMapping(
			"/account-books/{accountBookId}/temporary-expense-metas/{tempExpenseMetaId}/confirm")
	public ResponseEntity<ApiResponse<BatchConvertResponse>> confirmByMeta(
			@PathVariable Long accountBookId,
			@PathVariable Long tempExpenseMetaId,
			@RequestBody(required = false) BatchConvertRequest request,
			@LoginUser UUID userId) {

		BatchConversionResult result =
				temporaryExpenseCommandFacade.confirmByMeta(
						accountBookId,
						tempExpenseMetaId,
						request != null ? request.tempExpenseIds() : null,
						userId);

		List<BatchConvertResponse.ConversionResult> responseResults =
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
	 * 비동기 파싱 시작
	 */
	@Operation(
			summary = "임시지출 비동기 파싱 시작",
			description = "파일 목록 파싱 작업을 비동기로 시작하고 진행 조회용 taskId를 반환합니다.")
	@PostMapping("/account-books/{accountBookId}/temporary-expenses/parse-async")
	public ResponseEntity<ApiResponse<BatchParseResponse>> parseAsync(
			@PathVariable Long accountBookId,
			@RequestBody @Valid BatchParseRequest request,
			@LoginUser UUID userId) {
		String taskId =
				temporaryExpenseCommandFacade.startParseAsync(
						accountBookId, request.s3Keys(), userId);

		BatchParseResponse response =
				new BatchParseResponse(
						taskId,
						request.s3Keys().size(),
						"/account-books/"
								+ accountBookId
								+ "/temporary-expenses/parse-status/"
								+ taskId);

		return ResponseEntity.accepted().body(ApiResponse.success(response));
	}

	/**
	 * 파일 파싱 실행
	 */
	@Operation(
			summary = "(중복/AI 삭제 추천) 임시지출 동기 파싱",
			description = "단건 동기 파싱 API입니다. 비동기 파싱+상태조회 흐름과 중복되어 삭제를 권장합니다.")
	@PostMapping("/account-books/{accountBookId}/temporary-expenses/parse")
	public ResponseEntity<ApiResponse<ParseFileResponse>> parseFile(
			@PathVariable Long accountBookId,
			@RequestBody @Valid ParseFileRequest request,
			@LoginUser UUID userId) {
		ParsingResult result =
				temporaryExpenseCommandFacade.parseFile(accountBookId, request.s3Key(), userId);

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
	 * 임시지출내역 수정
	 */
	@Operation(
			summary = "(중복/AI 삭제 추천) 임시지출 수정(전역 경로)",
			description = "accountBook 스코프 없는 전역 경로입니다. 경로 일관성 유지를 위해 스코프 경로로 통합을 권장합니다.")
	@PutMapping("/temporary-expenses/{tempExpenseId}")
	public ResponseEntity<ApiResponse<TemporaryExpenseResponse>> updateTemporaryExpense(
			@PathVariable Long tempExpenseId,
			@RequestBody @Valid TemporaryExpenseUpdateRequest request,
			@LoginUser UUID userId) {
		TemporaryExpenseResult result =
				temporaryExpenseCommandFacade.updateTemporaryExpense(
						tempExpenseId, request, userId);
		TemporaryExpenseResponse response =
				new TemporaryExpenseResponse(
						result.tempExpenseId(),
						result.tempExpenseMetaId(),
						result.merchantName(),
						result.category(),
						result.localCountryCode(),
						result.localCurrencyAmount(),
						result.baseCountryCode(),
						result.baseCurrencyAmount(),
						result.paymentsMethod(),
						result.memo(),
						result.occurredAt(),
						result.status(),
						result.cardLastFourDigits());
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	/**
	 * 임시지출내역 삭제
	 */
	@Operation(summary = "임시지출 메타 삭제", description = "메타와 연결된 임시지출/파일을 함께 삭제합니다.")
	@DeleteMapping("/account-books/{accountBookId}/temporary-expense-metas/{tempExpenseMetaId}")
	public ResponseEntity<Void> deleteMeta(
			@PathVariable Long accountBookId,
			@PathVariable Long tempExpenseMetaId,
			@LoginUser UUID userId) {
		temporaryExpenseCommandFacade.deleteMeta(accountBookId, tempExpenseMetaId, userId);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

	@Operation(
			summary = "(중복/AI 삭제 추천) 임시지출 삭제(전역 경로)",
			description = "accountBook 스코프 없는 전역 경로입니다. 메타/가계부 스코프 경로로 통합을 권장합니다.")
	@DeleteMapping("/temporary-expenses/{tempExpenseId}")
	public ResponseEntity<Void> deleteTemporaryExpense(
			@PathVariable Long tempExpenseId, @LoginUser UUID userId) {
		temporaryExpenseCommandFacade.deleteTemporaryExpense(tempExpenseId, userId);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

	private PresignedUrlResponse toPresignedUrlResponse(FileUploadResult result) {
		return new PresignedUrlResponse(
				result.tempExpenseMetaId(),
				result.presignedUrl(),
				result.s3Key(),
				result.expiresIn());
	}
}
