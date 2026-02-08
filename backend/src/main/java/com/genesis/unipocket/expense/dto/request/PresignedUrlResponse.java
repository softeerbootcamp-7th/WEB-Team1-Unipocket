package com.genesis.unipocket.expense.dto.request;

/**
 * <b>Presigned URL 응답 DTO</b>
 *
 * @author 김동균
 * @since 2026-02-08
 */
public record PresignedUrlResponse(Long fileId, String presignedUrl, String s3Key, int expiresIn) {}
