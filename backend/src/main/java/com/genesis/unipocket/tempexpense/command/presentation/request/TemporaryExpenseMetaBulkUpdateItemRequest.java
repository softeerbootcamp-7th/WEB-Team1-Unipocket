package com.genesis.unipocket.tempexpense.command.presentation.request;

import com.genesis.unipocket.global.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <b>메타 단위 임시지출 수정 항목</b>
 */
public record TemporaryExpenseMetaBulkUpdateItemRequest(
		@NotNull Long tempExpenseId,
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
