package com.genesis.unipocket.tempexpense.command.application;

import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.tempexpense.command.application.command.TemporaryExpenseUpdateCommand;
import com.genesis.unipocket.tempexpense.command.application.result.TemporaryExpenseResult;
import com.genesis.unipocket.tempexpense.command.facade.port.AccountBookRateInfoProvider;
import com.genesis.unipocket.tempexpense.command.persistence.entity.TempExpenseMeta;
import com.genesis.unipocket.tempexpense.command.persistence.entity.TemporaryExpense;
import com.genesis.unipocket.tempexpense.command.persistence.entity.tempexpense.TempExpensePatch;
import com.genesis.unipocket.tempexpense.command.persistence.entity.tempexpense.TempExpenseStatusPolicy;
import com.genesis.unipocket.tempexpense.command.persistence.repository.TempExpenseMetaRepository;
import com.genesis.unipocket.tempexpense.command.persistence.repository.TemporaryExpenseRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class TempExpenseService {

	private final TemporaryExpenseRepository temporaryExpenseRepository;
	private final TempExpenseMetaRepository tempExpenseMetaRepository;
	private final AccountBookRateInfoProvider accountBookRateInfoProvider;
	private final TempExpenseStatusPolicy tempExpenseStatusPolicy;

	public TemporaryExpense findById(Long tempExpenseId) {
		return temporaryExpenseRepository
				.findById(tempExpenseId)
				.orElseThrow(() -> new BusinessException(ErrorCode.TEMP_EXPENSE_NOT_FOUND));
	}

	@Transactional
	public TemporaryExpenseResult updateTemporaryExpense(
			Long tempExpenseId, TemporaryExpenseUpdateCommand command) {
		TemporaryExpense entity = findById(tempExpenseId);
		return updateTemporaryExpense(entity, command, null);
	}

	TemporaryExpenseResult updateTemporaryExpense(
			TemporaryExpense entity,
			TemporaryExpenseUpdateCommand command,
			CurrencyCode defaultBaseCurrencyCode) {
		var amountInfo = entity.getAmountInfoOrEmpty();
		CurrencyCode fallbackBaseCurrencyCode =
				command.baseCountryCode() == null
								&& amountInfo.getBaseCurrencyCode() == null
								&& command.baseCurrencyAmount() != null
						? (defaultBaseCurrencyCode != null
								? defaultBaseCurrencyCode
								: defaultBaseCurrency(entity))
						: null;
		var resolvedBaseCountryCode =
				amountInfo.resolvePatchBaseCurrencyCode(
						command.baseCountryCode(),
						command.baseCurrencyAmount(),
						fallbackBaseCurrencyCode);
		TempExpensePatch patch = TempExpensePatch.from(command, resolvedBaseCountryCode);
		entity.applyPatch(patch, tempExpenseStatusPolicy);
		return TemporaryExpenseResult.from(temporaryExpenseRepository.save(entity));
	}

	private CurrencyCode defaultBaseCurrency(TemporaryExpense entity) {
		TempExpenseMeta meta =
				tempExpenseMetaRepository
						.findById(entity.getTempExpenseMetaId())
						.orElseThrow(
								() -> new BusinessException(ErrorCode.TEMP_EXPENSE_META_NOT_FOUND));
		return accountBookRateInfoProvider.getRateInfo(meta.getAccountBookId()).baseCurrencyCode();
	}

	@Transactional
	public void deleteTemporaryExpense(Long tempExpenseId) {
		TemporaryExpense entity = findById(tempExpenseId);
		temporaryExpenseRepository.delete(entity);
	}
}
