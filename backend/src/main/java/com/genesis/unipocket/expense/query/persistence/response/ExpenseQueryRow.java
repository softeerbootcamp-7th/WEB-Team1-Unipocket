package com.genesis.unipocket.expense.query.persistence.response;

import com.genesis.unipocket.global.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.global.common.enums.ExpenseSource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

public record ExpenseQueryRow(
		Long expenseId,
		Long accountBookId,
		Long travelId,
		Category category,
		CurrencyCode baseCurrencyCode,
		BigDecimal baseCurrencyAmount,
		BigDecimal exchangeRate,
		CurrencyCode localCurrencyCode,
		BigDecimal localCurrencyAmount,
		OffsetDateTime occurredAt,
		LocalDateTime updatedAt,
		String merchantName,
		String approvalNumber,
		Long userCardId,
		ExpenseSource expenseSource,
		String fileLink,
		String memo,
		String cardNumber) {}
