package com.genesis.unipocket.tempexpense.common.infrastructure.sse;

record ParsingTaskState(
		Long accountBookId, int progress, TaskStatus status, String errorMessage) {}
