package com.genesis.unipocket.tempexpense.command.application.command;

import com.genesis.unipocket.global.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.tempexpense.command.presentation.request.TemporaryExpenseMetaBulkUpdateItemRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

public record TemporaryExpenseUpdateCommand(
		String merchantName,
		Category category,
		CurrencyCode localCountryCode,
		BigDecimal localCurrencyAmount,
		CurrencyCode baseCountryCode,
		BigDecimal baseCurrencyAmount,
		String memo,
		LocalDateTime occurredAt,
		Optional<String> cardLastFourDigits) {

	public static TemporaryExpenseUpdateCommand from(
			TemporaryExpenseMetaBulkUpdateItemRequest request) {
		return new TemporaryExpenseUpdateCommand(
				request.merchantName(),
				request.category(),
				request.localCountryCode(),
				request.localCurrencyAmount(),
				request.baseCountryCode(),
				request.baseCurrencyAmount(),
				request.memo(),
				request.occurredAt(),
				request.cardLastFourDigits());
	}
}
