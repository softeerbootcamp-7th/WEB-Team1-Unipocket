package com.genesis.unipocket.tempexpense.command.presentation.request;

/**
 * <b>Presigned URL 요청 DTO</b>
 *
 * @author 김동균
 * @since 2026-02-08
 */
public record PresignedUrlRequest(
		String fileName, String mimeType, UploadType uploadType, Long tempExpenseMetaId) {
	public enum UploadType {
		IMAGE,
		DOCS
	}
}
