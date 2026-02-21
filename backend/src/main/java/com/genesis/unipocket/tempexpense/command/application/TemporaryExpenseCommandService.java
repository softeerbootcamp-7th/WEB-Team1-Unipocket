package com.genesis.unipocket.tempexpense.command.application;

import com.genesis.unipocket.expense.common.util.ExchangeAmountCalculator;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.tempexpense.command.application.command.TemporaryExpenseUpdateCommand;
import com.genesis.unipocket.tempexpense.command.application.dto.TemporaryExpenseExchangeComputation;
import com.genesis.unipocket.tempexpense.command.application.result.TemporaryExpenseResult;
import com.genesis.unipocket.tempexpense.command.facade.port.AccountBookRateInfoProvider;
import com.genesis.unipocket.tempexpense.command.facade.port.ExchangeRateProvider;
import com.genesis.unipocket.tempexpense.command.persistence.entity.TemporaryExpense;
import com.genesis.unipocket.tempexpense.command.persistence.repository.TemporaryExpenseRepository;
import com.genesis.unipocket.tempexpense.common.enums.TemporaryExpenseStatus;
import com.genesis.unipocket.tempexpense.common.validation.TemporaryExpenseValidator;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class TemporaryExpenseCommandService {

	private final TemporaryExpenseRepository temporaryExpenseRepository;
	private final AccountBookRateInfoProvider accountBookRateInfoProvider;
	private final ExchangeRateProvider exchangeRateProvider;
	private final TemporaryExpenseValidator temporaryExpenseValidator;

	@Transactional
	public TemporaryExpenseResult updateTemporaryExpense(
			Long accountBookId, TemporaryExpense entity, TemporaryExpenseUpdateCommand command) {
		CurrencyCode defaultBaseCurrencyCode =
				accountBookRateInfoProvider.getRateInfo(accountBookId).baseCurrencyCode();
		String resolvedMerchantName = valueOr(command.merchantName(), entity.getMerchantName());
		var resolvedCategory = valueOr(command.category(), entity.getCategory());
		var resolvedLocalCountryCode =
				valueOr(command.localCountryCode(), entity.getLocalCountryCode());
		var resolvedLocalCurrencyAmount =
				valueOr(command.localCurrencyAmount(), entity.getLocalCurrencyAmount());
		var resolvedBaseCountryCode =
				resolveBaseCountryCode(command, entity, defaultBaseCurrencyCode);
		String resolvedPaymentsMethod =
				valueOr(command.paymentsMethod(), entity.getPaymentsMethod());
		String resolvedMemo = valueOr(command.memo(), entity.getMemo());
		var resolvedOccurredAt = valueOr(command.occurredAt(), entity.getOccurredAt());
		String resolvedCardLastFourDigits =
				valueOr(command.cardLastFourDigits(), entity.getCardLastFourDigits());
		TemporaryExpenseExchangeComputation exchangeComputation =
				resolveExchangeComputation(
						entity,
						command,
						resolvedLocalCountryCode,
						resolvedLocalCurrencyAmount,
						resolvedBaseCountryCode,
						resolvedOccurredAt);

		BigDecimal resolvedBaseCurrencyAmount =
				resolveBaseCurrencyAmount(command, entity, exchangeComputation);
		BigDecimal resolvedExchangeRate =
				valueOr(exchangeComputation.exchangeRate(), entity.getExchangeRate());

		TemporaryExpenseStatus resolvedStatus =
				temporaryExpenseValidator.resolveStatus(
						resolvedMerchantName,
						resolvedCategory,
						resolvedLocalCountryCode,
						resolvedLocalCurrencyAmount,
						resolvedBaseCountryCode,
						resolvedBaseCurrencyAmount,
						resolvedExchangeRate,
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
						.exchangeRate(resolvedExchangeRate)
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
			TemporaryExpenseUpdateCommand command,
			TemporaryExpense entity,
			CurrencyCode defaultBaseCurrencyCode) {
		if (command.baseCountryCode() != null) {
			return command.baseCountryCode();
		}
		if (entity.getBaseCountryCode() != null) {
			return entity.getBaseCountryCode();
		}
		return defaultBaseCurrencyCode;
	}

	@Transactional
	public void deleteTemporaryExpense(TemporaryExpense entity) {
		temporaryExpenseRepository.delete(entity);
	}

	private BigDecimal resolveBaseCurrencyAmount(
			TemporaryExpenseUpdateCommand command,
			TemporaryExpense entity,
			TemporaryExpenseExchangeComputation exchangeComputation) {
		if (command.baseCurrencyAmount() != null) {
			return ExchangeAmountCalculator.scaleAmount(command.baseCurrencyAmount());
		}
		if (exchangeComputation.calculatedBaseAmount() != null) {
			return exchangeComputation.calculatedBaseAmount();
		}
		return entity.getBaseCurrencyAmount();
	}

	private TemporaryExpenseExchangeComputation resolveExchangeComputation(
			TemporaryExpense entity,
			TemporaryExpenseUpdateCommand command,
			CurrencyCode localCountryCode,
			BigDecimal localCurrencyAmount,
			CurrencyCode baseCountryCode,
			LocalDateTime occurredAt) {
		if (localCountryCode == null
				|| baseCountryCode == null
				|| localCurrencyAmount == null
				|| occurredAt == null
				|| localCurrencyAmount.signum() <= 0) {
			return TemporaryExpenseExchangeComputation.empty();
		}

		BigDecimal exchangeRate =
				resolveExchangeRate(entity, command, localCountryCode, baseCountryCode, occurredAt);
		if (exchangeRate == null) {
			return TemporaryExpenseExchangeComputation.empty();
		}
		BigDecimal calculatedBaseAmount =
				ExchangeAmountCalculator.calculateBaseAmount(localCurrencyAmount, exchangeRate);
		return new TemporaryExpenseExchangeComputation(calculatedBaseAmount, exchangeRate);
	}

	private BigDecimal resolveExchangeRate(
			TemporaryExpense entity,
			TemporaryExpenseUpdateCommand command,
			CurrencyCode localCountryCode,
			CurrencyCode baseCountryCode,
			LocalDateTime occurredAt) {
		if (localCountryCode == baseCountryCode) {
			return BigDecimal.ONE;
		}

		boolean requiresRateRefresh =
				command.localCountryCode() != null
						|| command.baseCountryCode() != null
						|| command.localCurrencyAmount() != null
						|| command.occurredAt() != null
						|| entity.getExchangeRate() == null;
		if (!requiresRateRefresh) {
			return entity.getExchangeRate();
		}

		return exchangeRateProvider.getExchangeRate(
				localCountryCode, baseCountryCode, occurredAt.atOffset(ZoneOffset.UTC));
	}
}
