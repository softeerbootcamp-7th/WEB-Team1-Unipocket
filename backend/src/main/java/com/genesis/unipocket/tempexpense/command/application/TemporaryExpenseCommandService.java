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
public class TemporaryExpenseCommandService {

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

		var resolvedBaseCountryCode = resolveBaseCountryCode(command, entity);
		TempExpensePatch patch =
				TempExpensePatch.from(
						command.merchantName(),
						command.category(),
						command.localCountryCode(),
						command.localCurrencyAmount(),
						resolvedBaseCountryCode,
						command.baseCurrencyAmount(),
						null,
						command.paymentsMethod(),
						command.memo(),
						command.occurredAt(),
						command.cardLastFourDigits(),
						null);
		entity.applyPatch(patch, tempExpenseStatusPolicy);
		return TemporaryExpenseResult.from(temporaryExpenseRepository.save(entity));
	}

	private CurrencyCode resolveBaseCountryCode(
			TemporaryExpenseUpdateCommand command, TemporaryExpense entity) {
		if (command.baseCountryCode() != null) {
			return command.baseCountryCode();
		}
		if (entity.getBaseCountryCode() != null) {
			return entity.getBaseCountryCode();
		}
		if (command.baseCurrencyAmount() != null) {
			TempExpenseMeta meta =
					tempExpenseMetaRepository
							.findById(entity.getTempExpenseMetaId())
							.orElseThrow(
									() ->
											new BusinessException(
													ErrorCode.TEMP_EXPENSE_META_NOT_FOUND));
			return accountBookRateInfoProvider
					.getRateInfo(meta.getAccountBookId())
					.baseCurrencyCode();
		}
		return null;
	}

	@Transactional
	public void deleteTemporaryExpense(Long tempExpenseId) {
		TemporaryExpense entity = findById(tempExpenseId);
		temporaryExpenseRepository.delete(entity);
	}
}
