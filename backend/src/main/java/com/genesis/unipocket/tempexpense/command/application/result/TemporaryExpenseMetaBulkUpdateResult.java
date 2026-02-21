package com.genesis.unipocket.tempexpense.command.application.result;

import java.util.List;

public record TemporaryExpenseMetaBulkUpdateResult(
		int totalRequested, int successCount, int failedCount, List<ItemResult> results) {

	public record ItemResult(
			Long tempExpenseId, String status, String reason, TemporaryExpenseResult updated) {}
}
