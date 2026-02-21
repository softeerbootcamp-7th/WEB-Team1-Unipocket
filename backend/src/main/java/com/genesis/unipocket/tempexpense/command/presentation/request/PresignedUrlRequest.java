package com.genesis.unipocket.tempexpense.command.presentation.request;

public record PresignedUrlRequest(
		String fileName, String mimeType, UploadType uploadType, Long tempExpenseMetaId) {
	public enum UploadType {
		IMAGE,
		DOCS
	}
}
