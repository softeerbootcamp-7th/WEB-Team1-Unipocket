package com.genesis.unipocket.expense.dto.response;

import java.util.List;

/**
 * <b>Batch 변환 응답 DTO</b>
 *
 * @author 김동균
 * @since 2026-02-08
 */
public record BatchConvertResponse(
		int totalRequested, int successCount, int failedCount, List<ConversionResult> results) {

	public record ConversionResult(
			Long tempExpenseId, Long expenseId, String status, String reason) {}
}
