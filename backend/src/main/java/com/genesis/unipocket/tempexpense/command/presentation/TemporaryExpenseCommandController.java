package com.genesis.unipocket.tempexpense.command.presentation;

import com.genesis.unipocket.auth.common.annotation.LoginUser;
import com.genesis.unipocket.tempexpense.command.application.result.ConfirmStartResult;
import com.genesis.unipocket.tempexpense.command.application.result.FileUploadResult;
import com.genesis.unipocket.tempexpense.command.application.result.ParseStartResult;
import com.genesis.unipocket.tempexpense.command.facade.TemporaryExpenseCommandFacade;
import com.genesis.unipocket.tempexpense.command.presentation.request.BatchParseRequest;
import com.genesis.unipocket.tempexpense.command.presentation.request.PresignedUrlRequest;
import com.genesis.unipocket.tempexpense.command.presentation.request.TemporaryExpenseMetaBulkUpdateRequest;
import com.genesis.unipocket.tempexpense.command.presentation.response.BatchConvertStartResponse;
import com.genesis.unipocket.tempexpense.command.presentation.response.BatchParseResponse;
import com.genesis.unipocket.tempexpense.command.presentation.response.PresignedUrlResponse;
import com.genesis.unipocket.tempexpense.command.presentation.response.TemporaryExpenseMetaBulkUpdateResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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

@Tag(name = "임시지출내역 기능")
@RestController
@RequiredArgsConstructor
@RequestMapping("/account-books/{accountBookId}")
public class TemporaryExpenseCommandController {

	private final TemporaryExpenseCommandFacade temporaryExpenseCommandFacade;

	@Operation(
			summary = "임시지출 업로드 URL 발급",
			description =
					"임시지출 파일 업로드를 위한 presigned URL을 발급합니다. request.tempExpenseMetaId가 없으면"
							+ " 새 메타를 생성하고, 값이 있으면 해당 메타에 파일을 추가합니다. 즉,"
							+ " 여러 이미지는 같은 tempExpenseMetaId를 재사용해 1(meta):N(file)로 업로드해야 합니다.")
	@PostMapping("/temporary-expenses/uploads/presigned-url")
	public ResponseEntity<PresignedUrlResponse> createPresignedUrl(
			@PathVariable Long accountBookId,
			@RequestBody @Valid PresignedUrlRequest request,
			@LoginUser UUID userId) {

		FileUploadResult result =
				temporaryExpenseCommandFacade.createPresignedUrl(
						accountBookId,
						request.fileName(),
						request.mimeType(),
						request.uploadType(),
						request.tempExpenseMetaId(),
						userId);

		return ResponseEntity.ok(PresignedUrlResponse.from(result));
	}

	@Operation(
			summary = "임시지출 확정",
			description = "메타 단위로 임시지출 전체를 비동기로 확정 변환하고 진행 조회용 taskId를 반환합니다.")
	@PostMapping("/temporary-expense-metas/{tempExpenseMetaId}/confirm")
	public ResponseEntity<BatchConvertStartResponse> confirm(
			@PathVariable Long accountBookId,
			@PathVariable Long tempExpenseMetaId,
			@LoginUser UUID userId) {

		ConfirmStartResult result =
				temporaryExpenseCommandFacade.confirm(accountBookId, tempExpenseMetaId, userId);

		BatchConvertStartResponse response =
				new BatchConvertStartResponse(
						result.taskId(), result.totalExpenses(), buildParseStatusUrl(accountBookId, result.taskId()));
		return ResponseEntity.accepted().body(response);
	}

	@Operation(
			summary = "임시지출 파싱 시작",
			description =
					"메타 단위로 파싱 작업을 비동기로 시작하고 진행 조회용 taskId를 반환합니다. 요청에 s3Keys가 없으면 메타의 전체 파일을"
							+ " 파싱합니다.")
	@PostMapping("/temporary-expenses/parse")
	public ResponseEntity<BatchParseResponse> parse(
			@PathVariable Long accountBookId,
			@RequestBody @Valid BatchParseRequest request,
			@LoginUser UUID userId) {
		ParseStartResult result =
				temporaryExpenseCommandFacade.startParseAsync(
						accountBookId, request.tempExpenseMetaId(), request.s3Keys(), userId);

		BatchParseResponse response =
				new BatchParseResponse(
						result.taskId(), result.totalFiles(), buildParseStatusUrl(accountBookId, result.taskId()));

		return ResponseEntity.accepted().body(response);
	}

	@Operation(summary = "임시지출 일괄 수정", description = "메타(파일) 단위로 여러 임시지출을 한 번에 수정합니다.")
	@PatchMapping("/temporary-expense-metas/{tempExpenseMetaId}/files/{fileId}/temporary-expenses")
	public ResponseEntity<TemporaryExpenseMetaBulkUpdateResponse> updateTemporaryExpensesByFile(
			@PathVariable Long accountBookId,
			@PathVariable Long tempExpenseMetaId,
			@PathVariable Long fileId,
			@RequestBody @Valid TemporaryExpenseMetaBulkUpdateRequest request,
			@LoginUser UUID userId) {
		TemporaryExpenseMetaBulkUpdateResponse response =
				temporaryExpenseCommandFacade.updateTemporaryExpensesByFile(
						accountBookId, tempExpenseMetaId, fileId, request, userId);
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

	@Operation(summary = "임시지출 단건 삭제", description = "메타/파일 범위에 속한 임시지출 단건을 삭제합니다.")
	@DeleteMapping(
			"/temporary-expense-metas/{tempExpenseMetaId}/files/{fileId}/temporary-expenses/{tempExpenseId}")
	public ResponseEntity<Void> deleteTemporaryExpenseByFile(
			@PathVariable Long accountBookId,
			@PathVariable Long tempExpenseMetaId,
			@PathVariable Long fileId,
			@PathVariable Long tempExpenseId,
			@LoginUser UUID userId) {
		temporaryExpenseCommandFacade.deleteTemporaryExpenseByFile(
				accountBookId, tempExpenseMetaId, fileId, tempExpenseId, userId);
		return ResponseEntity.noContent().build();
	}

	private String buildParseStatusUrl(Long accountBookId, String taskId) {
		return "/account-books/" + accountBookId + "/temporary-expenses/parse-status/" + taskId;
	}
}
