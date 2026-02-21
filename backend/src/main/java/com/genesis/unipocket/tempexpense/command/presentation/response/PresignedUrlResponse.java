package com.genesis.unipocket.tempexpense.command.presentation.response;

import com.genesis.unipocket.tempexpense.command.application.result.FileUploadResult;

public record PresignedUrlResponse(
		Long tempExpenseMetaId, String presignedUrl, String s3Key, int expiresIn) {

	public static PresignedUrlResponse from(FileUploadResult result) {
		return new PresignedUrlResponse(
				result.tempExpenseMetaId(),
				result.presignedUrl(),
				result.s3Key(),
				result.expiresIn());
	}
}
