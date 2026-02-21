package com.genesis.unipocket.expense.query.presentation.response;

import java.util.List;

public record ExpenseListResponse(
		List<ExpenseResponse> expenses, long totalCount, int page, int size) {

	public static ExpenseListResponse of(
			List<ExpenseResponse> expenses, long totalCount, int page, int size) {
		return new ExpenseListResponse(expenses, totalCount, page, size);
	}
}
