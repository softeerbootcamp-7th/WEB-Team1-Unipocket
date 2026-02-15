package com.genesis.unipocket.tempexpense.command.application.parsing;

import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.tempexpense.command.application.parsing.command.ExchangeRateLookupCommand;
import com.genesis.unipocket.tempexpense.command.application.parsing.result.AccountBookRateContext;
import com.genesis.unipocket.tempexpense.command.application.parsing.result.NormalizedParsedExpenseItem;
import com.genesis.unipocket.tempexpense.command.application.result.ParsingResult;
import com.genesis.unipocket.tempexpense.command.persistence.entity.TempExpenseMeta;
import com.genesis.unipocket.tempexpense.command.persistence.entity.TemporaryExpense;
import com.genesis.unipocket.tempexpense.command.persistence.repository.TemporaryExpenseRepository;
import com.genesis.unipocket.tempexpense.common.enums.TemporaryExpenseStatus;
import java.math.BigDecimal;
import java.math.RoundingMode;
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

	@Transactional
	public ParsingResult persist(
			TempExpenseMeta meta,
			List<NormalizedParsedExpenseItem> normalizedItems,
			AccountBookRateContext rateContext,
			Map<ExchangeRateLookupCommand, BigDecimal> exchangeRateMap) {
		List<TemporaryExpense> createdExpenses = new ArrayList<>();
		int normalCount = 0;
		int incompleteCount = 0;

		for (NormalizedParsedExpenseItem item : normalizedItems) {
			TemporaryExpenseStatus status = determineStatus(item);
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

		if (item.baseAmount() != null && item.baseCurrencyCode() == baseCurrencyCode) {
			baseAmount = item.baseAmount();
			if (item.localAmount() != null && item.localAmount().compareTo(BigDecimal.ZERO) > 0) {
				exchangeRate = baseAmount.divide(item.localAmount(), 4, RoundingMode.HALF_UP);
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
			baseAmount =
					item.localAmount().multiply(exchangeRate).setScale(2, RoundingMode.HALF_UP);
		}

		return new CalculatedAmount(baseAmount, exchangeRate);
	}

	private TemporaryExpenseStatus determineStatus(NormalizedParsedExpenseItem item) {
		if (item.merchantName() == null || item.localAmount() == null) {
			return TemporaryExpenseStatus.INCOMPLETE;
		}
		if (item.occurredAt() == null) {
			return TemporaryExpenseStatus.INCOMPLETE;
		}
		return TemporaryExpenseStatus.NORMAL;
	}

	private record CalculatedAmount(BigDecimal baseAmount, BigDecimal exchangeRate) {}
}
