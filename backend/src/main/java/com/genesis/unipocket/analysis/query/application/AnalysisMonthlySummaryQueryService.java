package com.genesis.unipocket.analysis.query.application;

import com.genesis.unipocket.analysis.common.enums.AnalysisMetricType;
import com.genesis.unipocket.analysis.common.enums.CurrencyType;
import com.genesis.unipocket.analysis.query.facade.port.AccountBookOwnershipValidator;
import com.genesis.unipocket.analysis.query.persistence.repository.AnalysisQueryRepository;
import com.genesis.unipocket.analysis.query.persistence.response.AnalysisOverviewRes;
import com.genesis.unipocket.global.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CountryCode;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.global.util.CountryCodeTimezoneMapper;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AnalysisMonthlySummaryQueryService {

	private final AnalysisQueryRepository analysisQueryRepository;
	private final AnalysisMonthlyDailySeriesBuilder dailySeriesBuilder;
	private final AnalysisMonthlyCategorySnapshotResolver categorySnapshotResolver;
	private final AnalysisPeerComparisonResolver peerComparisonResolver;
	private final AccountBookOwnershipValidator accountBookOwnershipValidator;

	@Transactional
	public AnalysisOverviewRes getAnalysisOverview(
			UUID userId,
			Long accountBookId,
			String yearText,
			String monthText,
			CurrencyType currencyType) {
		accountBookOwnershipValidator.validateOwnership(accountBookId, userId.toString());

		int year = AnalysisMonthlySummaryDateSupport.parseYear(yearText);
		int month = AnalysisMonthlySummaryDateSupport.parseMonth(monthText);
		Object[] countryCodes = analysisQueryRepository.getAccountBookCountryCodes(accountBookId);
		CountryCode localCountryCode = (CountryCode) countryCodes[0];
		CountryCode baseCountryCode = (CountryCode) countryCodes[1];
		validateCountryCodes(localCountryCode, baseCountryCode, currencyType);
		ZoneId zoneId = CountryCodeTimezoneMapper.getZoneId(localCountryCode);
		AnalysisMonthlySummaryDateSupport.validateNotFutureYearMonth(year, month, zoneId);

		CurrencyCode localCurrency = localCountryCode.getCurrencyCode();

		AnalysisMonthRange thisRange =
				AnalysisMonthlySummaryDateSupport.buildMonthRange(year, month, zoneId, true);
		AnalysisMonthRange prevRange =
				AnalysisMonthlySummaryDateSupport.buildMonthRange(
						thisRange.yearMonth().minusMonths(1).getYear(),
						thisRange.yearMonth().minusMonths(1).getMonthValue(),
						zoneId,
						false);

		AnalysisDailySeries thisSeries =
				dailySeriesBuilder.build(
						accountBookId,
						currencyType,
						thisRange.startUtc(),
						thisRange.endUtcExclusive(),
						thisRange.startLocalDate(),
						thisRange.endLocalDateInclusive(),
						zoneId,
						localCurrency);

		AnalysisDailySeries prevSeries =
				dailySeriesBuilder.build(
						accountBookId,
						currencyType,
						prevRange.startUtc(),
						prevRange.endUtcExclusive(),
						prevRange.startLocalDate(),
						prevRange.endLocalDateInclusive(),
						zoneId,
						localCurrency);

		AnalysisMyCategorySnapshot mySnapshot =
				categorySnapshotResolver.resolve(
						accountBookId, localCountryCode, thisRange, currencyType, localCurrency);
		Map<Category, BigDecimal> myCategoryMap = mySnapshot.categoryMap();

		AnalysisPairPeerContext peerContext =
				peerComparisonResolver.resolvePairPeerContext(
						localCountryCode,
						baseCountryCode,
						thisRange.yearMonth().atDay(1),
						toAmountMetricType(currencyType),
						thisSeries.total(),
						mySnapshot.monthlyBatchReady());

		Map<Category, BigDecimal> avgByCategory =
				peerComparisonResolver.resolvePairCategoryAverageMap(
						localCountryCode,
						baseCountryCode,
						thisRange.yearMonth().atDay(1),
						currencyType,
						peerContext,
						myCategoryMap);

		AnalysisOverviewRes.CompareWithAverage compareWithAverage =
				buildCompareWithAverage(month, thisSeries, peerContext);
		AnalysisOverviewRes.CompareWithLastMonth compareWithLastMonth =
				buildCompareWithLastMonth(month, thisSeries, prevRange, prevSeries);
		AnalysisOverviewRes.CompareByCategory compareByCategory =
				buildCompareByCategory(myCategoryMap, avgByCategory);
		CountryCode responseCountryCode =
				resolveResponseCountryCode(localCountryCode, baseCountryCode, currencyType);

		return new AnalysisOverviewRes(
				responseCountryCode.name(),
				compareWithAverage,
				compareWithLastMonth,
				compareByCategory);
	}

	private CountryCode resolveResponseCountryCode(
			CountryCode localCountryCode, CountryCode baseCountryCode, CurrencyType currencyType) {
		if (currencyType == CurrencyType.BASE) {
			if (baseCountryCode == null) {
				throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
			}
			return baseCountryCode;
		}
		if (localCountryCode == null) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
		}
		return localCountryCode;
	}

	private void validateCountryCodes(
			CountryCode localCountryCode, CountryCode baseCountryCode, CurrencyType currencyType) {
		if (localCountryCode == null) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
		}
		if (currencyType == CurrencyType.BASE && baseCountryCode == null) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
		}
	}

	private AnalysisOverviewRes.CompareWithAverage buildCompareWithAverage(
			int month, AnalysisDailySeries thisSeries, AnalysisPairPeerContext peerContext) {
		String avgTotalStr =
				peerContext.peerAvailable()
						? AnalysisMoneyFormatter.formatRoundedMoney(peerContext.avgTotal())
						: "0";
		String diffTotalStr =
				peerContext.peerAvailable()
						? AnalysisMoneyFormatter.formatRoundedMoney(
								thisSeries.total().subtract(peerContext.avgTotal()))
						: "0";

		return new AnalysisOverviewRes.CompareWithAverage(
				month,
				AnalysisMoneyFormatter.formatRoundedMoney(thisSeries.total()),
				avgTotalStr,
				diffTotalStr);
	}

	private AnalysisOverviewRes.CompareWithLastMonth buildCompareWithLastMonth(
			int month,
			AnalysisDailySeries thisSeries,
			AnalysisMonthRange prevRange,
			AnalysisDailySeries prevSeries) {
		int daysElapsed = thisSeries.items().size();
		int prevDays = prevSeries.items().size();
		BigDecimal prevMonthSameDayTotal = prevSeries.cumulativeAtSameDay(daysElapsed);
		BigDecimal diffWithLastMonth = thisSeries.total().subtract(prevMonthSameDayTotal);

		return new AnalysisOverviewRes.CompareWithLastMonth(
				AnalysisMoneyFormatter.formatRoundedMoney(diffWithLastMonth),
				month + "월",
				daysElapsed,
				prevRange.yearMonth().getMonthValue() + "월",
				prevDays,
				new AnalysisOverviewRes.CompareWithLastMonth.TotalSpent(
						AnalysisMoneyFormatter.formatRoundedMoney(thisSeries.total()),
						AnalysisMoneyFormatter.formatRoundedMoney(prevSeries.total())),
				AnalysisMoneyFormatter.formatRoundedMoney(thisSeries.total()),
				toDailySpentItems(thisSeries.items()),
				toDailySpentItems(prevSeries.items()));
	}

	private AnalysisOverviewRes.CompareByCategory buildCompareByCategory(
			Map<Category, BigDecimal> myCategoryMap, Map<Category, BigDecimal> avgByCategory) {
		List<AnalysisOverviewRes.CompareByCategory.CategoryItem> categoryItems = new ArrayList<>();
		int maxDiffCategoryIndex = -1;
		BigDecimal maxDiffAbs = BigDecimal.valueOf(-1);
		BigDecimal globalMaxAmount = BigDecimal.ZERO;
		BigDecimal totalMySpend = BigDecimal.ZERO;
		BigDecimal totalAvgSpend = BigDecimal.ZERO;

		for (Category category : Category.values()) {
			if (category == Category.INCOME || category == Category.UNCLASSIFIED) {
				continue;
			}

			BigDecimal mySpend = myCategoryMap.getOrDefault(category, BigDecimal.ZERO);
			BigDecimal avgSpend = avgByCategory.getOrDefault(category, BigDecimal.ZERO);
			totalMySpend = totalMySpend.add(mySpend);
			totalAvgSpend = totalAvgSpend.add(avgSpend);

			if (mySpend.compareTo(globalMaxAmount) > 0) {
				globalMaxAmount = mySpend;
			}
			if (avgSpend.compareTo(globalMaxAmount) > 0) {
				globalMaxAmount = avgSpend;
			}

			BigDecimal diffAbs = mySpend.subtract(avgSpend).abs();
			if (diffAbs.compareTo(maxDiffAbs) > 0) {
				maxDiffAbs = diffAbs;
				maxDiffCategoryIndex = category.ordinal();
			}

			categoryItems.add(
					new AnalysisOverviewRes.CompareByCategory.CategoryItem(
							category.ordinal(),
							AnalysisMoneyFormatter.formatRoundedMoney(mySpend),
							AnalysisMoneyFormatter.formatRoundedMoney(avgSpend)));
		}

		String maxLabel =
				AnalysisMoneyFormatter.formatRoundedMoney(
						globalMaxAmount.multiply(BigDecimal.valueOf(1.2)));
		boolean isOverSpent = totalMySpend.compareTo(totalAvgSpend) > 0;
		return new AnalysisOverviewRes.CompareByCategory(
				maxDiffCategoryIndex, isOverSpent, maxLabel, categoryItems);
	}

	private List<AnalysisOverviewRes.DailySpentItem> toDailySpentItems(
			List<AnalysisDailyRow> rows) {
		return rows.stream()
				.map(
						row ->
								new AnalysisOverviewRes.DailySpentItem(
										row.date(),
										AnalysisMoneyFormatter.formatRoundedMoney(
												row.cumulativeSpend())))
				.toList();
	}

	private AnalysisMetricType toAmountMetricType(CurrencyType currencyType) {
		if (currencyType == null) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
		}
		return currencyType == CurrencyType.BASE
				? AnalysisMetricType.TOTAL_BASE_AMOUNT
				: AnalysisMetricType.TOTAL_LOCAL_AMOUNT;
	}
}
