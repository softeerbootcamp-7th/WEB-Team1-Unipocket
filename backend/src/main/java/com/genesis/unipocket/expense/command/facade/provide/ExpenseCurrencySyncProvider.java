package com.genesis.unipocket.expense.command.facade.provide;

import com.genesis.unipocket.accountbook.command.facade.port.ExpenseCurrencySyncService;
import com.genesis.unipocket.expense.command.application.ExpenseCommandService;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExpenseCurrencySyncProvider implements ExpenseCurrencySyncService {

	private final ExpenseCommandService expenseCommandService;

	@Override
	public void updateBaseCurrency(Long accountBookId, CurrencyCode currencyCode) {
		expenseCommandService.updateBaseCurrency(accountBookId, currencyCode);
	}
}
