package com.genesis.unipocket.tempexpense.command.application;

import com.genesis.unipocket.media.command.application.MediaObjectStorage;
import com.genesis.unipocket.media.command.application.result.PresignedUrlResult;
import com.genesis.unipocket.tempexpense.command.application.result.FileRegisterResult;
import com.genesis.unipocket.tempexpense.command.application.result.FileUploadResult;
import com.genesis.unipocket.tempexpense.command.persistence.entity.File;
import com.genesis.unipocket.tempexpense.command.persistence.entity.File.FileType;
import com.genesis.unipocket.tempexpense.command.persistence.entity.TempExpenseMeta;
import com.genesis.unipocket.tempexpense.command.persistence.repository.FileRepository;
import com.genesis.unipocket.tempexpense.command.persistence.repository.TempExpenseMetaRepository;
import com.genesis.unipocket.tempexpense.command.persistence.repository.TemporaryExpenseRepository;
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
	private final TemporaryExpenseRepository temporaryExpenseRepository;
	private final MediaObjectStorage mediaObjectStorage;
	private static final String TEMP_EXPENSE_PREFIX = "temp-expenses/";

	/**
	 * Presigned URL 생성 및 메타데이터 저장
	 */
	@Transactional
	public FileUploadResult createPresignedUrl(
			Long accountBookId, String fileName, FileType fileType) {
		// 1. TempExpenseMeta 생성
		TempExpenseMeta meta = TempExpenseMeta.builder().accountBookId(accountBookId).build();
		TempExpenseMeta savedMeta = tempExpenseMetaRepository.save(meta);

		// 2. S3 Presigned URL 생성
		String prefix = "temp-expenses/" + accountBookId;
		PresignedUrlResult s3Response = mediaObjectStorage.getPresignedUrl(prefix, fileName);

		// 3. File 엔티티 생성
		File file =
				File.builder()
						.tempExpenseMetaId(savedMeta.getTempExpenseMetaId())
						.fileType(fileType)
						.s3Key(s3Response.imageKey())
						.build();
		fileRepository.save(file);

		// 4. Response 반환
		return new FileUploadResult(
				savedMeta.getTempExpenseMetaId(),
				s3Response.presignedUrl(),
				s3Response.imageKey(),
				300);
	}

	/**
	 * S3 업로드 파일 등록 (s3Key 기반)
	 */
	@Transactional
	public FileRegisterResult registerUploadedFile(
			Long accountBookId, String s3Key, FileType fileType) {
		if (s3Key == null || s3Key.isBlank()) {
			throw new IllegalArgumentException("s3Key는 필수입니다.");
		}
		if (!s3Key.startsWith(TEMP_EXPENSE_PREFIX + accountBookId + "/")) {
			throw new IllegalArgumentException("s3Key 경로가 올바르지 않습니다.");
		}
		if (fileRepository.existsByS3Key(s3Key)) {
			throw new IllegalArgumentException("이미 등록된 s3Key입니다.");
		}

		TempExpenseMeta meta = TempExpenseMeta.builder().accountBookId(accountBookId).build();
		TempExpenseMeta savedMeta = tempExpenseMetaRepository.save(meta);

		File file =
				File.builder()
						.tempExpenseMetaId(savedMeta.getTempExpenseMetaId())
						.fileType(fileType)
						.s3Key(s3Key)
						.build();
		File savedFile = fileRepository.save(file);

		return new FileRegisterResult(savedMeta.getTempExpenseMetaId(), savedFile.getS3Key());
	}

	@Transactional
	public void deleteMeta(Long accountBookId, Long tempExpenseMetaId) {
		TempExpenseMeta meta =
				tempExpenseMetaRepository
						.findById(tempExpenseMetaId)
						.orElseThrow(() -> new IllegalArgumentException("메타데이터를 찾을 수 없습니다."));
		if (!meta.getAccountBookId().equals(accountBookId)) {
			throw new IllegalArgumentException("가계부와 메타데이터가 일치하지 않습니다.");
		}
		temporaryExpenseRepository.deleteByTempExpenseMetaId(tempExpenseMetaId);
		fileRepository.deleteByTempExpenseMetaId(tempExpenseMetaId);
		tempExpenseMetaRepository.delete(meta);
	}
}
