package com.genesis.unipocket.analysis.command.application;

import com.genesis.unipocket.analysis.command.config.AnalysisBatchProperties;
import com.genesis.unipocket.analysis.command.persistence.entity.PairMonthlyAggregateEntity;
import com.genesis.unipocket.analysis.command.persistence.entity.PairMonthlyCategoryAggregateEntity;
import com.genesis.unipocket.analysis.command.persistence.repository.AnalysisBatchAggregationRepository;
import com.genesis.unipocket.analysis.command.persistence.repository.AnalysisBatchAggregationRepository.AccountAmountCount;
import com.genesis.unipocket.analysis.command.persistence.repository.AnalysisBatchAggregationRepository.AccountCategoryAmountCount;
import com.genesis.unipocket.analysis.command.persistence.repository.PairMonthlyAggregateRepository;
import com.genesis.unipocket.analysis.command.persistence.repository.PairMonthlyCategoryAggregateRepository;
import com.genesis.unipocket.analysis.common.enums.AnalysisMetricType;
import com.genesis.unipocket.analysis.common.enums.AnalysisQualityType;
import com.genesis.unipocket.analysis.common.enums.CurrencyType;
import com.genesis.unipocket.analysis.common.util.QuantileUtil;
import com.genesis.unipocket.global.common.enums.Category;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PairMonthlyAggregateRefresher {

	private static final MathContext MC = MathContext.DECIMAL64;

	private final AnalysisBatchAggregationRepository aggregationRepository;
	private final PairMonthlyAggregateRepository pairMonthlyAggregateRepository;
	private final PairMonthlyCategoryAggregateRepository pairMonthlyCategoryAggregateRepository;
	private final AnalysisBatchProperties properties;

	public void refresh(PairMonthKey pairMonthKey) {
		if (pairMonthKey.baseCountryCode() == null) {
			return;
		}
		refreshByCurrency(pairMonthKey, CurrencyType.LOCAL, AnalysisMetricType.TOTAL_LOCAL_AMOUNT);
		refreshByCurrency(pairMonthKey, CurrencyType.BASE, AnalysisMetricType.TOTAL_BASE_AMOUNT);
	}

	private void refreshByCurrency(
			PairMonthKey pairMonthKey, CurrencyType currencyType, AnalysisMetricType metricType) {
		List<AccountAmountCount> monthlyRows =
				aggregationRepository.aggregatePairMonthlyTotalByAccountFromMonthly(
						pairMonthKey.localCountryCode(),
						pairMonthKey.baseCountryCode(),
						pairMonthKey.targetYearMonth(),
						metricType,
						AnalysisQualityType.CLEANED);

		pairMonthlyCategoryAggregateRepository
				.deleteByLocalCountryCodeAndBaseCountryCodeAndTargetYearMonthAndQualityTypeAndCurrencyType(
						pairMonthKey.localCountryCode(),
						pairMonthKey.baseCountryCode(),
						pairMonthKey.targetYearMonth(),
						AnalysisQualityType.CLEANED,
						currencyType);
		pairMonthlyCategoryAggregateRepository.flush();

		if (monthlyRows.isEmpty() || monthlyRows.size() < properties.getPeerMinSampleSize()) {
			pairMonthlyAggregateRepository
					.findByLocalCountryCodeAndBaseCountryCodeAndTargetYearMonthAndQualityTypeAndMetricType(
							pairMonthKey.localCountryCode(),
							pairMonthKey.baseCountryCode(),
							pairMonthKey.targetYearMonth(),
							AnalysisQualityType.CLEANED,
							metricType)
					.ifPresent(pairMonthlyAggregateRepository::delete);
			return;
		}

		PairIqrBounds iqrBounds = computeIqrBounds(monthlyRows);
		List<AccountAmountCount> filteredRows = filterByIqr(monthlyRows, iqrBounds);
		List<AccountAmountCount> effectiveRows =
				filteredRows.isEmpty() ? monthlyRows : filteredRows;
		Set<Long> includedAccountIds =
				effectiveRows.stream()
						.map(AccountAmountCount::accountBookId)
						.collect(Collectors.toSet());

		BigDecimal totalMetricSum =
				effectiveRows.stream()
						.map(AccountAmountCount::totalAmount)
						.reduce(BigDecimal.ZERO, BigDecimal::add);
		long includedAccountCount = effectiveRows.size();
		BigDecimal averageMetricValue = divideScale(totalMetricSum, includedAccountCount, 4);

		var existingPairAggregate =
				pairMonthlyAggregateRepository
						.findByLocalCountryCodeAndBaseCountryCodeAndTargetYearMonthAndQualityTypeAndMetricType(
								pairMonthKey.localCountryCode(),
								pairMonthKey.baseCountryCode(),
								pairMonthKey.targetYearMonth(),
								AnalysisQualityType.CLEANED,
								metricType);
		if (existingPairAggregate.isPresent()) {
			existingPairAggregate
					.get()
					.update(
							includedAccountCount,
							totalMetricSum,
							averageMetricValue,
							iqrBounds == null ? null : iqrBounds.lower(),
							iqrBounds == null ? null : iqrBounds.upper());
		} else {
			pairMonthlyAggregateRepository.save(
					Objects.requireNonNull(
							PairMonthlyAggregateEntity.of(
									pairMonthKey.localCountryCode(),
									pairMonthKey.baseCountryCode(),
									pairMonthKey.targetYearMonth(),
									AnalysisQualityType.CLEANED,
									metricType,
									includedAccountCount,
									totalMetricSum,
									averageMetricValue,
									iqrBounds == null ? null : iqrBounds.lower(),
									iqrBounds == null ? null : iqrBounds.upper())));
		}

		List<AccountCategoryAmountCount> categoryRows =
				aggregationRepository.aggregatePairMonthlyCategoryByAccountFromMonthly(
						pairMonthKey.localCountryCode(),
						pairMonthKey.baseCountryCode(),
						pairMonthKey.targetYearMonth(),
						AnalysisQualityType.CLEANED,
						currencyType);
		Map<Integer, BigDecimal> categoryTotalMap = new HashMap<>();
		for (AccountCategoryAmountCount row : categoryRows) {
			if (!includedAccountIds.contains(row.accountBookId())) {
				continue;
			}
			if (row.categoryOrdinal() == null) {
				continue;
			}
			categoryTotalMap.merge(row.categoryOrdinal(), row.totalAmount(), BigDecimal::add);
		}

		List<PairMonthlyCategoryAggregateEntity> toSave = new ArrayList<>();
		for (Category category : Category.values()) {
			if (category == Category.INCOME) {
				continue;
			}
			BigDecimal totalAmount =
					categoryTotalMap.getOrDefault(category.ordinal(), BigDecimal.ZERO);
			BigDecimal averageAmount = divideScale(totalAmount, includedAccountCount, 4);
			toSave.add(
					PairMonthlyCategoryAggregateEntity.of(
							pairMonthKey.localCountryCode(),
							pairMonthKey.baseCountryCode(),
							pairMonthKey.targetYearMonth(),
							AnalysisQualityType.CLEANED,
							currencyType,
							category,
							includedAccountCount,
							totalAmount,
							averageAmount));
		}
		if (!toSave.isEmpty()) {
			pairMonthlyCategoryAggregateRepository.saveAll(toSave);
		}
	}

	private PairIqrBounds computeIqrBounds(List<AccountAmountCount> monthlyRows) {
		if (monthlyRows == null || monthlyRows.size() < properties.getPeerMinSampleSize()) {
			return null;
		}
		List<BigDecimal> sorted =
				monthlyRows.stream()
						.map(AccountAmountCount::totalAmount)
						.sorted(Comparator.naturalOrder())
						.toList();
		BigDecimal q1 = QuantileUtil.linearInterpolatedQuantile(sorted, 0.25d, MC);
		BigDecimal q3 = QuantileUtil.linearInterpolatedQuantile(sorted, 0.75d, MC);
		BigDecimal iqr = q3.subtract(q1, MC);
		if (iqr.compareTo(BigDecimal.ZERO) < 0) {
			return null;
		}
		BigDecimal delta =
				iqr.multiply(BigDecimal.valueOf(properties.getOutlierIqrMultiplier()), MC);
		return new PairIqrBounds(q1.subtract(delta, MC), q3.add(delta, MC));
	}

	private List<AccountAmountCount> filterByIqr(
			List<AccountAmountCount> monthlyRows, PairIqrBounds iqrBounds) {
		if (iqrBounds == null) {
			return monthlyRows;
		}
		return monthlyRows.stream()
				.filter(
						row ->
								row.totalAmount().compareTo(iqrBounds.lower()) >= 0
										&& row.totalAmount().compareTo(iqrBounds.upper()) <= 0)
				.toList();
	}

	private BigDecimal divideScale(BigDecimal value, long divisor, int scale) {
		if (divisor <= 0L) {
			return BigDecimal.ZERO;
		}
		return value.divide(BigDecimal.valueOf(divisor), scale, RoundingMode.HALF_UP);
	}

	private record PairIqrBounds(BigDecimal lower, BigDecimal upper) {}
}
