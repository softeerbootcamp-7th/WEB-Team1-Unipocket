package com.genesis.unipocket.tempexpense.command.application.parsing;

import com.genesis.unipocket.expense.common.util.ExchangeAmountCalculator;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.tempexpense.command.application.parsing.result.AccountBookRateContext;
import com.genesis.unipocket.tempexpense.command.application.parsing.result.NormalizedParsedExpenseItem;
import com.genesis.unipocket.tempexpense.command.facade.port.ExchangeRateProvider;
import com.genesis.unipocket.tempexpense.command.persistence.entity.File;
import com.genesis.unipocket.tempexpense.command.persistence.entity.TempExpenseMeta;
import com.genesis.unipocket.tempexpense.command.persistence.entity.TemporaryExpense;
import com.genesis.unipocket.tempexpense.command.persistence.entity.tempexpense.TempExpenseAmountInfo;
import com.genesis.unipocket.tempexpense.command.persistence.entity.tempexpense.TempExpenseContentInfo;
import com.genesis.unipocket.tempexpense.command.persistence.entity.tempexpense.TempExpenseStatusPolicy;
import com.genesis.unipocket.tempexpense.command.persistence.repository.TemporaryExpenseRepository;
import com.genesis.unipocket.tempexpense.common.enums.TemporaryExpenseStatus;
import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TemporaryExpensePersistenceService {

	private final TemporaryExpenseRepository temporaryExpenseRepository;
	private final TempExpenseStatusPolicy tempExpenseStatusPolicy;
	private final ExchangeRateProvider exchangeRateProvider;

	@Transactional
	public void persist(
			File file,
			TempExpenseMeta meta,
			List<NormalizedParsedExpenseItem> normalizedItems,
			AccountBookRateContext rateContext) {
		List<TemporaryExpense> createdExpenses = new ArrayList<>();

		for (NormalizedParsedExpenseItem item : normalizedItems) {
			CalculatedAmount calculatedAmount =
					calculateAmounts(item, rateContext.baseCurrencyCode());
			TempExpenseContentInfo contentInfo =
					TempExpenseContentInfo.of(
							item.merchantName(), item.category(), item.memo(), item.occurredAt());
			TempExpenseAmountInfo amountInfo =
					TempExpenseAmountInfo.of(
							item.localCurrencyCode(),
							item.localAmount(),
							rateContext.baseCurrencyCode(),
							calculatedAmount.baseAmount(),
							calculatedAmount.exchangeRate());
			TemporaryExpenseStatus status =
					tempExpenseStatusPolicy.resolve(contentInfo, amountInfo);

			TemporaryExpense expense =
					TemporaryExpense.builder()
							.tempExpenseMetaId(meta.getTempExpenseMetaId())
							.fileId(file.getFileId())
							.merchantName(contentInfo.getMerchantName())
							.category(contentInfo.getCategory())
							.localCountryCode(amountInfo.getLocalCurrencyCode())
							.localCurrencyAmount(amountInfo.getLocalCurrencyAmount())
							.baseCountryCode(amountInfo.getBaseCurrencyCode())
							.baseCurrencyAmount(amountInfo.getBaseCurrencyAmount())
							.exchangeRate(amountInfo.getExchangeRate())
							.paymentsMethod("카드")
							.memo(contentInfo.getMemo())
							.occurredAt(contentInfo.getOccurredAt())
							.status(status)
							.cardLastFourDigits(item.cardLastFourDigits())
							.approvalNumber(item.approvalNumber())
							.build();
			createdExpenses.add(expense);
		}

		temporaryExpenseRepository.saveAll(createdExpenses);
	}

	private CalculatedAmount calculateAmounts(
			NormalizedParsedExpenseItem item, CurrencyCode baseCurrencyCode) {
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
		if (item.localCurrencyCode() == null) {
			return new CalculatedAmount(null, null);
		}
		if (item.localCurrencyCode() == baseCurrencyCode) {
			exchangeRate = BigDecimal.ONE;
		} else {
			exchangeRate =
					exchangeRateProvider.getExchangeRate(
							item.localCurrencyCode(),
							baseCurrencyCode,
							item.occurredAt()
									.toLocalDate()
									.atStartOfDay()
									.atOffset(ZoneOffset.UTC));
		}
		if (exchangeRate != null) {
			baseAmount =
					ExchangeAmountCalculator.calculateBaseAmount(item.localAmount(), exchangeRate);
		}

		return new CalculatedAmount(baseAmount, exchangeRate);
	}

	private record CalculatedAmount(BigDecimal baseAmount, BigDecimal exchangeRate) {}
}
