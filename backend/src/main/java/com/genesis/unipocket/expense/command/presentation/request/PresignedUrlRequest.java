package com.genesis.unipocket.expense.command.presentation.request;

import com.genesis.unipocket.expense.command.persistence.entity.expense.File.FileType;

/**
 * <b>Presigned URL 요청 DTO</b>
 *
 * @author 김동균
 * @since 2026-02-08
 */
public record PresignedUrlRequest(Long accountBookId, String fileName, FileType fileType) {}
