package com.genesis.unipocket.tempexpense.command.application;

import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.tempexpense.command.application.command.TemporaryExpenseUpdateCommand;
import com.genesis.unipocket.tempexpense.command.application.result.TemporaryExpenseMetaBulkUpdateResult;
import com.genesis.unipocket.tempexpense.command.application.result.TemporaryExpenseResult;
import com.genesis.unipocket.tempexpense.command.facade.port.AccountBookRateInfoProvider;
import com.genesis.unipocket.tempexpense.command.facade.provide.TemporaryExpenseScopeValidationProvider;
import com.genesis.unipocket.tempexpense.command.persistence.entity.TemporaryExpense;
import com.genesis.unipocket.tempexpense.command.persistence.repository.TemporaryExpenseRepository;
import com.genesis.unipocket.tempexpense.command.presentation.request.TemporaryExpenseMetaBulkUpdateRequest;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TempExpenseFileService {

	private final TemporaryExpenseRepository temporaryExpenseRepository;
	private final AccountBookRateInfoProvider accountBookRateInfoProvider;
	private final TemporaryExpenseScopeValidationProvider temporaryExpenseScopeValidator;
	private final TempExpenseService tempExpenseService;

	@Transactional
	public TemporaryExpenseMetaBulkUpdateResult updateByFile(
			Long accountBookId,
			Long tempExpenseMetaId,
			Long fileId,
			TemporaryExpenseMetaBulkUpdateRequest request) {
		temporaryExpenseScopeValidator.validateMetaScope(accountBookId, tempExpenseMetaId);

		List<TemporaryExpenseMetaBulkUpdateResult.ItemResult> results = new ArrayList<>();
		int successCount = 0;

		CurrencyCode defaultBaseCurrencyCode =
				accountBookRateInfoProvider.getRateInfo(accountBookId).baseCurrencyCode();

		for (var item : request.items()) {
			try {
				TemporaryExpense target =
						temporaryExpenseRepository
								.findScopedById(item.tempExpenseId(), tempExpenseMetaId, fileId)
								.orElseThrow(
										() ->
												new BusinessException(
														ErrorCode.TEMP_EXPENSE_NOT_FOUND));

				TemporaryExpenseResult updated =
						tempExpenseService.updateTemporaryExpense(
								target,
								TemporaryExpenseUpdateCommand.from(item),
								defaultBaseCurrencyCode);

				successCount++;
				results.add(
						new TemporaryExpenseMetaBulkUpdateResult.ItemResult(
								item.tempExpenseId(), "SUCCESS", null, updated));
			} catch (BusinessException e) {
				results.add(
						new TemporaryExpenseMetaBulkUpdateResult.ItemResult(
								item.tempExpenseId(), "FAILED", e.getMessage(), null));
			} catch (Exception e) {
				results.add(
						new TemporaryExpenseMetaBulkUpdateResult.ItemResult(
								item.tempExpenseId(),
								"FAILED",
								ErrorCode.INTERNAL_SERVER_ERROR.getMessage(),
								null));
			}
		}

		int totalRequested = request.items().size();
		return new TemporaryExpenseMetaBulkUpdateResult(
				totalRequested, successCount, totalRequested - successCount, results);
	}

	@Transactional
	public void deleteByFile(
			Long accountBookId, Long tempExpenseMetaId, Long fileId, Long tempExpenseId) {
		temporaryExpenseScopeValidator.validateMetaScope(accountBookId, tempExpenseMetaId);

		TemporaryExpense target =
				temporaryExpenseRepository
						.findScopedById(tempExpenseId, tempExpenseMetaId, fileId)
						.orElseThrow(() -> new BusinessException(ErrorCode.TEMP_EXPENSE_NOT_FOUND));

		temporaryExpenseRepository.delete(target);
	}
}
