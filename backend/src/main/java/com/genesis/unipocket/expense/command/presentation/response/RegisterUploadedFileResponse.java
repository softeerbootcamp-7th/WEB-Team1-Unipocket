package com.genesis.unipocket.expense.command.presentation.response;

/**
 * <b>S3 업로드 파일 등록 응답 DTO</b>
 *
 * @author bluefishez
 * @since 2026-02-10
 */
public record RegisterUploadedFileResponse(Long fileId, Long metaId, String s3Key) {}
