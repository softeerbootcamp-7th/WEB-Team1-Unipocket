package com.genesis.unipocket.tempexpense.common.infrastructure.sse;

record ParsingTaskState(
		Long accountBookId,
		int progress,
		TaskStatus status,
		String errorCode,
		Integer errorStatus,
		String errorMessage,
		String lastMessage,
		String lastCode,
		String lastFileKey) {

	ParsingTaskState(
			Long accountBookId,
			int progress,
			TaskStatus status,
			String errorCode,
			Integer errorStatus,
			String errorMessage) {
		this(accountBookId, progress, status, errorCode, errorStatus, errorMessage, null, null, null);
	}
}
