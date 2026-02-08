package com.genesis.unipocket.expense.presentation.dto;

import com.genesis.unipocket.expense.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <b>임시지출내역 수정 요청 DTO</b>
 *
 * @author 김동균
 * @since 2026-02-08
 */
public record TemporaryExpenseUpdateRequest(
		String merchantName,
		Category category,
		CurrencyCode localCountryCode,
		BigDecimal localCurrencyAmount,
		CurrencyCode baseCountryCode,
		BigDecimal baseCurrencyAmount,
		String paymentsMethod,
		String memo,
		LocalDateTime occurredAt,
		String cardLastFourDigits) {}
