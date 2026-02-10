package com.genesis.unipocket.expense.command.presentation.response;

import com.genesis.unipocket.expense.command.application.result.ExpenseResult;
import com.genesis.unipocket.expense.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ExpenseManualCreateResponse(
		Long expenseId,
		Long accountBookId,
		String merchantName,
		Category category,
		BigDecimal localCurrencyAmount,
		CurrencyCode localCurrencyCode,
		BigDecimal baseCurrencyAmount,
		CurrencyCode baseCurrencyCode,
		LocalDateTime occurredAt) {

	public static ExpenseManualCreateResponse from(ExpenseResult result) {
		return new ExpenseManualCreateResponse(
				result.expenseId(),
				result.accountBookId(),
				result.displayMerchantName() != null
						? result.displayMerchantName()
						: result.merchantName(),
				result.category(),
				result.localCurrencyAmount(),
				result.localCurrencyCode(),
				result.baseCurrencyAmount(),
				result.baseCurrencyCode(),
				result.occurredAt());
	}
}
