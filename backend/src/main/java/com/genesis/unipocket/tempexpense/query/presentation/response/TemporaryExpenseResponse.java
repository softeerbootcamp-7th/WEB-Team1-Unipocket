package com.genesis.unipocket.tempexpense.query.presentation.response;

import com.genesis.unipocket.global.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.tempexpense.command.persistence.entity.TemporaryExpense;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <b>임시지출내역 응답 DTO</b>
 *
 * @author 김동균
 * @since 2026-02-08
 */
public record TemporaryExpenseResponse(
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

	/**
	 * Entity → Response DTO
	 */
	public static TemporaryExpenseResponse from(TemporaryExpense entity) {
		return new TemporaryExpenseResponse(
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

	/**
	 * Entity List → Response DTO List
	 */
	public static List<TemporaryExpenseResponse> fromList(List<TemporaryExpense> entities) {
		return entities.stream().map(TemporaryExpenseResponse::from).toList();
	}
}
