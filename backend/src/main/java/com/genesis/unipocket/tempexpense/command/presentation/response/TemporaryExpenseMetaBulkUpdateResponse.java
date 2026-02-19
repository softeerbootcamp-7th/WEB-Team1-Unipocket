package com.genesis.unipocket.tempexpense.command.presentation.response;

import com.genesis.unipocket.tempexpense.command.application.result.TemporaryExpenseMetaBulkUpdateResult;
import com.genesis.unipocket.tempexpense.query.presentation.response.TemporaryExpenseResponse;
import java.util.List;

/**
 * <b>메타 단위 임시지출 일괄 수정 응답</b>
 */
public record TemporaryExpenseMetaBulkUpdateResponse(
		int totalRequested, int successCount, int failedCount, List<ItemResult> results) {

	public static TemporaryExpenseMetaBulkUpdateResponse from(
			TemporaryExpenseMetaBulkUpdateResult result) {
		List<ItemResult> mappedResults = result.results().stream().map(ItemResult::from).toList();
		return new TemporaryExpenseMetaBulkUpdateResponse(
				result.totalRequested(),
				result.successCount(),
				result.failedCount(),
				mappedResults);
	}

	public record ItemResult(
			Long tempExpenseId, String status, String reason, TemporaryExpenseResponse updated) {

		public static ItemResult from(TemporaryExpenseMetaBulkUpdateResult.ItemResult item) {
			return new ItemResult(
					item.tempExpenseId(),
					item.status(),
					item.reason(),
					item.updated() != null ? TemporaryExpenseResponse.from(item.updated()) : null);
		}
	}
}
