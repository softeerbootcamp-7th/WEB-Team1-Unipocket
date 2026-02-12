package com.genesis.unipocket.expense.command.facade;

import com.genesis.unipocket.expense.command.application.ExpenseCommandService;
import com.genesis.unipocket.expense.command.application.command.ExpenseCreateCommand;
import com.genesis.unipocket.expense.command.application.command.ExpenseUpdateCommand;
import com.genesis.unipocket.expense.command.application.result.ExpenseResult;
import com.genesis.unipocket.expense.command.facade.port.AccountBookInfoFetchService;
import com.genesis.unipocket.expense.command.presentation.request.ExpenseManualCreateRequest;
import com.genesis.unipocket.expense.command.presentation.request.ExpenseUpdateRequest;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.tempexpense.command.facade.port.AccountBookOwnershipValidator;
import com.genesis.unipocket.tempexpense.command.facade.port.dto.AccountBookInfo;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <b>지출내역 도메인 내부용 Facade 클래스</b>
 * <p>지출내역 도메인에 대한 요청 처리
 *
 * @author codingbaraGo
 * @since 2026-02-03
 */
@Service
@AllArgsConstructor
public class ExpenseCommandFacade {

	private final ExpenseCommandService expenseService;
	private final AccountBookInfoFetchService accountBookInfoFetchService;
	private final AccountBookOwnershipValidator accountBookOwnershipValidator;

	@Transactional
	public ExpenseResult createExpenseManual(
			ExpenseManualCreateRequest request, Long accountBookId, UUID userId) {

		accountBookOwnershipValidator.validateOwnership(accountBookId, String.valueOf(userId));

		AccountBookInfo accountBookInfo =
				accountBookInfoFetchService.getAccountBook(accountBookId, userId.toString());

		CurrencyCode baseCurrencyCode = accountBookInfo.baseCountryCode().getCurrencyCode();

		ExpenseCreateCommand command =
				new ExpenseCreateCommand(
						accountBookId,
						request.merchantName(),
						request.category(),
						request.paymentMethod(),
						request.occurredAt(),
						request.localCurrencyAmount(),
						request.localCurrencyCode(),
						baseCurrencyCode,
						request.memo(),
						request.travelId());

		return expenseService.createExpenseManual(command);
	}

	@Transactional
	public ExpenseResult updateExpense(
			Long expenseId, Long accountBookId, UUID userId, ExpenseUpdateRequest request) {

		accountBookOwnershipValidator.validateOwnership(accountBookId, String.valueOf(userId));

		AccountBookInfo accountBookInfo =
				accountBookInfoFetchService.getAccountBook(accountBookId, userId.toString());

		CurrencyCode baseCurrencyCode = accountBookInfo.baseCountryCode().getCurrencyCode();

		ExpenseUpdateCommand command =
				new ExpenseUpdateCommand(
						expenseId,
						accountBookId,
						request.merchantName(),
						request.category(),
						request.paymentMethod(),
						request.memo(),
						request.occurredAt(),
						request.localCurrencyAmount(),
						request.localCurrencyCode(),
						request.travelId(),
						baseCurrencyCode);

		return expenseService.updateExpense(command);
	}

	@Transactional
	public void deleteExpense(Long expenseId, Long accountBookId, UUID userId) {
		accountBookOwnershipValidator.validateOwnership(accountBookId, userId.toString());
		expenseService.deleteExpense(expenseId, accountBookId);
	}
}
