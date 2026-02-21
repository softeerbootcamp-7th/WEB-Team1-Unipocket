package com.genesis.unipocket.expense.query.persistence.response;

import com.genesis.unipocket.global.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.global.common.enums.ExpenseSource;
import com.genesis.unipocket.user.common.enums.CardCompany;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

public record ExpenseOneShotRow(
		Long expenseId,
		Long accountBookId,
		Long travelId,
		String travelName,
		String travelImageKey,
		String merchantName,
		BigDecimal exchangeRate,
		Category category,
		OffsetDateTime occurredAt,
		LocalDateTime updatedAt,
		BigDecimal localCurrencyAmount,
		CurrencyCode localCurrencyCode,
		BigDecimal baseCurrencyAmount,
		CurrencyCode baseCurrencyCode,
		String memo,
		ExpenseSource source,
		String approvalNumber,
		String expenseCardNumber,
		String fileLink,
		Long userCardId,
		CardCompany cardCompany,
		String cardLabel,
		String cardLastDigits) {}
