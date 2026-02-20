package com.genesis.unipocket.expense.command.presentation.response;

import com.genesis.unipocket.expense.command.application.result.ExpenseResult;
import com.genesis.unipocket.expense.common.util.AmountFormatters;
import com.genesis.unipocket.global.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.global.common.enums.ExpenseSource;
import java.time.Instant;

public record ExpenseUpdateResponse(
		Long expenseId,
		Long accountBookId,
		Long travelId,
		String merchantName,
		String displayMerchantName,
		Category category,
		PaymentMethodResponse paymentMethod,
		Instant occurredAt,
		String localCurrencyAmount,
		CurrencyCode localCurrencyCode,
		String baseCurrencyAmount,
		CurrencyCode baseCurrencyCode,
		String memo,
		ExpenseSource source,
		String approvalNumber,
		String cardNumber,
		String fileLink) {

	public static ExpenseUpdateResponse from(ExpenseResult result) {
		return new ExpenseUpdateResponse(
				result.expenseId(),
				result.accountBookId(),
				result.travelId(),
				result.displayMerchantName(),
				result.displayMerchantName(),
				result.category(),
				PaymentMethodResponse.from(
						result.userCardId(),
						result.cardCompany(),
						result.cardLabel(),
						result.cardLastDigits()),
				result.occurredAt().toInstant(),
				AmountFormatters.toAmountString(result.localCurrencyAmount()),
				result.localCurrencyCode(),
				AmountFormatters.toAmountString(result.baseCurrencyAmount()),
				result.baseCurrencyCode(),
				result.memo(),
				result.expenseSource(),
				result.approvalNumber(),
				result.cardNumber(),
				result.fileLink());
	}
}
