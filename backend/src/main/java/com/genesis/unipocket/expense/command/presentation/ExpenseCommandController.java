package com.genesis.unipocket.expense.command.presentation;

import com.genesis.unipocket.auth.common.annotation.LoginUser;
import com.genesis.unipocket.expense.command.application.result.ExpenseResult;
import com.genesis.unipocket.expense.command.facade.ExpenseCommandFacade;
import com.genesis.unipocket.expense.command.presentation.request.ExpenseManualCreateRequest;
import com.genesis.unipocket.expense.command.presentation.request.ExpenseUpdateRequest;
import com.genesis.unipocket.expense.command.presentation.response.ExpenseManualCreateResponse;
import com.genesis.unipocket.expense.command.presentation.response.ExpenseUpdateResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
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
@Tag(name = "지출내역 기능")
@RestController
@AllArgsConstructor
public class ExpenseCommandController {

	private final ExpenseCommandFacade expenseFacade;

	@PostMapping("/api/account-books/{accountBookId}/expenses/manual")
	public ResponseEntity<ExpenseManualCreateResponse> createExpenseManual(
			@LoginUser UUID userId,
			@PathVariable Long accountBookId,
			@RequestBody @Valid ExpenseManualCreateRequest request) {

		ExpenseResult result = expenseFacade.createExpenseManual(request, accountBookId, userId);
		ExpenseManualCreateResponse response = ExpenseManualCreateResponse.from(result);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@PutMapping("/api/account-books/{accountBookId}/expenses/{expenseId}")
	public ResponseEntity<ExpenseUpdateResponse> updateExpense(
			@LoginUser UUID userId,
			@PathVariable Long accountBookId,
			@PathVariable Long expenseId,
			@RequestBody @Valid ExpenseUpdateRequest request) {

		ExpenseResult result =
				expenseFacade.updateExpense(expenseId, accountBookId, userId, request);
		ExpenseUpdateResponse response = ExpenseUpdateResponse.from(result);
		return ResponseEntity.ok(response);
	}

	@DeleteMapping("/api/account-books/{accountBookId}/expenses/{expenseId}")
	public ResponseEntity<Void> deleteExpense(
			@LoginUser UUID userId,
			@PathVariable Long accountBookId,
			@PathVariable Long expenseId) {

		expenseFacade.deleteExpense(expenseId, accountBookId, userId);
		return ResponseEntity.noContent().build();
	}
}
