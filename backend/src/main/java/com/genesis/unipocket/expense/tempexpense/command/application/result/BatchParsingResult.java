package com.genesis.unipocket.expense.tempexpense.command.application.result;

public record BatchParsingResult(
		Long metaId, int totalParsed, int normalCount, int incompleteCount, int abnormalCount) {}
