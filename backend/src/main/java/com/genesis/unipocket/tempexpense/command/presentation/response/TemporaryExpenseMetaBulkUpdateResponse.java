package com.genesis.unipocket.tempexpense.command.presentation.response;

import com.genesis.unipocket.tempexpense.query.presentation.response.TemporaryExpenseResponse;
import java.util.List;

/**
 * <b>메타 단위 임시지출 일괄 수정 응답</b>
 */
public record TemporaryExpenseMetaBulkUpdateResponse(
		int totalRequested, int successCount, int failedCount, List<ItemResult> results) {

	public record ItemResult(
			Long tempExpenseId, String status, String reason, TemporaryExpenseResponse updated) {}
}
