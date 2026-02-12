package com.genesis.unipocket.expense.tempexpense.command.application.result;

import com.genesis.unipocket.expense.tempexpense.command.persistence.entity.TemporaryExpense;
import java.util.List;

public record ParsingResult(
		Long metaId,
		int totalCount,
		int normalCount,
		int incompleteCount,
		int abnormalCount,
		List<TemporaryExpense> expenses) {}
