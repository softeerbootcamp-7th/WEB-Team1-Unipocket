package com.genesis.unipocket.expense.query.presentation.request;

import com.genesis.unipocket.global.common.enums.Category;
import java.time.OffsetDateTime;
import java.util.List;

public record ExpenseSearchFilter(
		OffsetDateTime startDate,
		OffsetDateTime endDate,
		List<String> cardNumber, // OR
		List<Category> category, // OR
		List<String> merchantName, // OR
		Long travelId) {}
