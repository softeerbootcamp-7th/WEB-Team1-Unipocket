package com.genesis.unipocket.tempexpense.command.application.command;

import com.genesis.unipocket.global.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.tempexpense.command.presentation.request.TemporaryExpenseMetaBulkUpdateItemRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <b>임시지출내역 수정 Command DTO</b>
 * <p>
 * Facade -> Application
 *
 * @author 김동균
 * @since 2026-02-08
 */
public record TemporaryExpenseUpdateCommand(
		String merchantName,
		Category category,
		CurrencyCode localCountryCode,
		BigDecimal localCurrencyAmount,
		CurrencyCode baseCountryCode,
		BigDecimal baseCurrencyAmount,
		String paymentsMethod,
		String memo,
		LocalDateTime occurredAt,
		String cardLastFourDigits) {

	public static TemporaryExpenseUpdateCommand from(
			TemporaryExpenseMetaBulkUpdateItemRequest request) {
		return new TemporaryExpenseUpdateCommand(
				request.merchantName(),
				request.category(),
				request.localCountryCode(),
				request.localCurrencyAmount(),
				request.baseCountryCode(),
				request.baseCurrencyAmount(),
				request.paymentsMethod(),
				request.memo(),
				request.occurredAt(),
				request.cardLastFourDigits());
	}
}
