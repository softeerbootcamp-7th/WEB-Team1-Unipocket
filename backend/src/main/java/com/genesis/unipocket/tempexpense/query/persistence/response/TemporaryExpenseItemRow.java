package com.genesis.unipocket.tempexpense.query.persistence.response;

import com.genesis.unipocket.global.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.tempexpense.common.enums.TemporaryExpenseStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TemporaryExpenseItemRow(
		Long tempExpenseId,
		Long tempExpenseMetaId,
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
		TemporaryExpenseStatus status,
		String cardLastFourDigits) {}
