package com.genesis.unipocket.tempexpense.command.application.parsing;

import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.expense.support.ExchangeAmountCalculator;
import com.genesis.unipocket.tempexpense.command.application.parsing.command.ExchangeRateLookupCommand;
import com.genesis.unipocket.tempexpense.command.application.parsing.result.AccountBookRateContext;
import com.genesis.unipocket.tempexpense.command.application.parsing.result.NormalizedParsedExpenseItem;
import com.genesis.unipocket.tempexpense.command.application.result.ParsingResult;
import com.genesis.unipocket.tempexpense.command.persistence.entity.File;
import com.genesis.unipocket.tempexpense.command.persistence.entity.TempExpenseMeta;
import com.genesis.unipocket.tempexpense.command.persistence.entity.TemporaryExpense;
import com.genesis.unipocket.tempexpense.command.persistence.repository.TemporaryExpenseRepository;
import com.genesis.unipocket.tempexpense.common.enums.TemporaryExpenseStatus;
import com.genesis.unipocket.tempexpense.common.validation.TemporaryExpenseValidator;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TemporaryExpensePersistenceService {

	private final TemporaryExpenseRepository temporaryExpenseRepository;
	private final TemporaryExpenseValidator temporaryExpenseValidator;

	@Transactional
	public ParsingResult persist(
			File file,
			TempExpenseMeta meta,
			List<NormalizedParsedExpenseItem> normalizedItems,
			AccountBookRateContext rateContext,
			Map<ExchangeRateLookupCommand, BigDecimal> exchangeRateMap) {
		List<TemporaryExpense> createdExpenses = new ArrayList<>();
		int normalCount = 0;
		int incompleteCount = 0;

		for (NormalizedParsedExpenseItem item : normalizedItems) {
			TemporaryExpenseStatus status =
					temporaryExpenseValidator.resolveStatus(
							null,
							item.merchantName(),
							item.category(),
							item.localCurrencyCode(),
							item.localAmount(),
							rateContext.baseCurrencyCode(),
							item.occurredAt());
			if (status == TemporaryExpenseStatus.NORMAL) {
				normalCount++;
			} else if (status == TemporaryExpenseStatus.INCOMPLETE) {
				incompleteCount++;
			}

			CalculatedAmount calculatedAmount =
					calculateAmounts(item, rateContext.baseCurrencyCode(), exchangeRateMap);

			TemporaryExpense expense =
					TemporaryExpense.builder()
							.tempExpenseMetaId(meta.getTempExpenseMetaId())
							.fileId(file.getFileId())
							.merchantName(item.merchantName())
							.category(item.category())
							.localCountryCode(item.localCurrencyCode())
							.localCurrencyAmount(item.localAmount())
							.baseCountryCode(rateContext.baseCurrencyCode())
							.baseCurrencyAmount(calculatedAmount.baseAmount())
							.exchangeRate(calculatedAmount.exchangeRate())
							.paymentsMethod("카드")
							.memo(item.memo())
							.occurredAt(item.occurredAt())
							.status(status)
							.cardLastFourDigits(item.cardLastFourDigits())
							.approvalNumber(item.approvalNumber())
							.build();
			createdExpenses.add(expense);
		}

		List<TemporaryExpense> savedExpenses = temporaryExpenseRepository.saveAll(createdExpenses);
		return new ParsingResult(
				meta.getTempExpenseMetaId(),
				savedExpenses.size(),
				normalCount,
				incompleteCount,
				0,
				savedExpenses);
	}

	private CalculatedAmount calculateAmounts(
			NormalizedParsedExpenseItem item,
			CurrencyCode baseCurrencyCode,
			Map<ExchangeRateLookupCommand, BigDecimal> exchangeRateMap) {
		BigDecimal baseAmount = null;
		BigDecimal exchangeRate = null;

		if (item.baseAmount() != null
				&& item.baseCurrencyCode() != null
				&& item.baseCurrencyCode() == baseCurrencyCode) {
			baseAmount = item.baseAmount();
			if (item.localAmount() != null && item.localAmount().compareTo(BigDecimal.ZERO) > 0) {
				exchangeRate =
						ExchangeAmountCalculator.deriveExchangeRate(baseAmount, item.localAmount());
			}
			return new CalculatedAmount(baseAmount, exchangeRate);
		}

		if (item.localAmount() == null || item.occurredAt() == null) {
			return new CalculatedAmount(null, null);
		}

		ExchangeRateLookupCommand key =
				new ExchangeRateLookupCommand(
						item.localCurrencyCode(),
						baseCurrencyCode,
						item.occurredAt().toLocalDate());
		exchangeRate = exchangeRateMap.get(key);
		if (exchangeRate != null) {
			baseAmount = ExchangeAmountCalculator.calculateBaseAmount(item.localAmount(), exchangeRate);
		}

		return new CalculatedAmount(baseAmount, exchangeRate);
	}

	private record CalculatedAmount(BigDecimal baseAmount, BigDecimal exchangeRate) {}
}
