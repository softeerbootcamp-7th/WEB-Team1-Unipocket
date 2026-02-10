package com.genesis.unipocket.expense.command.presentation.request;

import com.genesis.unipocket.expense.command.persistence.entity.expense.File.FileType;
import java.util.List;

/**
 * <b>Batch Presigned URL 요청 DTO</b>
 *
 * @author 김동균
 * @since 2026-02-08
 */
public record BatchPresignedUrlRequest(Long accountBookId, List<FileInfo> files) {

	public record FileInfo(String fileName, FileType fileType) {}
}
