package com.genesis.unipocket.expense.command.presentation.response;

import com.genesis.unipocket.expense.command.application.result.TemporaryExpenseResult;
import com.genesis.unipocket.expense.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <b>임시지출내역 수정 응답 DTO</b>
 *
 * @author bluefishez
 * @since 2026-02-10
 */
public record TemporaryExpenseUpdateResponse(
		Long tempExpenseId,
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
