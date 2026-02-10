package com.genesis.unipocket.expense.query.presentation;

import com.genesis.unipocket.auth.common.annotation.LoginUser;
import com.genesis.unipocket.expense.command.persistence.entity.expense.TemporaryExpense;
import com.genesis.unipocket.expense.common.infrastructure.ParsingProgressPublisher;
import com.genesis.unipocket.expense.query.presentation.response.TemporaryExpenseListResponse;
import com.genesis.unipocket.expense.query.presentation.response.TemporaryExpenseResponse;
import com.genesis.unipocket.expense.query.service.TemporaryExpenseQueryService;
import com.genesis.unipocket.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

	private final TemporaryExpenseQueryService temporaryExpenseQueryService;
	private final ParsingProgressPublisher progressPublisher;

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
				temporaryExpenseQueryService.getTemporaryExpenses(accountBookId, status, userId);
		TemporaryExpenseListResponse response = new TemporaryExpenseListResponse(items);

		return ResponseEntity.ok(ApiResponse.success(response));
	}

	/**
	 * 임시지출내역 단건 조회
	 */
	@GetMapping("/api/temporary-expenses/{tempExpenseId}")
	public ResponseEntity<ApiResponse<TemporaryExpenseResponse>> getTemporaryExpense(
			@PathVariable Long tempExpenseId, @LoginUser UUID userId) {
		TemporaryExpenseResponse response =
				temporaryExpenseQueryService.getTemporaryExpense(tempExpenseId, userId);
		return ResponseEntity.ok(ApiResponse.success(response));
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
}
