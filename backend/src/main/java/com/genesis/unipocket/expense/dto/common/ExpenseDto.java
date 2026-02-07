package com.genesis.unipocket.expense.dto.common;

import com.genesis.unipocket.expense.common.enums.Category;
import com.genesis.unipocket.expense.common.enums.ExpenseSource;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ExpenseDto(
		Long id,
		Long accountBookId,
		Long travelId,
		Category category,
		CurrencyCode baseCurrencyCode,
		BigDecimal baseCurrencyAmount,
		CurrencyCode localCurrencyCode,
		BigDecimal localCurrencyAmount,
		LocalDateTime occurredAt,
		String merchantName,
		String displayMerchantName,
		String approvalNumber,
		String paymentMethod,
		ExpenseSource expenseSource,
		String fileLink,
		String memo,
		String cardNumber) {}
