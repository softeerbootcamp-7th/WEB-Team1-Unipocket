package com.genesis.unipocket.expense.command.presentation.response;

import java.util.List;

/**
 * <b>파일 파싱 응답 DTO</b>
 *
 * @author 김동균
 * @since 2026-02-08
 */
public record ParseFileResponse(
		Long metaId,
		int totalParsedCount,
		int normalCount,
		int incompleteCount,
		int abnormalCount,
		List<ParsedItemSummary> items) {

	public record ParsedItemSummary(Long tempExpenseId, String merchantName, String status) {}
}
