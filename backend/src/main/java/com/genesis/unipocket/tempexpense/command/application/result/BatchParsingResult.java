package com.genesis.unipocket.tempexpense.command.application.result;

import java.util.List;

public record BatchParsingResult(
		Long metaId,
		int totalParsed,
		int normalCount,
		int incompleteCount,
		int abnormalCount,
		List<FileParsingOutcome> fileResults) {

	public record FileParsingOutcome(Long fileId, String fileKey, String status, String message) {}
}
