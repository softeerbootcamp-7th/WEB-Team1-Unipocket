package com.genesis.unipocket.tempexpense.query.presentation.response;

/**
 * <b>가계부 전체 이미지 처리 현황 응답 DTO</b>
 *
 * @author Antigravity
 * @since 2026-02-11
 */
public record ImageProcessingSummaryResponse(
		int totalImages,
		int processedImages,
		int unprocessedImages,
		int totalExpenses,
		int normalExpenses,
		int incompleteExpenses,
		int abnormalExpenses) {}
