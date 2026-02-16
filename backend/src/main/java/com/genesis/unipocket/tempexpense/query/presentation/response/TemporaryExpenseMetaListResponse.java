package com.genesis.unipocket.tempexpense.query.presentation.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <b>가계부의 임시지출 메타 목록 응답 DTO</b>
 */
public record TemporaryExpenseMetaListResponse(List<MetaSummary> metas) {

	public record MetaSummary(
			Long tempExpenseMetaId,
			LocalDateTime createdAt,
			int fileCount,
			int totalExpenses,
			int normalCount,
			int incompleteCount,
			int abnormalCount) {}
}

