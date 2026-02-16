package com.genesis.unipocket.tempexpense.query.presentation.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <b>메타 단위 파일별 임시지출 상세 응답 DTO</b>
 */
public record TemporaryExpenseMetaFilesResponse(
		Long tempExpenseMetaId, LocalDateTime createdAt, List<FileExpenses> files) {

	public record FileExpenses(
			Long fileId, String s3Key, String fileType, List<TemporaryExpenseResponse> expenses) {}
}

