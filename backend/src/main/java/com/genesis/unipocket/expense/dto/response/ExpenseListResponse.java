package com.genesis.unipocket.expense.dto.response;

import java.util.List;

/**
 * <b>지출내역 목록 조회 응답 DTO</b>
 *
 * @author bluefishez
 * @since 2026-02-07
 */
public record ExpenseListResponse(
		List<ExpenseResponse> expenses, long totalCount, int page, int size) {

	public static ExpenseListResponse of(
			List<ExpenseResponse> expenses, long totalCount, int page, int size) {
		return new ExpenseListResponse(expenses, totalCount, page, size);
	}
}
