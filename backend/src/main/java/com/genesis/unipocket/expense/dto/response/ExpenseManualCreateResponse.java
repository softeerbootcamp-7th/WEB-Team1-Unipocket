package com.genesis.unipocket.expense.dto.response;

import com.genesis.unipocket.expense.common.enums.Category;
import com.genesis.unipocket.expense.dto.common.ExpenseDto;
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

	public static ExpenseManualCreateResponse from(ExpenseDto dto) {
		return new ExpenseManualCreateResponse(
				dto.id(),
				dto.accountBookId(),
				dto.displayMerchantName() != null ? dto.displayMerchantName() : dto.merchantName(),
				dto.category(),
				dto.localCurrencyAmount(),
				dto.localCurrencyCode(),
				dto.baseCurrencyAmount(),
				dto.baseCurrencyCode(),
				dto.occurredAt());
	}
}
