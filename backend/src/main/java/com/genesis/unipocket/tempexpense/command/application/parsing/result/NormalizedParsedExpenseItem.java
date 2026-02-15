package com.genesis.unipocket.tempexpense.command.application.parsing.result;

import com.genesis.unipocket.global.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record NormalizedParsedExpenseItem(
		String merchantName,
		Category category,
		CurrencyCode localCurrencyCode,
		BigDecimal localAmount,
		CurrencyCode baseCurrencyCode,
		BigDecimal baseAmount,
		String memo,
		LocalDateTime occurredAt,
		String cardLastFourDigits,
		String approvalNumber) {}
