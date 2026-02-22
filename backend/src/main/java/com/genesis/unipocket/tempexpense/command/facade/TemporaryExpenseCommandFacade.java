package com.genesis.unipocket.tempexpense.command.facade;

import com.genesis.unipocket.tempexpense.command.application.FileUploadService;
import com.genesis.unipocket.tempexpense.command.application.TempExpenseFileService;
import com.genesis.unipocket.tempexpense.command.application.TemporaryExpenseConversionService;
import com.genesis.unipocket.tempexpense.command.application.parsing.TemporaryExpenseParsingService;
import com.genesis.unipocket.tempexpense.command.application.result.ConfirmStartResult;
import com.genesis.unipocket.tempexpense.command.application.result.FileUploadResult;
import com.genesis.unipocket.tempexpense.command.application.result.ParseStartResult;
import com.genesis.unipocket.tempexpense.command.presentation.request.PresignedUrlRequest.UploadType;
import com.genesis.unipocket.tempexpense.command.presentation.request.TemporaryExpenseMetaBulkUpdateRequest;
import com.genesis.unipocket.tempexpense.command.presentation.response.TemporaryExpenseMetaBulkUpdateResponse;
import com.genesis.unipocket.tempexpense.common.facade.port.AccountBookOwnershipValidator;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class TemporaryExpenseCommandFacade {

	private final FileUploadService fileUploadService;
	private final TemporaryExpenseParsingService temporaryExpenseParsingService;
	private final TemporaryExpenseConversionService temporaryExpenseConversionService;
	private final TempExpenseFileService tempExpenseFileService;
	private final AccountBookOwnershipValidator accountBookOwnershipValidator;

	public FileUploadResult createPresignedUrl(
			Long accountBookId,
			String fileName,
			String mimeType,
			UploadType uploadType,
			Long tempExpenseMetaId,
			UUID userId) {
		validateOwnership(accountBookId, userId);
		return fileUploadService.createPresignedUrl(
				accountBookId, fileName, mimeType, uploadType, tempExpenseMetaId);
	}

	public ParseStartResult startParseAsync(
			Long accountBookId, Long tempExpenseMetaId, List<String> s3Keys, UUID userId) {
		validateOwnership(accountBookId, userId);
		return temporaryExpenseParsingService.startParseAsync(
				accountBookId, tempExpenseMetaId, s3Keys);
	}

	public ConfirmStartResult confirm(Long accountBookId, Long tempExpenseMetaId, UUID userId) {

		validateOwnership(accountBookId, userId);

		return temporaryExpenseConversionService.startConfirmAsync(
				accountBookId, tempExpenseMetaId);
	}

	public void deleteMeta(Long accountBookId, Long tempExpenseMetaId, UUID userId) {
		validateOwnership(accountBookId, userId);
		fileUploadService.deleteMeta(accountBookId, tempExpenseMetaId);
	}

	public TemporaryExpenseMetaBulkUpdateResponse updateTemporaryExpensesByFile(
			Long accountBookId,
			Long tempExpenseMetaId,
			Long fileId,
			TemporaryExpenseMetaBulkUpdateRequest request,
			UUID userId) {

		validateOwnership(accountBookId, userId);

		return TemporaryExpenseMetaBulkUpdateResponse.from(
				tempExpenseFileService.updateByFile(
						accountBookId, tempExpenseMetaId, fileId, request));
	}

	public void deleteTemporaryExpenseByFile(
			Long accountBookId,
			Long tempExpenseMetaId,
			Long fileId,
			Long tempExpenseId,
			UUID userId) {
		validateOwnership(accountBookId, userId);
		tempExpenseFileService.deleteByFile(
				accountBookId, tempExpenseMetaId, fileId, tempExpenseId);
	}

	private void validateOwnership(Long accountBookId, UUID userId) {
		accountBookOwnershipValidator.validateOwnership(accountBookId, userId.toString());
	}
}
