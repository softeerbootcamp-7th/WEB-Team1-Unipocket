package com.genesis.unipocket.tempexpense.query.presentation.response;

import java.time.LocalDateTime;
import java.util.List;

public record TemporaryExpenseMetaFilesResponse(
		Long tempExpenseMetaId, LocalDateTime createdAt, List<FileExpenses> files) {

	public record FileExpenses(
			Long fileId, String s3Key, String fileType, List<TemporaryExpenseResponse> expenses) {}
}
