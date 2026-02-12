package com.genesis.unipocket.tempexpense.command.application.result;

public record BatchParsingResult(
		Long metaId, int totalParsed, int normalCount, int incompleteCount, int abnormalCount) {}
