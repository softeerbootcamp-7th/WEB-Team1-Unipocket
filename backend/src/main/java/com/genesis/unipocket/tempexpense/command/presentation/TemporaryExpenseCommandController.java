package com.genesis.unipocket.tempexpense.command.presentation;

import com.genesis.unipocket.auth.common.annotation.LoginUser;
import com.genesis.unipocket.tempexpense.command.application.result.BatchConversionResult;
import com.genesis.unipocket.tempexpense.command.application.result.FileUploadResult;
import com.genesis.unipocket.tempexpense.command.facade.TemporaryExpenseCommandFacade;
import com.genesis.unipocket.tempexpense.command.presentation.request.BatchConvertRequest;
import com.genesis.unipocket.tempexpense.command.presentation.request.BatchParseRequest;
import com.genesis.unipocket.tempexpense.command.presentation.request.PresignedUrlRequest;
import com.genesis.unipocket.tempexpense.command.presentation.request.TemporaryExpenseMetaBulkUpdateRequest;
import com.genesis.unipocket.tempexpense.command.presentation.response.BatchConvertResponse;
import com.genesis.unipocket.tempexpense.command.presentation.response.BatchParseResponse;
import com.genesis.unipocket.tempexpense.command.presentation.response.PresignedUrlResponse;
import com.genesis.unipocket.tempexpense.command.presentation.response.TemporaryExpenseMetaBulkUpdateResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
@RequestMapping("/account-books/{accountBookId}")
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
	@PostMapping("/temporary-expenses/uploads/presigned-url")
	public ResponseEntity<PresignedUrlResponse> createPresignedUrl(
			@PathVariable Long accountBookId,
			@RequestBody @Valid PresignedUrlRequest request,
			@LoginUser UUID userId) {

		FileUploadResult result =
				temporaryExpenseCommandFacade.createPresignedUrl(
						accountBookId, request.fileName(), request.fileType(), userId);

		return ResponseEntity.ok(PresignedUrlResponse.from(result));
	}

	@Operation(
			summary = "임시지출 확정",
			description = "파일 메타 단위로 임시지출을 확정 변환합니다. tempExpenseIds가 있으면 부분 확정이 가능합니다.")
	@PostMapping("/temporary-expense-metas/{tempExpenseMetaId}/confirm")
	public ResponseEntity<BatchConvertResponse> confirm(
			@PathVariable Long accountBookId,
			@PathVariable Long tempExpenseMetaId,
			@RequestBody(required = false) BatchConvertRequest request,
			@LoginUser UUID userId) {

		BatchConversionResult result =
				temporaryExpenseCommandFacade.confirm(
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

		return ResponseEntity.ok(response);
	}

	/**
	 * 비동기 파싱 시작
	 */
	@Operation(summary = "임시지출 파싱 시작", description = "파일 목록 파싱 작업을 비동기로 시작하고 진행 조회용 taskId를 반환합니다.")
	@PostMapping("/temporary-expenses/parse")
	public ResponseEntity<BatchParseResponse> parse(
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

		return ResponseEntity.accepted().body(response);
	}

	/**
	 * 임시지출 메타 단위 일괄 수정
	 */
	@Operation(summary = "임시지출 일괄 수정", description = "메타(파일) 단위로 여러 임시지출을 한 번에 수정합니다.")
	@PatchMapping("/temporary-expense-metas/{tempExpenseMetaId}/temporary-expenses")
	public ResponseEntity<TemporaryExpenseMetaBulkUpdateResponse> updateTemporaryExpensesByMeta(
			@PathVariable Long accountBookId,
			@PathVariable Long tempExpenseMetaId,
			@RequestBody @Valid TemporaryExpenseMetaBulkUpdateRequest request,
			@LoginUser UUID userId) {
		TemporaryExpenseMetaBulkUpdateResponse response =
				temporaryExpenseCommandFacade.updateTemporaryExpensesByMeta(
						accountBookId, tempExpenseMetaId, request, userId);
		return ResponseEntity.ok(response);
	}

	@Operation(summary = "임시지출 메타 삭제", description = "메타와 연결된 임시지출/파일을 함께 삭제합니다.")
	@DeleteMapping("/temporary-expense-metas/{tempExpenseMetaId}")
	public ResponseEntity<Void> deleteMeta(
			@PathVariable Long accountBookId,
			@PathVariable Long tempExpenseMetaId,
			@LoginUser UUID userId) {
		temporaryExpenseCommandFacade.deleteMeta(accountBookId, tempExpenseMetaId, userId);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}
}
