package com.genesis.unipocket.expense.service;

import com.genesis.unipocket.expense.persistence.entity.expense.File;
import com.genesis.unipocket.expense.persistence.entity.expense.File.FileType;
import com.genesis.unipocket.expense.persistence.entity.expense.TempExpenseMeta;
import com.genesis.unipocket.expense.persistence.repository.FileRepository;
import com.genesis.unipocket.expense.persistence.repository.TempExpenseMetaRepository;
import com.genesis.unipocket.global.infrastructure.aws.S3Service;
import com.genesis.unipocket.global.infrastructure.aws.S3Service.PresignedUrlResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <b>파일 업로드 서비스</b>
 *
 * @author 김동균
 * @since 2026-02-08
 */
@Service
@AllArgsConstructor
public class FileUploadService {

	private final TempExpenseMetaRepository tempExpenseMetaRepository;
	private final FileRepository fileRepository;
	private final S3Service s3Service;

	/**
	 * Presigned URL 생성 및 메타데이터 저장
	 */
	@Transactional
	public FileUploadResponse createPresignedUrl(
			Long accountBookId, String fileName, FileType fileType) {
		// 1. TempExpenseMeta 생성
		TempExpenseMeta meta = TempExpenseMeta.builder().accountBookId(accountBookId).build();
		TempExpenseMeta savedMeta = tempExpenseMetaRepository.save(meta);

		// 2. S3 Presigned URL 생성
		String prefix = "temp-expenses/" + accountBookId;
		PresignedUrlResponse s3Response = s3Service.getPresignedUrl(prefix, fileName);

		// 3. File 엔티티 생성
		File file =
				File.builder()
						.tempExpenseMetaId(savedMeta.getTempExpenseMetaId())
						.fileType(fileType)
						.s3Key(s3Response.imageKey())
						.build();
		File savedFile = fileRepository.save(file);

		// 4. Response 반환
		return new FileUploadResponse(
				savedFile.getFileId(), s3Response.presignedUrl(), s3Response.imageKey(), 300);
	}

	/**
	 * 여러 파일 Presigned URL 생성 (Batch)
	 */
	@Transactional
	public java.util.List<FileUploadResponse> createBatchPresignedUrls(
			Long accountBookId,
			java.util.List<
							com.genesis.unipocket.expense.dto.request.BatchPresignedUrlRequest
									.FileInfo>
					files) {
		java.util.List<FileUploadResponse> responses = new java.util.ArrayList<>();

		for (com.genesis.unipocket.expense.dto.request.BatchPresignedUrlRequest.FileInfo fileInfo :
				files) {
			FileUploadResponse response =
					createPresignedUrl(accountBookId, fileInfo.fileName(), fileInfo.fileType());
			responses.add(response);
		}

		return responses;
	}

	/**
	 * 파일 업로드 응답
	 */
	public record FileUploadResponse(
			Long fileId, String presignedUrl, String s3Key, int expiresIn) {}
}
