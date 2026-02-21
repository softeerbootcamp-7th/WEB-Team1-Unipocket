package com.genesis.unipocket.tempexpense.command.presentation.response;

import com.genesis.unipocket.global.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.tempexpense.command.application.result.TemporaryExpenseResult;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TemporaryExpenseUpdateResponse(
		Long tempExpenseId,
		Long tempExpenseMetaId,
		Long fileId,
		String merchantName,
		Category category,
		CurrencyCode localCountryCode,
		BigDecimal localCurrencyAmount,
		CurrencyCode baseCountryCode,
		BigDecimal baseCurrencyAmount,
		String paymentsMethod,
		String memo,
		LocalDateTime occurredAt,
		String status,
		String cardLastFourDigits) {

	public static TemporaryExpenseUpdateResponse from(TemporaryExpenseResult result) {
		return new TemporaryExpenseUpdateResponse(
				result.tempExpenseId(),
				result.tempExpenseMetaId(),
				result.fileId(),
				result.merchantName(),
				result.category(),
				result.localCountryCode(),
				result.localCurrencyAmount(),
				result.baseCountryCode(),
				result.baseCurrencyAmount(),
				result.paymentsMethod(),
				result.memo(),
				result.occurredAt(),
				result.status(),
				result.cardLastFourDigits());
	}
}
