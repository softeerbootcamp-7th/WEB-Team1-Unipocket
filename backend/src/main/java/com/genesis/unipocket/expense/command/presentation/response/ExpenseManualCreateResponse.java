package com.genesis.unipocket.expense.command.presentation.response;

import com.genesis.unipocket.expense.application.result.ExpenseResult;
import com.genesis.unipocket.expense.presentation.support.AmountFormatters;
import com.genesis.unipocket.global.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import java.time.Instant;

public record ExpenseManualCreateResponse(
		Long expenseId,
		Long accountBookId,
		String merchantName,
		Category category,
		PaymentMethodResponse paymentMethod,
		String localCurrencyAmount,
		CurrencyCode localCurrencyCode,
		String baseCurrencyAmount,
		CurrencyCode baseCurrencyCode,
		Instant occurredAt) {

	public static ExpenseManualCreateResponse from(ExpenseResult result) {
		return new ExpenseManualCreateResponse(
				result.expenseId(),
				result.accountBookId(),
				result.displayMerchantName(),
				result.category(),
				PaymentMethodResponse.from(
						result.userCardId(),
						result.cardCompany(),
						result.cardLabel(),
						result.cardLastDigits()),
				AmountFormatters.toAmountString(result.localCurrencyAmount()),
				result.localCurrencyCode(),
				AmountFormatters.toAmountString(result.baseCurrencyAmount()),
				result.baseCurrencyCode(),
				result.occurredAt().toInstant());
	}
}
