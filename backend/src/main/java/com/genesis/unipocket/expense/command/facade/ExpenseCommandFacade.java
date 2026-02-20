package com.genesis.unipocket.expense.command.facade;

import com.genesis.unipocket.expense.command.application.result.ExpenseResult;
import com.genesis.unipocket.expense.command.application.ExpenseCommandContextService;
import com.genesis.unipocket.expense.command.application.ExpenseCommandService;
import com.genesis.unipocket.expense.command.application.command.ExpenseCreateCommand;
import com.genesis.unipocket.expense.command.application.command.ExpenseUpdateCommand;
import com.genesis.unipocket.expense.command.facade.port.AccountBookInfoFetchService;
import com.genesis.unipocket.expense.command.facade.port.AccountBookOwnershipValidator;
import com.genesis.unipocket.expense.command.facade.port.dto.AccountBookInfo;
import com.genesis.unipocket.expense.command.presentation.request.ExpenseBulkUpdateItemRequest;
import com.genesis.unipocket.expense.command.presentation.request.ExpenseBulkUpdateRequest;
import com.genesis.unipocket.expense.command.presentation.request.ExpenseManualCreateRequest;
import com.genesis.unipocket.expense.command.presentation.request.ExpenseUpdateRequest;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class ExpenseCommandFacade {

	private final ExpenseCommandService expenseService;
	private final ExpenseCommandContextService expenseCommandContextService;
	private final AccountBookInfoFetchService accountBookInfoFetchService;
	private final AccountBookOwnershipValidator accountBookOwnershipValidator;

	@Transactional
	public ExpenseResult createExpenseManual(
			ExpenseManualCreateRequest request, Long accountBookId, UUID userId) {
		accountBookOwnershipValidator.validateOwnership(accountBookId, String.valueOf(userId));

		AccountBookInfo accountBookInfo =
				accountBookInfoFetchService.getAccountBook(accountBookId, userId.toString());
		CurrencyCode localCurrencyCode =
				expenseCommandContextService.resolveLocalCurrencyCode(
						request.localCurrencyCode(),
						accountBookInfo.localCountryCode().getCurrencyCode());
		CurrencyCode baseCurrencyCode = accountBookInfo.baseCountryCode().getCurrencyCode();
		OffsetDateTime occurredAt = request.occurredAt().atOffset(ZoneOffset.UTC);

		ExpenseCreateCommand command =
				new ExpenseCreateCommand(
						accountBookId,
						request.merchantName(),
						request.category(),
						request.userCardId(),
						occurredAt,
						request.localCurrencyAmount(),
						request.baseCurrencyAmount(),
						localCurrencyCode,
						baseCurrencyCode,
						request.memo(),
						request.travelId());

		ExpenseResult result = expenseService.createExpenseManual(command);
		return expenseCommandContextService.enrichWithCardInfo(result);
	}

	@Transactional
	public ExpenseResult updateExpense(
			Long expenseId, Long accountBookId, UUID userId, ExpenseUpdateRequest request) {
		accountBookOwnershipValidator.validateOwnership(accountBookId, String.valueOf(userId));

		AccountBookInfo accountBookInfo =
				accountBookInfoFetchService.getAccountBook(accountBookId, userId.toString());
		CurrencyCode localCurrencyCode =
				expenseCommandContextService.resolveLocalCurrencyCode(
						request.localCurrencyCode(),
						accountBookInfo.localCountryCode().getCurrencyCode());
		CurrencyCode baseCurrencyCode = accountBookInfo.baseCountryCode().getCurrencyCode();
		OffsetDateTime occurredAt = request.occurredAt().atOffset(ZoneOffset.UTC);

		ExpenseUpdateCommand command =
				new ExpenseUpdateCommand(
						expenseId,
						accountBookId,
						request.merchantName(),
						request.category(),
						request.userCardId(),
						request.memo(),
						occurredAt,
						request.localCurrencyAmount(),
						request.baseCurrencyAmount(),
						localCurrencyCode,
						request.travelId(),
						baseCurrencyCode);

		ExpenseResult result = expenseService.updateExpense(command);
		return expenseCommandContextService.enrichWithCardInfo(result);
	}

	@Transactional
	public List<ExpenseResult> updateExpensesBulk(
			Long accountBookId, UUID userId, ExpenseBulkUpdateRequest request) {
		accountBookOwnershipValidator.validateOwnership(accountBookId, String.valueOf(userId));

		AccountBookInfo accountBookInfo =
				accountBookInfoFetchService.getAccountBook(accountBookId, userId.toString());
		CurrencyCode accountBookLocalCurrencyCode = accountBookInfo.localCountryCode().getCurrencyCode();
		CurrencyCode baseCurrencyCode = accountBookInfo.baseCountryCode().getCurrencyCode();

		return request.items().stream()
				.map(
						item ->
								updateExpenseItem(
										accountBookId,
										item,
										accountBookLocalCurrencyCode,
										baseCurrencyCode))
				.toList();
	}

	private ExpenseResult updateExpenseItem(
			Long accountBookId,
			ExpenseBulkUpdateItemRequest item,
			CurrencyCode accountBookLocalCurrencyCode,
			CurrencyCode baseCurrencyCode) {
		CurrencyCode localCurrencyCode =
				expenseCommandContextService.resolveLocalCurrencyCode(
						item.localCurrencyCode(), accountBookLocalCurrencyCode);
		OffsetDateTime occurredAt = item.occurredAt().atOffset(ZoneOffset.UTC);

		ExpenseUpdateCommand command =
				new ExpenseUpdateCommand(
						item.expenseId(),
						accountBookId,
						item.merchantName(),
						item.category(),
						item.userCardId(),
						item.memo(),
						occurredAt,
						item.localCurrencyAmount(),
						item.baseCurrencyAmount(),
						localCurrencyCode,
						item.travelId(),
						baseCurrencyCode);

		ExpenseResult result = expenseService.updateExpense(command);
		return expenseCommandContextService.enrichWithCardInfo(result);
	}

	@Transactional
	public void deleteExpense(Long expenseId, Long accountBookId, UUID userId) {
		accountBookOwnershipValidator.validateOwnership(accountBookId, userId.toString());
		expenseService.deleteExpense(expenseId, accountBookId);
	}
}
