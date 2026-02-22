package com.genesis.unipocket.expense.command.application.command;

import com.genesis.unipocket.global.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ExpenseCreateCommand(
		Long accountBookId,
		String merchantName,
		Category category,
		Long userCardId,
		OffsetDateTime occurredAt,
		BigDecimal localCurrencyAmount,
		BigDecimal baseCurrencyAmount,
		CurrencyCode localCurrencyCode,
		CurrencyCode baseCurrencyCode,
		String memo,
		Long travelId) {}
