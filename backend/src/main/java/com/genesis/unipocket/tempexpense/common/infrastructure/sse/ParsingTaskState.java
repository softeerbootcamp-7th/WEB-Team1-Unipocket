package com.genesis.unipocket.tempexpense.common.infrastructure.sse;

record ParsingTaskState(
		Long accountBookId,
		int progress,
		TaskStatus status,
		String errorCode,
		Integer errorStatus,
		String errorMessage) {}
