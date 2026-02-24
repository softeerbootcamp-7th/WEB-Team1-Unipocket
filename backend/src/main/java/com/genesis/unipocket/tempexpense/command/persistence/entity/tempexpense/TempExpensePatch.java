package com.genesis.unipocket.tempexpense.command.persistence.entity.tempexpense;

import com.genesis.unipocket.global.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.tempexpense.command.application.command.TemporaryExpenseUpdateCommand;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TempExpensePatch(
		String merchantName,
		Category category,
		CurrencyCode localCurrencyCode,
		BigDecimal localCurrencyAmount,
		CurrencyCode baseCurrencyCode,
		BigDecimal baseCurrencyAmount,
		BigDecimal exchangeRate,
		String memo,
		LocalDateTime occurredAt,
		String cardLastFourDigits,
		String approvalNumber) {
	public boolean hasAnyChange() {
		return merchantName != null
				|| category != null
				|| localCurrencyCode != null
				|| localCurrencyAmount != null
				|| baseCurrencyCode != null
				|| baseCurrencyAmount != null
				|| exchangeRate != null
				|| memo != null
				|| occurredAt != null
				|| cardLastFourDigits != null
				|| approvalNumber != null;
	}

	public static TempExpensePatch from(
			String merchantName,
			Category category,
			CurrencyCode localCurrencyCode,
			BigDecimal localCurrencyAmount,
			CurrencyCode baseCurrencyCode,
			BigDecimal baseCurrencyAmount,
			BigDecimal exchangeRate,
			String memo,
			LocalDateTime occurredAt,
			String cardLastFourDigits,
			String approvalNumber) {
		return new TempExpensePatch(
				normalizeBlank(merchantName),
				category,
				localCurrencyCode,
				localCurrencyAmount,
				baseCurrencyCode,
				baseCurrencyAmount,
				exchangeRate,
				memo,
				occurredAt,
				normalizeBlank(cardLastFourDigits),
				normalizeBlank(approvalNumber));
	}

	public static TempExpensePatch from(
			TemporaryExpenseUpdateCommand command, CurrencyCode resolvedBaseCurrencyCode) {
		return from(
				command.merchantName(),
				command.category(),
				command.localCountryCode(),
				command.localCurrencyAmount(),
				resolvedBaseCurrencyCode,
				command.baseCurrencyAmount(),
				null,
				command.memo(),
				command.occurredAt(),
				command.cardLastFourDigits(),
				null);
	}

	private static String normalizeBlank(String value) {
		if (value == null) return null;
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}
}
