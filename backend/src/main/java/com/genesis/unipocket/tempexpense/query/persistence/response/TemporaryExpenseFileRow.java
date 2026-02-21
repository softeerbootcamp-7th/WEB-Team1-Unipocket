package com.genesis.unipocket.tempexpense.query.persistence.response;

public record TemporaryExpenseFileRow(
		Long fileId, Long tempExpenseMetaId, String s3Key, Object fileType) {}
