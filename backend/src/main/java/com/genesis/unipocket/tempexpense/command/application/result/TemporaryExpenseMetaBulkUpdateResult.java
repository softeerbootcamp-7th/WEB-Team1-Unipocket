package com.genesis.unipocket.tempexpense.command.application.result;

import java.util.List;

/**
 * <b>메타 단위 임시지출 일괄 수정 결과 DTO</b>
 */
public record TemporaryExpenseMetaBulkUpdateResult(
		int totalRequested, int successCount, int failedCount, List<ItemResult> results) {

	public record ItemResult(
			Long tempExpenseId, String status, String reason, TemporaryExpenseResult updated) {}
}
