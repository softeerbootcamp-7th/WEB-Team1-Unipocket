package com.genesis.unipocket.tempexpense.command.application.result;

public record FileUploadResult(
		Long tempExpenseMetaId,
		String presignedUrl,
		String s3Key,
		String fileName,
		int expiresIn) {}
