package com.genesis.unipocket.tempexpense.query.persistence.response;

import java.time.LocalDateTime;

public record TemporaryExpenseMetaSummaryRow(
		Long tempExpenseMetaId,
		LocalDateTime createdAt,
		long fileCount,
		long normalCount,
		long incompleteCount,
		long abnormalCount) {}
