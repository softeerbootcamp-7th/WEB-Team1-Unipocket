package com.genesis.unipocket.tempexpense.query.presentation;

import com.genesis.unipocket.auth.common.annotation.LoginUser;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.tempexpense.command.facade.port.AccountBookOwnershipValidator;
import com.genesis.unipocket.tempexpense.common.enums.TemporaryExpenseStatus;
import com.genesis.unipocket.tempexpense.common.infrastructure.ParsingProgressPublisher;
import com.genesis.unipocket.tempexpense.query.presentation.response.FileProcessingSummaryResponse;
import com.genesis.unipocket.tempexpense.query.presentation.response.ImageProcessingSummaryResponse;
import com.genesis.unipocket.tempexpense.query.presentation.response.TemporaryExpenseListResponse;
import com.genesis.unipocket.tempexpense.query.presentation.response.TemporaryExpenseResponse;
import com.genesis.unipocket.tempexpense.query.service.TemporaryExpenseQueryService;
import io.swagger.v3.oas.annotations.Operation;
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

	private final TemporaryExpenseQueryService temporaryExpenseQueryService;
	private final ParsingProgressPublisher progressPublisher;
	private final AccountBookOwnershipValidator accountBookOwnershipValidator;

	/**
	 * 가계부별 임시지출내역 목록 조회 (상태 필터 선택)
	 *
	 * @param accountBookId 가계부 ID
	 * @param status        필터링할 상태 (선택, 없으면 전체 조회)
	 * @return 임시지출내역 목록
	 */
	@Operation(summary = "임시지출 목록 조회", description = "가계부의 임시지출 목록을 상태 조건과 함께 조회합니다.")
	@GetMapping("/account-books/{accountBookId}/temporary-expenses")
	public ResponseEntity<TemporaryExpenseListResponse> getTemporaryExpenses(
			@PathVariable Long accountBookId,
			@RequestParam(required = false) TemporaryExpenseStatus status,
			@LoginUser UUID userId) {
		List<TemporaryExpenseResponse> items =
				temporaryExpenseQueryService.getTemporaryExpenses(accountBookId, status, userId);
		TemporaryExpenseListResponse response = new TemporaryExpenseListResponse(items);

		return ResponseEntity.ok(response);
	}

	/**
	 * 임시지출내역 단건 조회
	 */
	@Operation(summary = "임시지출 단건 조회", description = "가계부 내 임시지출 1건의 상세 정보를 조회합니다.")
	@GetMapping("/account-books/{accountBookId}/temporary-expenses/{tempExpenseId}")
	public ResponseEntity<TemporaryExpenseResponse> getTemporaryExpense(
			@PathVariable Long accountBookId,
			@PathVariable Long tempExpenseId,
			@LoginUser UUID userId) {
		TemporaryExpenseResponse response =
				temporaryExpenseQueryService.getTemporaryExpense(
						accountBookId, tempExpenseId, userId);
		return ResponseEntity.ok(response);
	}

	/**
	 * 파일(이미지) 단위 처리 현황 조회
	 */
	@Operation(summary = "파일 처리 현황 조회", description = "메타(업로드 파일) 단위의 임시지출 파싱 처리 현황을 조회합니다.")
	@GetMapping("/account-books/{accountBookId}/temporary-expense-metas/summary")
	public ResponseEntity<FileProcessingSummaryResponse> getFileProcessingSummary(
			@PathVariable Long accountBookId, @LoginUser UUID userId) {
		FileProcessingSummaryResponse response =
				temporaryExpenseQueryService.getFileProcessingSummary(accountBookId, userId);
		return ResponseEntity.ok(response);
	}

	/**
	 * 가계부 전체 이미지 처리 현황 요약 조회
	 */
	@Operation(summary = "이미지 처리 요약 조회", description = "가계부 전체 파일 처리 현황을 합산한 요약 정보를 조회합니다.")
	@GetMapping("/account-books/{accountBookId}/temporary-expense-metas/image-processing-summary")
	public ResponseEntity<ImageProcessingSummaryResponse> getImageProcessingSummary(
			@PathVariable Long accountBookId, @LoginUser UUID userId) {

		ImageProcessingSummaryResponse response =
				temporaryExpenseQueryService.getImageProcessingSummary(accountBookId, userId);

		return ResponseEntity.ok(response);
	}

	/**
	 * SSE 진행 상황 스트림
	 */
	@Operation(summary = "파싱 진행 상황 스트림", description = "비동기 파싱 작업의 진행/완료 이벤트를 SSE로 구독합니다.")
	@GetMapping(
			value = "/account-books/{accountBookId}/temporary-expenses/parse-status/{taskId}",
			produces = org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE)
	public SseEmitter streamParsingProgress(
			@PathVariable Long accountBookId, @PathVariable String taskId, @LoginUser UUID userId) {

		accountBookOwnershipValidator.validateOwnership(accountBookId, userId.toString());

		if (!progressPublisher.isTaskOwnedBy(taskId, accountBookId)) {
			throw new BusinessException(ErrorCode.TEMP_EXPENSE_PARSE_TASK_NOT_FOUND);
		}

		SseEmitter emitter = new SseEmitter(60000L); // 60초

		progressPublisher.addEmitter(taskId, emitter);

		emitter.onCompletion(() -> progressPublisher.removeEmitter(taskId));
		emitter.onTimeout(() -> progressPublisher.removeEmitter(taskId));
		emitter.onError((e) -> progressPublisher.removeEmitter(taskId));

		return emitter;
	}
}
