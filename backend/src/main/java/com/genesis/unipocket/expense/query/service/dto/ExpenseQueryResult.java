package com.genesis.unipocket.expense.query.service.dto;

import com.genesis.unipocket.global.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.global.common.enums.ExpenseSource;
import com.genesis.unipocket.user.common.enums.CardCompany;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ExpenseQueryResult(
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
		OffsetDateTime updatedAt,
		String displayMerchantName,
		String approvalNumber,
		Long userCardId,
		CardCompany cardCompany,
		String cardLabel,
		String cardLastDigits,
		ExpenseSource expenseSource,
		String fileLink,
		String memo,
		String cardNumber) {}
