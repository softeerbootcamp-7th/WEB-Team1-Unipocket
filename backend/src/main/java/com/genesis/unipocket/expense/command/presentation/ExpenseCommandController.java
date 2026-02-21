package com.genesis.unipocket.expense.command.presentation;

import com.genesis.unipocket.auth.common.annotation.LoginUser;
import com.genesis.unipocket.expense.command.application.result.ExpenseResult;
import com.genesis.unipocket.expense.command.facade.ExpenseCommandFacade;
import com.genesis.unipocket.expense.command.presentation.request.ExpenseBulkUpdateRequest;
import com.genesis.unipocket.expense.command.presentation.request.ExpenseManualCreateRequest;
import com.genesis.unipocket.expense.command.presentation.request.ExpenseUpdateRequest;
import com.genesis.unipocket.expense.command.presentation.response.ExpenseBulkUpdateResponse;
import com.genesis.unipocket.expense.command.presentation.response.ExpenseManualCreateResponse;
import com.genesis.unipocket.expense.command.presentation.response.ExpenseUpdateResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "지출내역 기능")
@RestController
@AllArgsConstructor
public class ExpenseCommandController {

	private final ExpenseCommandFacade expenseFacade;

	@Operation(
			summary = "지출내역 수기작성 생성 API",
			description = "지출내역을 accountBookId 하위에 수기 작성하여 하나 생성합니다.")
	@PostMapping("/account-books/{accountBookId}/expenses/manual")
	public ResponseEntity<ExpenseManualCreateResponse> createExpenseManual(
			@LoginUser UUID userId,
			@PathVariable Long accountBookId,
			@RequestBody @Valid ExpenseManualCreateRequest request) {

		ExpenseResult result = expenseFacade.createExpenseManual(request, accountBookId, userId);
		ExpenseManualCreateResponse response = ExpenseManualCreateResponse.from(result);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@Operation(
			summary = "지출내역 수정 API",
			description = "지출내역 하나에 대한 전체 데이터 수정을 합니다. 전체 데이터가 덮어씌워지며, 기존의 데이터는 남지 않습니다.")
	@PutMapping("/account-books/{accountBookId}/expenses/{expenseId}")
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

	@Operation(
			summary = "지출내역 일괄 수정 API",
			description = "여러 지출내역 ID를 한 번에 수정합니다. 요청된 항목은 모두 전체 데이터로 덮어씌워집니다.")
	@PutMapping("/account-books/{accountBookId}/expenses/bulk")
	public ResponseEntity<ExpenseBulkUpdateResponse> updateExpensesBulk(
			@LoginUser UUID userId,
			@PathVariable Long accountBookId,
			@RequestBody @Valid ExpenseBulkUpdateRequest request) {

		var results = expenseFacade.updateExpensesBulk(accountBookId, userId, request);
		ExpenseBulkUpdateResponse response = ExpenseBulkUpdateResponse.from(results);
		return ResponseEntity.ok(response);
	}

	@Operation(summary = "지출내역 삭제 API", description = "accountBookId 하위의 지출내역 하나를 삭제합니다.")
	@DeleteMapping("/account-books/{accountBookId}/expenses/{expenseId}")
	public ResponseEntity<Void> deleteExpense(
			@LoginUser UUID userId,
			@PathVariable Long accountBookId,
			@PathVariable Long expenseId) {

		expenseFacade.deleteExpense(expenseId, accountBookId, userId);
		return ResponseEntity.noContent().build();
	}
}
