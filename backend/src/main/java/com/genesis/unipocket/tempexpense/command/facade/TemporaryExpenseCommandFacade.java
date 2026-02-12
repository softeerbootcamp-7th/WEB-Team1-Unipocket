package com.genesis.unipocket.tempexpense.command.facade;

import com.genesis.unipocket.expense.command.persistence.entity.ExpenseEntity;
import com.genesis.unipocket.tempexpense.command.application.FileUploadService;
import com.genesis.unipocket.tempexpense.command.application.TemporaryExpenseCommandService;
import com.genesis.unipocket.tempexpense.command.application.TemporaryExpenseConversionService;
import com.genesis.unipocket.tempexpense.command.application.TemporaryExpenseParsingService;
import com.genesis.unipocket.tempexpense.command.application.command.TemporaryExpenseUpdateCommand;
import com.genesis.unipocket.tempexpense.command.application.result.BatchConversionResult;
import com.genesis.unipocket.tempexpense.command.application.result.FileRegisterResult;
import com.genesis.unipocket.tempexpense.command.application.result.FileUploadResult;
import com.genesis.unipocket.tempexpense.command.application.result.ParsingResult;
import com.genesis.unipocket.tempexpense.command.application.result.TemporaryExpenseResult;
import com.genesis.unipocket.tempexpense.command.facade.port.AccountBookOwnershipValidator;
import com.genesis.unipocket.tempexpense.command.persistence.entity.File.FileType;
import com.genesis.unipocket.tempexpense.command.persistence.entity.TempExpenseMeta;
import com.genesis.unipocket.tempexpense.command.persistence.entity.TemporaryExpense;
import com.genesis.unipocket.tempexpense.command.persistence.repository.TempExpenseMetaRepository;
import com.genesis.unipocket.tempexpense.command.presentation.request.TemporaryExpenseUpdateRequest;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * <b>임시지출내역 Facade 클래스</b>
 * <p>
 * 임시지출내역 도메인에 대한 요청 처리
 *
 * @author 김동균
 * @since 2026-02-08
 */
@Service
@AllArgsConstructor
public class TemporaryExpenseCommandFacade {

	private final TemporaryExpenseCommandService temporaryExpenseService;
	private final FileUploadService fileUploadService;
	private final TemporaryExpenseParsingService temporaryExpenseParsingService;
	private final TemporaryExpenseConversionService temporaryExpenseConversionService;
	private final TempExpenseMetaRepository tempExpenseMetaRepository;
	private final AccountBookOwnershipValidator accountBookOwnershipValidator;

	public FileUploadResult createPresignedUrl(
			Long accountBookId, String fileName, FileType fileType, UUID userId) {
		validateOwnership(accountBookId, userId);
		return fileUploadService.createPresignedUrl(accountBookId, fileName, fileType);
	}

	public FileRegisterResult registerUploadedFile(
			Long accountBookId, String s3Key, FileType fileType, UUID userId) {
		validateOwnership(accountBookId, userId);
		return fileUploadService.registerUploadedFile(accountBookId, s3Key, fileType);
	}

	public String startParseAsync(Long accountBookId, List<String> s3Keys, UUID userId) {
		validateOwnership(accountBookId, userId);
		String taskId = java.util.UUID.randomUUID().toString();
		temporaryExpenseParsingService.parseBatchFilesAsync(accountBookId, s3Keys, taskId);
		return taskId;
	}

	public ParsingResult parseFile(Long accountBookId, String s3Key, UUID userId) {
		validateOwnership(accountBookId, userId);
		return temporaryExpenseParsingService.parseFile(accountBookId, s3Key);
	}

	public ExpenseEntity convertToExpense(Long accountBookId, Long tempExpenseId, UUID userId) {
		validateOwnership(accountBookId, userId);
		return temporaryExpenseConversionService.convertToExpense(accountBookId, tempExpenseId);
	}

	public BatchConversionResult convertBatch(
			Long accountBookId, List<Long> tempExpenseIds, UUID userId) {
		validateOwnership(accountBookId, userId);
		return temporaryExpenseConversionService.convertBatch(accountBookId, tempExpenseIds);
	}

	public BatchConversionResult confirmByMeta(
			Long accountBookId, Long tempExpenseMetaId, List<Long> tempExpenseIds, UUID userId) {
		validateOwnership(accountBookId, userId);
		return temporaryExpenseConversionService.convertMeta(
				accountBookId, tempExpenseMetaId, tempExpenseIds);
	}

	public void deleteMeta(Long accountBookId, Long tempExpenseMetaId, UUID userId) {
		validateOwnership(accountBookId, userId);
		fileUploadService.deleteMeta(accountBookId, tempExpenseMetaId);
	}

	/**
	 * 임시지출내역 수정
	 */
	public TemporaryExpenseResult updateTemporaryExpense(
			Long tempExpenseId, TemporaryExpenseUpdateRequest request, UUID userId) {
		// Validate ownership
		TemporaryExpense tempExpense = temporaryExpenseService.findById(tempExpenseId);
		Long accountBookId = getAccountBookIdFromTempExpense(tempExpense);
		accountBookOwnershipValidator.validateOwnership(accountBookId, userId.toString());

		TemporaryExpenseResult updated =
				temporaryExpenseService.updateTemporaryExpense(
						tempExpenseId, TemporaryExpenseUpdateCommand.from(request));
		return updated;
	}

	/**
	 * 임시지출내역 삭제
	 */
	public void deleteTemporaryExpense(Long tempExpenseId, UUID userId) {
		// Validate ownership
		TemporaryExpense tempExpense = temporaryExpenseService.findById(tempExpenseId);
		Long accountBookId = getAccountBookIdFromTempExpense(tempExpense);
		validateOwnership(accountBookId, userId);

		temporaryExpenseService.deleteTemporaryExpense(tempExpenseId);
	}

	private void validateOwnership(Long accountBookId, UUID userId) {
		accountBookOwnershipValidator.validateOwnership(accountBookId, userId.toString());
	}

	/**
	 * Helper method to get accountBookId from temporary expense
	 */
	private Long getAccountBookIdFromTempExpense(TemporaryExpense tempExpense) {
		TempExpenseMeta meta =
				tempExpenseMetaRepository
						.findById(tempExpense.getTempExpenseMetaId())
						.orElseThrow(() -> new IllegalArgumentException("메타데이터를 찾을 수 없습니다."));
		return meta.getAccountBookId();
	}
}
