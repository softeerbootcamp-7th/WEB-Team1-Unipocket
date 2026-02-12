package com.genesis.unipocket.tempexpense.command.presentation.request;

import com.genesis.unipocket.tempexpense.command.persistence.entity.File.FileType;

/**
 * <b>Presigned URL 요청 DTO</b>
 *
 * @author 김동균
 * @since 2026-02-08
 */
public record PresignedUrlRequest(String fileName, FileType fileType) {}
