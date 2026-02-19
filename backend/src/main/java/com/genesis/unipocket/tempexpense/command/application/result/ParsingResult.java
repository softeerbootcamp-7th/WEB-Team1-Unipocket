package com.genesis.unipocket.tempexpense.command.application.result;

import com.genesis.unipocket.tempexpense.command.persistence.entity.TemporaryExpense;
import java.util.List;

public record ParsingResult(
		Long metaId,
		int totalCount,
		int normalCount,
		int incompleteCount,
		int abnormalCount,
		List<TemporaryExpense> expenses) {}
