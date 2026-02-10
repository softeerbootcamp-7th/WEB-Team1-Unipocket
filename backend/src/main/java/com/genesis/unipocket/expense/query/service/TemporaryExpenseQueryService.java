package com.genesis.unipocket.expense.query.service;

import com.genesis.unipocket.expense.command.persistence.entity.expense.File;
import com.genesis.unipocket.expense.command.persistence.entity.expense.TempExpenseMeta;
import com.genesis.unipocket.expense.command.persistence.entity.expense.TemporaryExpense;
import com.genesis.unipocket.expense.command.persistence.repository.FileRepository;
import com.genesis.unipocket.expense.command.persistence.repository.TempExpenseMetaRepository;
import com.genesis.unipocket.expense.command.persistence.repository.TemporaryExpenseRepository;
import com.genesis.unipocket.expense.common.validator.AccountBookOwnershipValidator;
import com.genesis.unipocket.expense.query.presentation.response.TemporaryExpenseResponse;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <b>임시지출내역 조회 서비스</b>
 *
 * @author 김동균
 * @since 2026-02-10
 */
@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class TemporaryExpenseQueryService {

	private final TemporaryExpenseRepository temporaryExpenseRepository;
	private final FileRepository fileRepository;
	private final TempExpenseMetaRepository tempExpenseMetaRepository;
	private final AccountBookOwnershipValidator accountBookOwnershipValidator;

	public List<TemporaryExpenseResponse> getTemporaryExpenses(
			Long accountBookId, TemporaryExpense.TemporaryExpenseStatus status, UUID userId) {
		accountBookOwnershipValidator.validateOwnership(accountBookId, userId.toString());

		List<TemporaryExpense> entities =
				status != null
						? temporaryExpenseRepository.findByAccountBookIdAndStatus(
								accountBookId, status)
						: temporaryExpenseRepository.findByAccountBookId(accountBookId);

		return TemporaryExpenseResponse.fromList(entities);
	}

	public TemporaryExpenseResponse getTemporaryExpense(Long tempExpenseId, UUID userId) {
		TemporaryExpense tempExpense = findById(tempExpenseId);
		Long accountBookId = getAccountBookIdFromTempExpense(tempExpense);
		accountBookOwnershipValidator.validateOwnership(accountBookId, userId.toString());

		return TemporaryExpenseResponse.from(tempExpense);
	}

	private TemporaryExpense findById(Long tempExpenseId) {
		return temporaryExpenseRepository
				.findById(tempExpenseId)
				.orElseThrow(
						() ->
								new IllegalArgumentException(
										"임시지출내역을 찾을 수 없습니다. ID: " + tempExpenseId));
	}

	private Long getAccountBookIdFromTempExpense(TemporaryExpense tempExpense) {
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
