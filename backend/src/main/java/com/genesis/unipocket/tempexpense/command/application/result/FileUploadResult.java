package com.genesis.unipocket.tempexpense.command.application.result;

public record FileUploadResult(
		Long tempExpenseMetaId, String presignedUrl, String s3Key, int expiresIn) {}
