package com.genesis.unipocket.expense.presentation;

import com.genesis.unipocket.expense.facade.ExpenseOrchestrator;
import com.genesis.unipocket.expense.presentation.dto.ExpenseManualCreateRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * <b>지출내역 도메인 컨트롤러</b>
 *
 * @author codingbaraGo
 * @since 2026-02-03
 */
@RestController
@AllArgsConstructor
public class ExpenseController {

	private final ExpenseOrchestrator orchestrator;
	private final Long USER_ID_TEMP = 1L;

	@PostMapping("/api/account-books/{accountBookId}/expenses/manual")
	public ResponseEntity createExpenseManual(
			@PathVariable Long accountBookId,
			@RequestBody @Valid ExpenseManualCreateRequest request) {
		Long userId = USER_ID_TEMP;
		// TODO: User 객체, 혹은 User ID를 받아올 수 있는 필터 구현
		orchestrator.createExpense(request, accountBookId, userId);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}
}
