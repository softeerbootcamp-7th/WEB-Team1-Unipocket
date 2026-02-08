package com.genesis.unipocket.expense.controller;

import com.genesis.unipocket.auth.annotation.LoginUser;
import com.genesis.unipocket.expense.dto.common.ExpenseDto;
import com.genesis.unipocket.expense.dto.request.ExpenseManualCreateRequest;
import com.genesis.unipocket.expense.dto.request.ExpenseSearchFilter;
import com.genesis.unipocket.expense.dto.request.ExpenseUpdateRequest;
import com.genesis.unipocket.expense.dto.response.ExpenseListResponse;
import com.genesis.unipocket.expense.dto.response.ExpenseManualCreateResponse;
import com.genesis.unipocket.expense.dto.response.ExpenseResponse;
import com.genesis.unipocket.expense.facade.ExpenseFacade;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
public class ExpenseController {

	private final ExpenseFacade expenseFacade;

	@PostMapping("/api/account-books/{accountBookId}/expenses/manual")
	public ResponseEntity<ExpenseManualCreateResponse> createExpenseManual(
			@LoginUser UUID userId,
			@PathVariable Long accountBookId,
			@RequestBody @Valid ExpenseManualCreateRequest request) {

		ExpenseDto dto = expenseFacade.createExpenseManual(request, accountBookId, userId);
		ExpenseManualCreateResponse response = ExpenseManualCreateResponse.from(dto);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping("/api/account-books/{accountBookId}/expenses/{expenseId}")
	public ResponseEntity<ExpenseResponse> getExpense(
			@LoginUser UUID userId,
			@PathVariable Long accountBookId,
			@PathVariable Long expenseId) {

		ExpenseDto dto = expenseFacade.getExpense(expenseId, accountBookId, userId);
		ExpenseResponse response = ExpenseResponse.from(dto);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/api/account-books/{accountBookId}/expenses")
	public ResponseEntity<ExpenseListResponse> getExpenses(
			@LoginUser UUID userId,
			@PathVariable Long accountBookId,
			ExpenseSearchFilter filter,
			@PageableDefault(sort = "occurredAt", direction = Sort.Direction.DESC)
					Pageable pageable) {

		Page<ExpenseDto> dtoPage =
				expenseFacade.getExpenses(accountBookId, userId, filter, pageable);

		List<ExpenseResponse> responses =
				dtoPage.getContent().stream().map(ExpenseResponse::from).toList();

		ExpenseListResponse response =
				ExpenseListResponse.of(
						responses,
						dtoPage.getTotalElements(),
						pageable.getPageNumber(),
						pageable.getPageSize());

		return ResponseEntity.ok(response);
	}

	@PutMapping("/api/account-books/{accountBookId}/expenses/{expenseId}")
	public ResponseEntity<ExpenseResponse> updateExpense(
			@LoginUser UUID userId,
			@PathVariable Long accountBookId,
			@PathVariable Long expenseId,
			@RequestBody @Valid ExpenseUpdateRequest request) {

		ExpenseDto dto = expenseFacade.updateExpense(expenseId, accountBookId, userId, request);
		ExpenseResponse response = ExpenseResponse.from(dto);
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

	private Sort.Order[] parseSortParams(String[] sort) {
		return Arrays.stream(sort)
				.map(
						s -> {
							String[] parts = s.split(",");
							String field = parts[0];
							Sort.Direction direction =
									parts.length > 1 && parts[1].equalsIgnoreCase("asc")
											? Sort.Direction.ASC
											: Sort.Direction.DESC;
							return new Sort.Order(direction, field);
						})
				.toArray(Sort.Order[]::new);
	}
}
