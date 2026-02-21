package com.genesis.unipocket.expense.query.presentation.request;

import com.genesis.unipocket.global.common.enums.Category;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ExpenseSearchFilter(
		OffsetDateTime startDate,
		OffsetDateTime endDate,
		Category category,
		BigDecimal minAmount,
		BigDecimal maxAmount,
		String merchantName,
		Long travelId) {}
