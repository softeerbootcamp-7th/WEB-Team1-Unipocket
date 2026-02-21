package com.genesis.unipocket.tempexpense.command.application;

import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.global.infrastructure.MediaContentType;
import com.genesis.unipocket.media.command.application.result.PresignedUrlResult;
import com.genesis.unipocket.tempexpense.command.application.result.FileUploadResult;
import com.genesis.unipocket.tempexpense.command.facade.port.TempExpenseMediaAccessService;
import com.genesis.unipocket.tempexpense.command.facade.provide.TemporaryExpenseScopeValidationProvider;
import com.genesis.unipocket.tempexpense.command.persistence.entity.File;
import com.genesis.unipocket.tempexpense.command.persistence.entity.File.FileType;
import com.genesis.unipocket.tempexpense.command.persistence.entity.TempExpenseMeta;
import com.genesis.unipocket.tempexpense.command.persistence.repository.FileRepository;
import com.genesis.unipocket.tempexpense.command.persistence.repository.TempExpenseMetaRepository;
import com.genesis.unipocket.tempexpense.command.persistence.repository.TemporaryExpenseRepository;
import com.genesis.unipocket.tempexpense.command.presentation.request.PresignedUrlRequest.UploadType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FileUploadService {

	private static final int MAX_IMAGE_UPLOAD_COUNT = 10;
	private static final int MAX_DOCUMENT_UPLOAD_COUNT = 1;

	private final TempExpenseMetaRepository tempExpenseMetaRepository;
	private final FileRepository fileRepository;
	private final TemporaryExpenseRepository temporaryExpenseRepository;
	private final TempExpenseMediaAccessService tempExpenseMediaAccessService;
	private final TemporaryExpenseScopeValidationProvider temporaryExpenseScopeValidator;

	@Value("${app.media.presigned-put-expiration-seconds:300}")
	private int presignedPutExpirationSeconds;

	@Transactional
	public FileUploadResult createPresignedUrl(
			Long accountBookId,
			String fileName,
			String mimeType,
			UploadType uploadType,
			Long tempExpenseMetaId) {
		TempExpenseMeta savedMeta = resolveMeta(accountBookId, tempExpenseMetaId);
		FileType fileType = resolveFileType(mimeType, uploadType);
		validateFileGroupingPolicy(savedMeta.getTempExpenseMetaId(), fileType);

		PresignedUrlResult s3Response =
				tempExpenseMediaAccessService.issueUploadPath(accountBookId, mimeType);

		File file =
				File.builder()
						.tempExpenseMetaId(savedMeta.getTempExpenseMetaId())
						.fileType(fileType)
						.s3Key(s3Response.imageKey())
						.build();
		fileRepository.save(file);

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
		if (tempExpenseMetaId == null) {
			TempExpenseMeta meta = TempExpenseMeta.builder().accountBookId(accountBookId).build();
			return tempExpenseMetaRepository.save(meta);
		}

		return temporaryExpenseScopeValidator.validateMetaScope(accountBookId, tempExpenseMetaId);
	}

	private void validateFileGroupingPolicy(Long tempExpenseMetaId, FileType fileType) {
		List<File> existingFiles = fileRepository.findByTempExpenseMetaId(tempExpenseMetaId);

		if (existingFiles.isEmpty()) {
			return;
		}

		FileType existingType = existingFiles.get(0).getFileType();
		if (existingType != fileType) {
			throw new BusinessException(ErrorCode.TEMP_EXPENSE_INVALID_FILE_TYPE);
		}

		if (fileType != FileType.IMAGE) {
			if (existingFiles.size() >= MAX_DOCUMENT_UPLOAD_COUNT) {
				throw new BusinessException(ErrorCode.TEMP_EXPENSE_PARSE_FILE_LIMIT_EXCEEDED);
			}
		}

		if (existingFiles.size() >= MAX_IMAGE_UPLOAD_COUNT) {
			throw new BusinessException(ErrorCode.TEMP_EXPENSE_PARSE_FILE_LIMIT_EXCEEDED);
		}
	}

	@Transactional
	public void deleteMeta(Long accountBookId, Long tempExpenseMetaId) {
		TempExpenseMeta meta =
				temporaryExpenseScopeValidator.validateMetaScope(accountBookId, tempExpenseMetaId);

		temporaryExpenseRepository.deleteByTempExpenseMetaId(tempExpenseMetaId);
		fileRepository.deleteByTempExpenseMetaId(tempExpenseMetaId);
		tempExpenseMetaRepository.delete(meta);
	}
}
