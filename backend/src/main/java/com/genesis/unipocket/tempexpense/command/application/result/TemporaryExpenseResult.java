package com.genesis.unipocket.tempexpense.command.application.result;

import com.genesis.unipocket.global.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.tempexpense.command.persistence.entity.TemporaryExpense;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TemporaryExpenseResult(
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

	public static TemporaryExpenseResult from(TemporaryExpense entity) {
		return new TemporaryExpenseResult(
				entity.getTempExpenseId(),
				entity.getTempExpenseMetaId(),
				entity.getFileId(),
				entity.getMerchantName(),
				entity.getCategory(),
				entity.getLocalCountryCode(),
				entity.getLocalCurrencyAmount(),
				entity.getBaseCountryCode(),
				entity.getBaseCurrencyAmount(),
				entity.getMemo(),
				entity.getOccurredAt(),
				entity.getStatus() != null ? entity.getStatus().name() : null,
				entity.getCardLastFourDigits());
	}
}
