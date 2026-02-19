package com.genesis.unipocket.tempexpense.command.application;

import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.global.infrastructure.MediaContentType;
import com.genesis.unipocket.media.command.application.result.PresignedUrlResult;
import com.genesis.unipocket.tempexpense.command.application.result.FileUploadResult;
import com.genesis.unipocket.tempexpense.command.facade.port.TempExpenseMediaAccessService;
import com.genesis.unipocket.tempexpense.command.persistence.entity.File;
import com.genesis.unipocket.tempexpense.command.persistence.entity.File.FileType;
import com.genesis.unipocket.tempexpense.command.persistence.entity.TempExpenseMeta;
import com.genesis.unipocket.tempexpense.command.persistence.repository.FileRepository;
import com.genesis.unipocket.tempexpense.command.persistence.repository.TempExpenseMetaRepository;
import com.genesis.unipocket.tempexpense.command.persistence.repository.TemporaryExpenseRepository;
import com.genesis.unipocket.tempexpense.command.presentation.request.PresignedUrlRequest.UploadType;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <b>파일 업로드 서비스</b>
 * <p>
 *   비즈니스적인 요구사항을 고려하여 파일 업로드를 허용/반려 해주는 서비스
 * </p>
 *
 * @author 김동균
 * @since 2026-02-08
 */
@Service
@RequiredArgsConstructor
public class FileUploadService {

	private static final int MAX_IMAGE_UPLOAD_COUNT = 10;
	private static final int MAX_DOCUMENT_UPLOAD_COUNT = 1;

	private final TempExpenseMetaRepository tempExpenseMetaRepository;
	private final FileRepository fileRepository;
	private final TemporaryExpenseRepository temporaryExpenseRepository;
	private final TempExpenseMediaAccessService tempExpenseMediaAccessService;

	@Value("${app.media.presigned-put-expiration-seconds:300}")
	private int presignedPutExpirationSeconds;

	/**
	 * Presigned URL 생성 및 메타데이터 저장
	 */
	@Transactional
	public FileUploadResult createPresignedUrl(
			Long accountBookId,
			String fileName,
			String mimeType,
			UploadType uploadType,
			Long tempExpenseMetaId) {

		// Presigned URL 발급 단에서 Meta 를 찾아오는 함수
		TempExpenseMeta savedMeta = resolveMeta(accountBookId, tempExpenseMetaId);
		FileType fileType = resolveFileType(mimeType, uploadType);

		// 비즈니스 로직으로 두는 제한에 어긋나는지 확인하는 함수
		validateFileGroupingPolicy(savedMeta.getTempExpenseMetaId(), fileType);

		// 2. S3 Presigned URL 생성
		PresignedUrlResult s3Response =
				tempExpenseMediaAccessService.issueUploadPath(accountBookId, mimeType);

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
				presignedPutExpirationSeconds);
	}

	private FileType resolveFileType(String mimeType, UploadType uploadType) {
		if (uploadType == null) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
		}
		MediaContentType mediaContentType =
				MediaContentType.fromMimeType(mimeType)
						.orElseThrow(() -> new BusinessException(ErrorCode.UNSUPPORTED_MEDIA_TYPE));

		return switch (uploadType) {
			case IMAGE -> {
				if (!mediaContentType.getMimeType().startsWith("image/")) {
					throw new BusinessException(ErrorCode.TEMP_EXPENSE_INVALID_FILE_TYPE);
				}
				yield FileType.IMAGE;
			}
			case DOCS -> {
				if (mediaContentType == MediaContentType.CSV) {
					yield FileType.CSV;
				}
				if (mediaContentType == MediaContentType.XLS
						|| mediaContentType == MediaContentType.XLSX) {
					yield FileType.EXCEL;
				}
				throw new BusinessException(ErrorCode.TEMP_EXPENSE_INVALID_FILE_TYPE);
			}
		};
	}

	private TempExpenseMeta resolveMeta(Long accountBookId, Long tempExpenseMetaId) {
		// tempExpenseMetaId 가 null 일 경우 새로운 시작임을 뜻함
		if (tempExpenseMetaId == null) {
			TempExpenseMeta meta = TempExpenseMeta.builder().accountBookId(accountBookId).build();
			return tempExpenseMetaRepository.save(meta);
		}

		// ID 가 들어오면, 해당 ID에 새로운 이미지를 추가하는 경우임.
		TempExpenseMeta meta =
				tempExpenseMetaRepository
						.findById(tempExpenseMetaId)
						.orElseThrow(
								() -> new BusinessException(ErrorCode.TEMP_EXPENSE_META_NOT_FOUND));

		// 가계부 내의 Meta ID 가 아니면 반려함
		if (!meta.getAccountBookId().equals(accountBookId)) {
			throw new BusinessException(ErrorCode.TEMP_EXPENSE_SCOPE_MISMATCH);
		}

		return meta;
	}

	private void validateFileGroupingPolicy(Long tempExpenseMetaId, FileType fileType) {
		// 자바의 File 이 아님에 주의
		List<File> existingFiles = fileRepository.findByTempExpenseMetaId(tempExpenseMetaId);

		if (existingFiles.isEmpty()) {
			return;
		}

		FileType existingType = existingFiles.get(0).getFileType();
		if (existingType != fileType) {
			throw new BusinessException(ErrorCode.TEMP_EXPENSE_INVALID_FILE_TYPE);
		}

		if (fileType != FileType.IMAGE) {
			// CSV/EXCEL은 메타당 단일 파일만 허용하기 때문에, validation 에서 어긋납니다.
			// 해당 로직은 기존 메타에 파일을 추가하는 경우이기 때문에, 이렇게 수행됩니다.
			if (existingFiles.size() >= MAX_DOCUMENT_UPLOAD_COUNT) {
				throw new BusinessException(ErrorCode.TEMP_EXPENSE_PARSE_FILE_LIMIT_EXCEEDED);
			}
		}

		// 이미지 업로드 개수 제한
		if (existingFiles.size() >= MAX_IMAGE_UPLOAD_COUNT) {
			throw new BusinessException(ErrorCode.TEMP_EXPENSE_PARSE_FILE_LIMIT_EXCEEDED);
		}
	}

	@Transactional
	public void deleteMeta(Long accountBookId, Long tempExpenseMetaId) {
		TempExpenseMeta meta =
				tempExpenseMetaRepository
						.findById(tempExpenseMetaId)
						.orElseThrow(
								() -> new BusinessException(ErrorCode.TEMP_EXPENSE_META_NOT_FOUND));

		if (!meta.getAccountBookId().equals(accountBookId)) {
			throw new BusinessException(ErrorCode.TEMP_EXPENSE_SCOPE_MISMATCH);
		}

		// Meta 와 연결된 임시 지출내역 임시지출 내역 / 파일 정보 / 마지막으로 메타 자체 삭제해줍니다.
		temporaryExpenseRepository.deleteByTempExpenseMetaId(tempExpenseMetaId);
		fileRepository.deleteByTempExpenseMetaId(tempExpenseMetaId);
		tempExpenseMetaRepository.delete(meta);
	}
}
