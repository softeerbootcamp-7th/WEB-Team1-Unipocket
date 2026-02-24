package com.genesis.unipocket.analysis.command.application;

import com.genesis.unipocket.analysis.command.persistence.repository.AnalysisBatchAggregationRepository.CategoryAmountPairCount;
import com.genesis.unipocket.analysis.command.persistence.repository.AnalysisBatchAggregationRepository.CategoryLocalCurrencyGroupRow;
import com.genesis.unipocket.analysis.command.persistence.repository.AnalysisBatchAggregationRepository.LocalCurrencyGroupRow;
import com.genesis.unipocket.analysis.common.util.CategoryOrdinalParser;
import com.genesis.unipocket.exchange.common.service.ExchangeRateService;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MonthlyAmountCorrectionCalculator {

	private final ExchangeRateService exchangeRateService;

	public BigDecimal computeCorrectedLocalAmount(
			List<LocalCurrencyGroupRow> groups,
			CurrencyCode targetCurrency,
			OffsetDateTime refDateTime) {
		BigDecimal total = BigDecimal.ZERO;
		for (LocalCurrencyGroupRow group : groups) {
			if (group.localCurrencyCode() == null) {
				continue;
			}
			CurrencyCode from = parseCurrencyCode(group.localCurrencyCode());
			if (from == null) {
				continue;
			}
			BigDecimal amount = group.localAmountSum();
			if (from == targetCurrency) {
				total = total.add(amount);
			} else {
				total =
						total.add(
								exchangeRateService.convertAmount(
										amount, from, targetCurrency, refDateTime));
			}
		}
		return total;
	}

	public List<CategoryAmountPairCount> computeCorrectedCategoryRows(
			List<CategoryAmountPairCount> rawCategoryRows,
			List<CategoryLocalCurrencyGroupRow> currencyByCategoryRows,
			CurrencyCode targetCurrency,
			OffsetDateTime refDateTime) {
		Map<Integer, BigDecimal> correctedLocalByCategory = new HashMap<>();
		for (CategoryLocalCurrencyGroupRow row : currencyByCategoryRows) {
			if (row.localCurrencyCode() == null) {
				continue;
			}
			Integer ordinal = CategoryOrdinalParser.parse(row.categoryValue());
			if (ordinal == null) {
				continue;
			}
			CurrencyCode from = parseCurrencyCode(row.localCurrencyCode());
			if (from == null) {
				continue;
			}
			BigDecimal amount = row.localAmountSum();
			BigDecimal converted =
					from == targetCurrency
							? amount
							: exchangeRateService.convertAmount(
									amount, from, targetCurrency, refDateTime);
			correctedLocalByCategory.merge(ordinal, converted, BigDecimal::add);
		}
		return rawCategoryRows.stream()
				.map(
						raw ->
								new CategoryAmountPairCount(
										raw.categoryOrdinal(),
										correctedLocalByCategory.getOrDefault(
												raw.categoryOrdinal(), BigDecimal.ZERO),
										raw.totalBaseAmount(),
										raw.expenseCount()))
				.toList();
	}

	private CurrencyCode parseCurrencyCode(String code) {
		if (code == null) {
			return null;
		}
		try {
			int ordinal = Integer.parseInt(code);
			CurrencyCode[] values = CurrencyCode.values();
			if (ordinal >= 0 && ordinal < values.length) {
				return values[ordinal];
			}
			return null;
		} catch (NumberFormatException e) {
			try {
				return CurrencyCode.valueOf(code);
			} catch (IllegalArgumentException ex) {
				return null;
			}
		}
	}
}
