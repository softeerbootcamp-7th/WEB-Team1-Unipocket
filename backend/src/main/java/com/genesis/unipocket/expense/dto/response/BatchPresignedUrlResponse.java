package com.genesis.unipocket.expense.dto.response;

import java.util.List;

/**
 * <b>Batch Presigned URL 응답 DTO</b>
 *
 * @author 김동균
 * @since 2026-02-08
 */
public record BatchPresignedUrlResponse(List<FileUploadInfo> files) {

	public record FileUploadInfo(Long fileId, String presignedUrl, String s3Key) {}
}
