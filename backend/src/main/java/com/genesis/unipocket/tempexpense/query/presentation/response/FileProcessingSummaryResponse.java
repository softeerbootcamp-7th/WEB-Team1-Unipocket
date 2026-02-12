package com.genesis.unipocket.tempexpense.query.presentation.response;

import java.util.List;

/**
 * <b>파일(이미지) 단위 처리 현황 응답 DTO</b>
 *
 * @author Antigravity
 * @since 2026-02-11
 */
public record FileProcessingSummaryResponse(
		List<FileSummary> files, int totalFiles, int processedFiles, int unprocessedFiles) {

	public record FileSummary(
			Long tempExpenseMetaId,
			String s3Key,
			String fileType,
			int totalExpenses,
			int normalCount,
			int incompleteCount,
			int abnormalCount,
			boolean processed) {}
}
