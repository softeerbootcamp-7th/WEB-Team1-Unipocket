package com.genesis.unipocket.expense.command.facade;

import com.genesis.unipocket.expense.command.application.TemporaryExpenseCommandService;
import com.genesis.unipocket.expense.command.application.command.TemporaryExpenseUpdateCommand;
import com.genesis.unipocket.expense.command.application.result.TemporaryExpenseResult;
import com.genesis.unipocket.expense.command.persistence.entity.expense.File;
import com.genesis.unipocket.expense.command.persistence.entity.expense.TempExpenseMeta;
import com.genesis.unipocket.expense.command.persistence.entity.expense.TemporaryExpense;
import com.genesis.unipocket.expense.command.persistence.repository.FileRepository;
import com.genesis.unipocket.expense.command.persistence.repository.TempExpenseMetaRepository;
import com.genesis.unipocket.expense.command.presentation.request.TemporaryExpenseUpdateRequest;
import com.genesis.unipocket.expense.common.port.AccountBookOwnershipValidator;
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
	private final FileRepository fileRepository;
	private final TempExpenseMetaRepository tempExpenseMetaRepository;
	private final AccountBookOwnershipValidator accountBookOwnershipValidator;

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
		accountBookOwnershipValidator.validateOwnership(accountBookId, userId.toString());

		temporaryExpenseService.deleteTemporaryExpense(tempExpenseId);
	}

	/**
	 * Helper method to get accountBookId from temporary expense
	 */
	private Long getAccountBookIdFromTempExpense(TemporaryExpense tempExpense) {
		// Get accountBookId via file → meta
		File file =
				fileRepository
						.findById(tempExpense.getFileId())
						.orElseThrow(() -> new IllegalArgumentException("파일을 찾을 수 없습니다."));
		TempExpenseMeta meta =
				tempExpenseMetaRepository
						.findById(file.getTempExpenseMetaId())
						.orElseThrow(() -> new IllegalArgumentException("메타데이터를 찾을 수 없습니다."));
		return meta.getAccountBookId();
	}
}
