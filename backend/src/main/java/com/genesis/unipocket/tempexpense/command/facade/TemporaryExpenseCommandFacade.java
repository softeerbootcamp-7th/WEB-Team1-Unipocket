package com.genesis.unipocket.tempexpense.command.facade;

import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.tempexpense.command.application.FileUploadService;
import com.genesis.unipocket.tempexpense.command.application.TemporaryExpenseCommandService;
import com.genesis.unipocket.tempexpense.command.application.TemporaryExpenseConversionService;
import com.genesis.unipocket.tempexpense.command.application.command.TemporaryExpenseUpdateCommand;
import com.genesis.unipocket.tempexpense.command.application.parsing.TemporaryExpenseParsingService;
import com.genesis.unipocket.tempexpense.command.application.result.ConfirmStartResult;
import com.genesis.unipocket.tempexpense.command.application.result.FileUploadResult;
import com.genesis.unipocket.tempexpense.command.application.result.ParseStartResult;
import com.genesis.unipocket.tempexpense.command.application.result.TemporaryExpenseResult;
import com.genesis.unipocket.tempexpense.command.facade.provide.TemporaryExpenseScopeValidationProvider;
import com.genesis.unipocket.tempexpense.command.presentation.request.PresignedUrlRequest.UploadType;
import com.genesis.unipocket.tempexpense.command.presentation.request.TemporaryExpensePatchRequest;
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
	private final TemporaryExpenseCommandService temporaryExpenseCommandService;
	private final AccountBookOwnershipValidator accountBookOwnershipValidator;
	private final TemporaryExpenseScopeValidationProvider temporaryExpenseScopeValidationProvider;

	public FileUploadResult createPresignedUrl(
			Long accountBookId,
			String fileName,
			String mimeType,
			UploadType uploadType,
			Long tempExpenseMetaId,
			UUID userId) {
		validateOwnership(accountBookId, userId);
		if (tempExpenseMetaId != null) {
			temporaryExpenseScopeValidationProvider.validateMetaScope(
					accountBookId, tempExpenseMetaId);
		}
		return fileUploadService.createPresignedUrl(
				accountBookId, fileName, mimeType, uploadType, tempExpenseMetaId);
	}

	public ParseStartResult startParseTask(
			Long accountBookId, Long tempExpenseMetaId, List<String> s3Keys, UUID userId) {
		validateOwnership(accountBookId, userId);
		if (tempExpenseMetaId == null) {
			throw new BusinessException(ErrorCode.TEMP_EXPENSE_META_NOT_FOUND);
		}
		temporaryExpenseScopeValidationProvider.validateMetaScope(accountBookId, tempExpenseMetaId);
		return temporaryExpenseParsingService.startParseTask(tempExpenseMetaId, s3Keys);
	}

	public ConfirmStartResult convertMetaToExpenses(
			Long accountBookId, Long tempExpenseMetaId, UUID userId) {
		validateOwnership(accountBookId, userId);
		temporaryExpenseScopeValidationProvider.validateMetaScope(accountBookId, tempExpenseMetaId);
		return temporaryExpenseConversionService.convertMetaToExpenses(
				accountBookId, tempExpenseMetaId);
	}

	public void deleteMeta(Long accountBookId, Long tempExpenseMetaId, UUID userId) {
		validateOwnership(accountBookId, userId);
		temporaryExpenseScopeValidationProvider.validateMetaScope(accountBookId, tempExpenseMetaId);
		fileUploadService.deleteMeta(tempExpenseMetaId);
	}

	public TemporaryExpenseResult updateTemporaryExpense(
			Long accountBookId,
			Long tempExpenseMetaId,
			Long fileId,
			Long tempExpenseId,
			TemporaryExpensePatchRequest request,
			UUID userId) {
		validateOwnership(accountBookId, userId);
		temporaryExpenseScopeValidationProvider.validateFileScope(
				accountBookId, tempExpenseMetaId, fileId);
		var target =
				temporaryExpenseScopeValidationProvider.validateTempExpenseScope(
						tempExpenseMetaId, fileId, tempExpenseId);
		return temporaryExpenseCommandService.updateTemporaryExpense(
				accountBookId, target, TemporaryExpenseUpdateCommand.from(request));
	}

	public void deleteTemporaryExpenseByFile(
			Long accountBookId,
			Long tempExpenseMetaId,
			Long fileId,
			Long tempExpenseId,
			UUID userId) {
		validateOwnership(accountBookId, userId);
		temporaryExpenseScopeValidationProvider.validateFileScope(
				accountBookId, tempExpenseMetaId, fileId);
		var target =
				temporaryExpenseScopeValidationProvider.validateTempExpenseScope(
						tempExpenseMetaId, fileId, tempExpenseId);
		temporaryExpenseCommandService.deleteTemporaryExpense(target);
	}

	private void validateOwnership(Long accountBookId, UUID userId) {
		accountBookOwnershipValidator.validateOwnership(accountBookId, userId.toString());
	}
}
