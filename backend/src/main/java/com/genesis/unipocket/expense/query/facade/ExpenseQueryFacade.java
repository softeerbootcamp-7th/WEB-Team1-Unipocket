package com.genesis.unipocket.expense.query.facade;

import com.genesis.unipocket.expense.query.facade.port.AccountBookOwnershipValidator;
import com.genesis.unipocket.expense.query.facade.port.ExpenseMediaAccessService;
import com.genesis.unipocket.expense.query.persistence.response.ExpenseOneShotRow;
import com.genesis.unipocket.expense.query.presentation.request.ExpenseSearchFilter;
import com.genesis.unipocket.expense.query.presentation.response.ExpenseFileUrlResponse;
import com.genesis.unipocket.expense.query.presentation.response.ExpenseListResponse;
import com.genesis.unipocket.expense.query.presentation.response.ExpenseMerchantSearchResponse;
import com.genesis.unipocket.expense.query.presentation.response.ExpenseResponse;
import com.genesis.unipocket.expense.query.service.ExpenseQueryService;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExpenseQueryFacade {

	private final ExpenseQueryService expenseQueryService;
	private final AccountBookOwnershipValidator accountBookOwnershipValidator;
	private final ExpenseMediaAccessService expenseMediaAccessService;

	@Value("${app.media.presigned-get-expiration-seconds:600}")
	private int presignedGetExpirationSeconds;

	public ExpenseResponse getExpense(Long expenseId, Long accountBookId, UUID userId) {
		validateOwnership(accountBookId, userId);
		ExpenseOneShotRow row = expenseQueryService.getExpenseOneShot(expenseId, accountBookId);
		return ExpenseResponse.from(row);
	}

	public ExpenseListResponse getExpenses(
			Long accountBookId, UUID userId, ExpenseSearchFilter filter, Pageable pageable) {
		validateOwnership(accountBookId, userId);

		Page<ExpenseOneShotRow> rowPage =
				expenseQueryService.getExpensesOneShot(accountBookId, filter, pageable);

		List<ExpenseResponse> responses =
				rowPage.getContent().stream().map(ExpenseResponse::from).toList();

		return ExpenseListResponse.of(
				responses,
				rowPage.getTotalElements(),
				pageable.getPageNumber(),
				pageable.getPageSize());
	}

	public ExpenseMerchantSearchResponse searchMerchantNames(
			Long accountBookId, UUID userId, String query, Integer limit) {
		validateOwnership(accountBookId, userId);
		List<String> merchantNames =
				expenseQueryService.searchMerchantNames(accountBookId, query, limit);
		return new ExpenseMerchantSearchResponse(merchantNames);
	}

	public ExpenseFileUrlResponse getExpenseFileUrl(
			Long expenseId, Long accountBookId, UUID userId) {
		validateOwnership(accountBookId, userId);
		ExpenseOneShotRow row = expenseQueryService.getExpenseOneShot(expenseId, accountBookId);
		String presignedUrl =
				expenseMediaAccessService.issueGetPath(
						row.fileLink(), Duration.ofSeconds(presignedGetExpirationSeconds));
		return new ExpenseFileUrlResponse(presignedUrl, presignedGetExpirationSeconds);
	}

	private void validateOwnership(Long accountBookId, UUID userId) {
		accountBookOwnershipValidator.validateOwnership(accountBookId, userId.toString());
	}
}
