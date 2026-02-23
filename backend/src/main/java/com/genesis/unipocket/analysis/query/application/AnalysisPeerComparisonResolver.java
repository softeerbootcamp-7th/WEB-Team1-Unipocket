package com.genesis.unipocket.analysis.query.application;

import com.genesis.unipocket.analysis.command.persistence.entity.PairMonthlyAggregateEntity;
import com.genesis.unipocket.analysis.command.persistence.entity.PairMonthlyCategoryAggregateEntity;
import com.genesis.unipocket.analysis.command.persistence.repository.PairMonthlyAggregateRepository;
import com.genesis.unipocket.analysis.command.persistence.repository.PairMonthlyCategoryAggregateRepository;
import com.genesis.unipocket.analysis.common.enums.AnalysisMetricType;
import com.genesis.unipocket.analysis.common.enums.AnalysisQualityType;
import com.genesis.unipocket.analysis.common.enums.CurrencyType;
import com.genesis.unipocket.global.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CountryCode;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class AnalysisPeerComparisonResolver {

	private static final int AVERAGE_DIVISION_SCALE = 10;
	private static final AnalysisQualityType PEER_QUALITY_TYPE = AnalysisQualityType.CLEANED;

	private final PairMonthlyAggregateRepository pairMonthlyAggregateRepository;
	private final PairMonthlyCategoryAggregateRepository pairMonthlyCategoryAggregateRepository;

	AnalysisPairPeerContext resolvePairPeerContext(
			CountryCode localCountryCode,
			CountryCode baseCountryCode,
			LocalDate monthStart,
			AnalysisMetricType metricType,
			BigDecimal myTotalAmount,
			boolean myMonthlyReady) {
		Optional<PairMonthlyAggregateEntity> optional =
				pairMonthlyAggregateRepository
						.findByLocalCountryCodeAndBaseCountryCodeAndTargetYearMonthAndQualityTypeAndMetricType(
								localCountryCode,
								baseCountryCode,
								monthStart,
								PEER_QUALITY_TYPE,
								metricType);
		if (optional.isEmpty()) {
			return AnalysisPairPeerContext.unavailable();
		}

		PairMonthlyAggregateEntity row = optional.get();
		long includedAccountCount = row.getIncludedAccountCount();
		if (includedAccountCount <= 0L) {
			return AnalysisPairPeerContext.unavailable();
		}

		boolean myIncluded =
				myMonthlyReady
						&& isWithinBounds(
								myTotalAmount, row.getIqrLowerBound(), row.getIqrUpperBound());

		BigDecimal totalSum = row.getTotalMetricSum();
		long effectivePeerCount = includedAccountCount;
		if (myIncluded) {
			if (includedAccountCount <= 1L) {
				return AnalysisPairPeerContext.unavailable();
			}
			effectivePeerCount = includedAccountCount - 1L;
			totalSum = totalSum.subtract(myTotalAmount);
			if (totalSum.compareTo(BigDecimal.ZERO) < 0) {
				totalSum = BigDecimal.ZERO;
			}
		}
		if (effectivePeerCount <= 0L) {
			return AnalysisPairPeerContext.unavailable();
		}
		return AnalysisPairPeerContext.available(
				divide(totalSum, effectivePeerCount), effectivePeerCount, myIncluded);
	}

	Map<Category, BigDecimal> resolvePairCategoryAverageMap(
			CountryCode localCountryCode,
			CountryCode baseCountryCode,
			LocalDate monthStart,
			CurrencyType currencyType,
			AnalysisPairPeerContext peerContext,
			Map<Category, BigDecimal> myCategoryMap) {
		Map<Category, BigDecimal> avgByCategory = new EnumMap<>(Category.class);
		if (!peerContext.peerAvailable()) {
			return avgByCategory;
		}

		List<PairMonthlyCategoryAggregateEntity> rows =
				pairMonthlyCategoryAggregateRepository
						.findAllByLocalCountryCodeAndBaseCountryCodeAndTargetYearMonthAndQualityTypeAndCurrencyType(
								localCountryCode,
								baseCountryCode,
								monthStart,
								PEER_QUALITY_TYPE,
								currencyType);
		Map<Category, PairMonthlyCategoryAggregateEntity> rowMap = new EnumMap<>(Category.class);
		for (PairMonthlyCategoryAggregateEntity row : rows) {
			rowMap.put(row.getCategory(), row);
		}

		for (Category category : Category.values()) {
			if (category == Category.INCOME || category == Category.UNCLASSIFIED) {
				continue;
			}
			PairMonthlyCategoryAggregateEntity row = rowMap.get(category);
			BigDecimal totalAmount = row == null ? BigDecimal.ZERO : row.getTotalAmount();
			if (peerContext.myIncluded()) {
				totalAmount =
						totalAmount.subtract(myCategoryMap.getOrDefault(category, BigDecimal.ZERO));
			}
			if (totalAmount.compareTo(BigDecimal.ZERO) < 0) {
				totalAmount = BigDecimal.ZERO;
			}
			avgByCategory.put(category, divide(totalAmount, peerContext.effectivePeerCount()));
		}
		return avgByCategory;
	}

	private boolean isWithinBounds(BigDecimal value, BigDecimal lower, BigDecimal upper) {
		if (value == null) {
			return false;
		}
		if (lower != null && value.compareTo(lower) < 0) {
			return false;
		}
		if (upper != null && value.compareTo(upper) > 0) {
			return false;
		}
		return true;
	}

	private BigDecimal divide(BigDecimal amount, long divisor) {
		if (divisor <= 0L) {
			return BigDecimal.ZERO;
		}
		return amount.divide(
				BigDecimal.valueOf(divisor), AVERAGE_DIVISION_SCALE, RoundingMode.HALF_UP);
	}
}
