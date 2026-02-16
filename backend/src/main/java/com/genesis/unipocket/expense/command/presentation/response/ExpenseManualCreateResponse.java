package com.genesis.unipocket.expense.command.presentation.response;

import com.genesis.unipocket.expense.command.application.result.ExpenseResult;
import com.genesis.unipocket.global.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import java.math.BigDecimal;
import java.time.Instant;

public record ExpenseManualCreateResponse(
		Long expenseId,
		Long accountBookId,
		String merchantName,
		Category category,
		PaymentMethodResponse paymentMethod,
		BigDecimal localCurrencyAmount,
		CurrencyCode localCurrencyCode,
		BigDecimal baseCurrencyAmount,
		CurrencyCode baseCurrencyCode,
		Instant occurredAt) {

	public static ExpenseManualCreateResponse from(ExpenseResult result) {
		return new ExpenseManualCreateResponse(
				result.expenseId(),
				result.accountBookId(),
				result.displayMerchantName() != null
						? result.displayMerchantName()
						: result.merchantName(),
				result.category(),
				PaymentMethodResponse.from(
						result.userCardId(),
						result.cardCompany(),
						result.cardLabel(),
						result.cardLastDigits()),
				result.localCurrencyAmount(),
				result.localCurrencyCode(),
				result.baseCurrencyAmount(),
				result.baseCurrencyCode(),
				result.occurredAt().toInstant());
	}
}
