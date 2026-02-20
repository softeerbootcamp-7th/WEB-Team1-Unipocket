package com.genesis.unipocket.expense.command.presentation.response;

import com.genesis.unipocket.expense.command.application.result.ExpenseResult;
import java.util.List;

public record ExpenseBulkUpdateResponse(int totalUpdated, List<ExpenseUpdateResponse> items) {

	public static ExpenseBulkUpdateResponse from(List<ExpenseResult> results) {
		return new ExpenseBulkUpdateResponse(
				results.size(), results.stream().map(ExpenseUpdateResponse::from).toList());
	}
}
