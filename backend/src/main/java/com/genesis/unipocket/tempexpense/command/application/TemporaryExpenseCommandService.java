package com.genesis.unipocket.tempexpense.command.application;

import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.tempexpense.command.application.command.TemporaryExpenseUpdateCommand;
import com.genesis.unipocket.tempexpense.command.application.result.TemporaryExpenseResult;
import com.genesis.unipocket.tempexpense.command.facade.port.AccountBookRateInfoProvider;
import com.genesis.unipocket.tempexpense.command.persistence.entity.TempExpenseMeta;
import com.genesis.unipocket.tempexpense.command.persistence.entity.TemporaryExpense;
import com.genesis.unipocket.tempexpense.command.persistence.repository.TempExpenseMetaRepository;
import com.genesis.unipocket.tempexpense.command.persistence.repository.TemporaryExpenseRepository;
import com.genesis.unipocket.tempexpense.common.enums.TemporaryExpenseStatus;
import com.genesis.unipocket.tempexpense.common.validation.TemporaryExpenseValidator;
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
	private final TemporaryExpenseValidator temporaryExpenseValidator;

	public TemporaryExpense findById(Long tempExpenseId) {
		return temporaryExpenseRepository
				.findById(tempExpenseId)
				.orElseThrow(() -> new BusinessException(ErrorCode.TEMP_EXPENSE_NOT_FOUND));
	}

	@Transactional
	public TemporaryExpenseResult updateTemporaryExpense(
			Long tempExpenseId, TemporaryExpenseUpdateCommand command) {
		TemporaryExpense entity = findById(tempExpenseId);

		String resolvedMerchantName = valueOr(command.merchantName(), entity.getMerchantName());
		var resolvedCategory = valueOr(command.category(), entity.getCategory());
		var resolvedLocalCountryCode =
				valueOr(command.localCountryCode(), entity.getLocalCountryCode());
		var resolvedLocalCurrencyAmount =
				valueOr(command.localCurrencyAmount(), entity.getLocalCurrencyAmount());
		var resolvedBaseCountryCode = resolveBaseCountryCode(command, entity);
		var resolvedBaseCurrencyAmount =
				valueOr(command.baseCurrencyAmount(), entity.getBaseCurrencyAmount());
		String resolvedPaymentsMethod =
				valueOr(command.paymentsMethod(), entity.getPaymentsMethod());
		String resolvedMemo = valueOr(command.memo(), entity.getMemo());
		var resolvedOccurredAt = valueOr(command.occurredAt(), entity.getOccurredAt());
		String resolvedCardLastFourDigits =
				valueOr(command.cardLastFourDigits(), entity.getCardLastFourDigits());

		TemporaryExpenseStatus resolvedStatus =
				temporaryExpenseValidator.resolveStatus(
						entity.getStatus(),
						resolvedMerchantName,
						resolvedCategory,
						resolvedLocalCountryCode,
						resolvedLocalCurrencyAmount,
						resolvedBaseCountryCode,
						resolvedOccurredAt);

		TemporaryExpense updated =
				TemporaryExpense.builder()
						.tempExpenseId(entity.getTempExpenseId())
						.tempExpenseMetaId(entity.getTempExpenseMetaId())
						.fileId(entity.getFileId())
						.merchantName(resolvedMerchantName)
						.category(resolvedCategory)
						.localCountryCode(resolvedLocalCountryCode)
						.localCurrencyAmount(resolvedLocalCurrencyAmount)
						.baseCountryCode(resolvedBaseCountryCode)
						.baseCurrencyAmount(resolvedBaseCurrencyAmount)
						.exchangeRate(entity.getExchangeRate())
						.paymentsMethod(resolvedPaymentsMethod)
						.memo(resolvedMemo)
						.occurredAt(resolvedOccurredAt)
						.status(resolvedStatus)
						.cardLastFourDigits(resolvedCardLastFourDigits)
						.approvalNumber(entity.getApprovalNumber())
						.build();

		return TemporaryExpenseResult.from(temporaryExpenseRepository.save(updated));
	}

	private <T> T valueOr(T candidate, T fallback) {
		return candidate != null ? candidate : fallback;
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
