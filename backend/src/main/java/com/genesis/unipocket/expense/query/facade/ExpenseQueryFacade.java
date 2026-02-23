package com.genesis.unipocket.expense.query.facade;

import com.genesis.unipocket.expense.command.facade.port.AccountBookFetchService;
import com.genesis.unipocket.expense.command.facade.port.dto.AccountBookInfo;
import com.genesis.unipocket.expense.common.validation.ExpenseOwnershipValidator;
import com.genesis.unipocket.expense.query.facade.port.ExpenseMediaAccessService;
import com.genesis.unipocket.expense.query.persistence.response.ExpenseOneShotRow;
import com.genesis.unipocket.expense.query.presentation.request.ExpenseSearchFilter;
import com.genesis.unipocket.expense.query.presentation.response.ExpenseFileUrlResponse;
import com.genesis.unipocket.expense.query.presentation.response.ExpenseListResponse;
import com.genesis.unipocket.expense.query.presentation.response.ExpenseMerchantSearchResponse;
import com.genesis.unipocket.expense.query.presentation.response.ExpenseResponse;
import com.genesis.unipocket.expense.query.service.ExpenseQueryService;
import com.genesis.unipocket.global.util.CountryCodeTimezoneMapper;
import java.time.Duration;
import java.time.ZoneId;
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
	private final ExpenseOwnershipValidator expenseOwnershipValidator;
	private final ExpenseMediaAccessService expenseMediaAccessService;
	private final AccountBookFetchService accountBookFetchService;

	@Value("${app.media.presigned-get-expiration-seconds:600}")
	private int presignedGetExpirationSeconds;

	public ExpenseResponse getExpense(Long expenseId, Long accountBookId, UUID userId) {
		AccountBookInfo accountBook =
				accountBookFetchService.getAccountBook(accountBookId, userId.toString());
		ZoneId zoneId = CountryCodeTimezoneMapper.getZoneId(accountBook.localCountryCode());
		ExpenseOneShotRow row = expenseQueryService.getExpenseOneShot(expenseId, accountBookId);
		return ExpenseResponse.from(row, zoneId);
	}

	public ExpenseListResponse getExpenses(
			Long accountBookId, UUID userId, ExpenseSearchFilter filter, Pageable pageable) {
		AccountBookInfo accountBook =
				accountBookFetchService.getAccountBook(accountBookId, userId.toString());
		ZoneId zoneId = CountryCodeTimezoneMapper.getZoneId(accountBook.localCountryCode());

		Page<ExpenseOneShotRow> rowPage =
				expenseQueryService.getExpensesOneShot(accountBookId, filter, pageable);

		List<ExpenseResponse> responses =
				rowPage.getContent().stream()
						.map(row -> ExpenseResponse.from(row, zoneId))
						.toList();

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
		expenseOwnershipValidator.validateOwnership(accountBookId, userId.toString());
	}
}
