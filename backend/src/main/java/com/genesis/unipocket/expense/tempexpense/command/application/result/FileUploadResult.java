package com.genesis.unipocket.expense.tempexpense.command.application.result;

public record FileUploadResult(
		Long tempExpenseMetaId, String presignedUrl, String s3Key, int expiresIn) {}
