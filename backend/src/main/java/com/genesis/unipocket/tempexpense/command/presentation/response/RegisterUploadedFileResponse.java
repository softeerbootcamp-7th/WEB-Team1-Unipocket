package com.genesis.unipocket.tempexpense.command.presentation.response;

/**
 * <b>S3 업로드 파일 등록 응답 DTO</b>
 *
 * @author bluefishez
 * @since 2026-02-10
 */
public record RegisterUploadedFileResponse(Long tempExpenseMetaId, String s3Key) {}
