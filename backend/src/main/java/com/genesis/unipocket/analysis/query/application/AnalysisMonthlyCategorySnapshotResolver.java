package com.genesis.unipocket.analysis.query.application;

import com.genesis.unipocket.analysis.command.persistence.repository.AnalysisBatchAggregationRepository;
import com.genesis.unipocket.analysis.command.persistence.repository.AnalysisBatchAggregationRepository.CategoryAmountCount;
import com.genesis.unipocket.analysis.command.persistence.repository.AnalysisMonthlyDirtyRepository;
import com.genesis.unipocket.analysis.common.enums.AnalysisBatchJobStatus;
import com.genesis.unipocket.analysis.common.enums.AnalysisMetricType;
import com.genesis.unipocket.analysis.common.enums.AnalysisQualityType;
import com.genesis.unipocket.analysis.common.enums.CurrencyType;
import com.genesis.unipocket.analysis.query.persistence.repository.AnalysisQueryRepository;
import com.genesis.unipocket.exchange.common.service.ExchangeRateService;
import com.genesis.unipocket.global.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CountryCode;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class AnalysisMonthlyCategorySnapshotResolver {

	private static final AnalysisQualityType SUMMARY_QUALITY_TYPE = AnalysisQualityType.CLEANED;

	private final AnalysisQueryRepository analysisQueryRepository;
	private final AnalysisBatchAggregationRepository aggregationRepository;
	private final AnalysisMonthlyDirtyRepository monthlyDirtyRepository;
	private final ExchangeRateService exchangeRateService;

	AnalysisMyCategorySnapshot resolve(
			Long accountBookId,
			CountryCode localCountryCode,
			AnalysisMonthRange range,
			CurrencyType currencyType,
			CurrencyCode localCurrency) {
		boolean monthlyBatchReady =
				isMonthlyBatchReady(accountBookId, localCountryCode, range, currencyType);
		if (monthlyBatchReady) {
			return new AnalysisMyCategorySnapshot(
					loadCategoryMapFromMonthly(accountBookId, range, currencyType), true);
		}

		OffsetDateTime startOffset = toOffsetUtc(range.startUtc());
		OffsetDateTime endOffset = toOffsetUtc(range.endUtcExclusive());
		if (currencyType == CurrencyType.LOCAL) {
			Map<Category, BigDecimal> realtimeCategoryMap =
					toMyCategoryMapWithConversion(
							analysisQueryRepository.getMyCategorySpentGroupedByCurrency(
									accountBookId, startOffset, endOffset),
							localCurrency,
							startOffset);
			return new AnalysisMyCategorySnapshot(realtimeCategoryMap, false);
		}

		Map<Category, BigDecimal> realtimeCategoryMap =
				toMyCategoryMap(
						analysisQueryRepository.getMyCategorySpent(
								accountBookId, startOffset, endOffset, currencyType));
		return new AnalysisMyCategorySnapshot(realtimeCategoryMap, false);
	}

	private Map<Category, BigDecimal> loadCategoryMapFromMonthly(
			Long accountBookId, AnalysisMonthRange range, CurrencyType currencyType) {
		List<CategoryAmountCount> categoryRows =
				aggregationRepository.aggregateAccountMonthlyCategoryFromMonthly(
						accountBookId,
						range.yearMonth().atDay(1),
						SUMMARY_QUALITY_TYPE,
						currencyType);
		Map<Category, BigDecimal> categoryMap = new EnumMap<>(Category.class);
		for (CategoryAmountCount row : categoryRows) {
			if (row.categoryOrdinal() == null) {
				continue;
			}
			if (row.categoryOrdinal() < 0 || row.categoryOrdinal() >= Category.values().length) {
				continue;
			}
			Category category = Category.values()[row.categoryOrdinal()];
			if (category == Category.INCOME || category == Category.UNCLASSIFIED) {
				continue;
			}
			categoryMap.put(category, row.totalAmount());
		}
		return categoryMap;
	}

	private boolean isMonthlyBatchReady(
			Long accountBookId,
			CountryCode localCountryCode,
			AnalysisMonthRange range,
			CurrencyType currencyType) {
		if (isDirtyPending(accountBookId, localCountryCode, range.yearMonth().atDay(1))) {
			return false;
		}
		return aggregationRepository.hasAccountMonthlyAggregate(
				accountBookId,
				range.yearMonth().atDay(1),
				toAmountMetricType(currencyType),
				SUMMARY_QUALITY_TYPE);
	}

	private boolean isDirtyPending(
			Long accountBookId, CountryCode localCountryCode, LocalDate monthStart) {
		return monthlyDirtyRepository
				.existsByCountryCodeAndAccountBookIdAndTargetYearMonthAndStatusNot(
						localCountryCode,
						accountBookId,
						monthStart,
						AnalysisBatchJobStatus.SUCCESS);
	}

	private AnalysisMetricType toAmountMetricType(CurrencyType currencyType) {
		if (currencyType == null) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
		}
		return currencyType == CurrencyType.BASE
				? AnalysisMetricType.TOTAL_BASE_AMOUNT
				: AnalysisMetricType.TOTAL_LOCAL_AMOUNT;
	}

	private Map<Category, BigDecimal> toMyCategoryMap(List<Object[]> rows) {
		Map<Category, BigDecimal> result = new EnumMap<>(Category.class);
		for (Object[] row : rows) {
			Category category = (Category) row[0];
			result.put(category, toBigDecimal(row[1]));
		}
		return result;
	}

	private Map<Category, BigDecimal> toMyCategoryMapWithConversion(
			List<Object[]> rows, CurrencyCode targetCurrency, OffsetDateTime refDateTime) {
		Map<Category, BigDecimal> result = new EnumMap<>(Category.class);
		for (Object[] row : rows) {
			Category category = (Category) row[0];
			CurrencyCode from = (CurrencyCode) row[1];
			BigDecimal amount = toBigDecimal(row[2]);
			if (from != null && from != targetCurrency) {
				amount =
						exchangeRateService.convertAmount(
								amount, from, targetCurrency, refDateTime);
			}
			result.merge(category, amount, BigDecimal::add);
		}
		return result;
	}

	private OffsetDateTime toOffsetUtc(LocalDateTime localDateTime) {
		return localDateTime.atOffset(ZoneOffset.UTC);
	}

	private BigDecimal toBigDecimal(Object value) {
		if (value == null) {
			return BigDecimal.ZERO;
		}
		if (value instanceof BigDecimal bd) {
			return bd;
		}
		return new BigDecimal(value.toString());
	}
}
