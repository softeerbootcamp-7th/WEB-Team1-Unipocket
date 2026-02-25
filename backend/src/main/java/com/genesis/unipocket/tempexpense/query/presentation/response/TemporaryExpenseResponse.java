package com.genesis.unipocket.tempexpense.query.presentation.response;

import com.genesis.unipocket.global.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.tempexpense.query.persistence.response.TemporaryExpenseItemRow;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TemporaryExpenseResponse(
		Long tempExpenseId,
		Long tempExpenseMetaId,
		Long fileId,
		String merchantName,
		Category category,
		CurrencyCode localCountryCode,
		BigDecimal localCurrencyAmount,
		CurrencyCode baseCountryCode,
		BigDecimal baseCurrencyAmount,
		String memo,
		LocalDateTime occurredAt,
		String status,
		String cardLastFourDigits) {

	public static TemporaryExpenseResponse from(TemporaryExpenseItemRow row) {
		return new TemporaryExpenseResponse(
				row.tempExpenseId(),
				row.tempExpenseMetaId(),
				row.fileId(),
				row.merchantName(),
				row.category(),
				row.localCountryCode(),
				row.localCurrencyAmount(),
				row.baseCountryCode(),
				row.baseCurrencyAmount(),
				row.memo(),
				row.occurredAt(),
				row.status() != null ? row.status().name() : null,
				row.cardLastFourDigits());
	}
}
