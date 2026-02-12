package com.genesis.unipocket.tempexpense.command.presentation.response;

/**
 * <b>Presigned URL 응답 DTO</b>
 *
 * @author 김동균
 * @since 2026-02-08
 */
public record PresignedUrlResponse(
		Long tempExpenseMetaId, String presignedUrl, String s3Key, int expiresIn) {}
