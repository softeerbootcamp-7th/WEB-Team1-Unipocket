package com.genesis.unipocket.expense.command.presentation.request;

import com.genesis.unipocket.global.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;

public record ExpenseBulkUpdateItemRequest(
		@NotNull Long expenseId,
		@NotBlank @Size(max = 40, message = "거래처명은 40자 이하여야 합니다.") String merchantName,
		Category category,
		Long userCardId,
		@NotNull Instant occurredAt,
		BigDecimal localCurrencyAmount,
		CurrencyCode localCurrencyCode,
		BigDecimal baseCurrencyAmount,
		String memo,
		Long travelId) {}
