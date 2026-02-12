package com.genesis.unipocket.expense.expense.query.presentation;

import com.genesis.unipocket.auth.common.annotation.LoginUser;
import com.genesis.unipocket.expense.common.dto.ExpenseDto;
import com.genesis.unipocket.expense.expense.query.presentation.request.ExpenseSearchFilter;
import com.genesis.unipocket.expense.expense.query.presentation.response.ExpenseListResponse;
import com.genesis.unipocket.expense.expense.query.presentation.response.ExpenseResponse;
import com.genesis.unipocket.expense.expense.query.service.ExpenseQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <b>지출내역 조회 컨트롤러</b>
 *
 * @author codingbaraGo
 * @since 2026-02-03
 */
@Tag(name = "지출내역 기능")
@RestController
@RequestMapping
@AllArgsConstructor
public class ExpenseQueryController {

	private final ExpenseQueryService expenseQueryService;

	@Operation(
			summary = "지출내역 상세 조회 API",
			description = "하나의 지출내역에 대한 조회를 합니다.")
	@GetMapping("/account-books/{accountBookId}/expenses/{expenseId}")
	public ResponseEntity<ExpenseResponse> getExpense(
			@LoginUser UUID userId,
			@PathVariable Long accountBookId,
			@PathVariable Long expenseId) {

		ExpenseDto dto = expenseQueryService.getExpense(expenseId, accountBookId, userId);
		ExpenseResponse response = ExpenseResponse.from(dto);
		return ResponseEntity.ok(response);
	}

	@Operation(
			summary = "지출내역 조회 및 필터링 API",
			description = "지출내역 리스트를 조회합니다. 필터링 및 정렬을 할 수도 있습니다.")
	@GetMapping("/account-books/{accountBookId}/expenses")
	public ResponseEntity<ExpenseListResponse> getExpenses(
			@LoginUser UUID userId,
			@PathVariable Long accountBookId,
			ExpenseSearchFilter filter,
			@PageableDefault(sort = "occurredAt", direction = Sort.Direction.DESC)
					Pageable pageable) {

		Page<ExpenseDto> dtoPage =
				expenseQueryService.getExpenses(accountBookId, userId, filter, pageable);

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
}
