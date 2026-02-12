package com.genesis.unipocket.tempexpense.command.application.result;

import com.genesis.unipocket.global.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.tempexpense.command.persistence.entity.TemporaryExpense;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <b>임시지출내역 결과 DTO</b>
 * <p>Application -> Facade
 *
 * @author bluefishez
 * @since 2026-02-10
 */
public record TemporaryExpenseResult(
		Long tempExpenseId,
		Long tempExpenseMetaId,
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

	public static TemporaryExpenseResult from(TemporaryExpense entity) {
		return new TemporaryExpenseResult(
				entity.getTempExpenseId(),
				entity.getTempExpenseMetaId(),
				entity.getMerchantName(),
				entity.getCategory(),
				entity.getLocalCountryCode(),
				entity.getLocalCurrencyAmount(),
				entity.getBaseCountryCode(),
				entity.getBaseCurrencyAmount(),
				entity.getPaymentsMethod(),
				entity.getMemo(),
				entity.getOccurredAt(),
				entity.getStatus() != null ? entity.getStatus().name() : null,
				entity.getCardLastFourDigits());
	}
}
