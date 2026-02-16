package com.genesis.unipocket.tempexpense.query.presentation;

import com.genesis.unipocket.auth.common.annotation.LoginUser;
import com.genesis.unipocket.tempexpense.query.facade.TemporaryExpenseQueryFacade;
import com.genesis.unipocket.tempexpense.query.presentation.response.TemporaryExpenseMetaFilesResponse;
import com.genesis.unipocket.tempexpense.query.presentation.response.TemporaryExpenseMetaListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * <b>임시지출내역 조회 컨트롤러</b>
 *
 * @author 김동균
 * @since 2026-02-10
 */
@Tag(name = "임시지출내역 기능")
@RestController
@RequestMapping
@AllArgsConstructor
public class TemporaryExpenseQueryController {

	private final TemporaryExpenseQueryFacade temporaryExpenseQueryFacade;

	/**
	 * 메타 목록 조회
	 */
	@Operation(summary = "임시지출 메타 목록 조회", description = "가계부에 속한 임시지출 메타 목록을 조회합니다.")
	@GetMapping("/account-books/{accountBookId}/temporary-expense-metas")
	public ResponseEntity<TemporaryExpenseMetaListResponse> getTemporaryExpenseMetas(
			@PathVariable Long accountBookId, @LoginUser UUID userId) {
		TemporaryExpenseMetaListResponse response =
				temporaryExpenseQueryFacade.getTemporaryExpenseMetas(accountBookId, userId);
		return ResponseEntity.ok(response);
	}

	/**
	 * 메타 내부 파일별 임시지출 조회
	 */
	@Operation(summary = "메타 내부 파일별 임시지출 조회", description = "메타 1건에 속한 파일별 임시지출 목록을 조회합니다.")
	@GetMapping("/account-books/{accountBookId}/temporary-expense-metas/{tempExpenseMetaId}/files")
	public ResponseEntity<TemporaryExpenseMetaFilesResponse> getTemporaryExpenseMetaFiles(
			@PathVariable Long accountBookId,
			@PathVariable Long tempExpenseMetaId,
			@LoginUser UUID userId) {
		TemporaryExpenseMetaFilesResponse response =
				temporaryExpenseQueryFacade.getTemporaryExpenseMetaFiles(
						accountBookId, tempExpenseMetaId, userId);
		return ResponseEntity.ok(response);
	}

	/**
	 * 메타 내부 파일 단건 조회
	 */
	@Operation(summary = "메타 내부 파일 단건 임시지출 조회", description = "메타 1건에 속한 파일 1건의 임시지출 목록을 조회합니다.")
	@GetMapping(
			"/account-books/{accountBookId}/temporary-expense-metas/{tempExpenseMetaId}/files/{fileId}")
	public ResponseEntity<TemporaryExpenseMetaFilesResponse.FileExpenses>
			getTemporaryExpenseMetaFile(
					@PathVariable Long accountBookId,
					@PathVariable Long tempExpenseMetaId,
					@PathVariable Long fileId,
					@LoginUser UUID userId) {
		TemporaryExpenseMetaFilesResponse.FileExpenses response =
				temporaryExpenseQueryFacade.getTemporaryExpenseMetaFile(
						accountBookId, tempExpenseMetaId, fileId, userId);
		return ResponseEntity.ok(response);
	}

	/**
	 * SSE 진행 상황 스트림
	 */
	@Operation(summary = "파싱 진행 상황 스트림", description = "비동기 파싱 작업의 진행/완료 이벤트를 SSE로 구독합니다.")
	@GetMapping(
			value = "/account-books/{accountBookId}/temporary-expenses/parse-status/{taskId}",
			produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public SseEmitter streamParsingProgress(
			@PathVariable Long accountBookId, @PathVariable String taskId, @LoginUser UUID userId) {
		return temporaryExpenseQueryFacade.streamParsingProgress(accountBookId, taskId, userId);
	}
}
