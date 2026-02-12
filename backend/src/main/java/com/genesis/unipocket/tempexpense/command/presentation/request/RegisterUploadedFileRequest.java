package com.genesis.unipocket.tempexpense.command.presentation.request;

import com.genesis.unipocket.tempexpense.command.persistence.entity.File.FileType;

/**
 * <b>S3 업로드 파일 등록 요청 DTO</b>
 *
 * @author bluefishez
 * @since 2026-02-10
 */
public record RegisterUploadedFileRequest(String s3Key, FileType fileType) {}
