package com.genesis.unipocket.analysis.query.service;

import com.genesis.unipocket.analysis.command.persistence.entity.AnalysisBatchJobStatus;
import com.genesis.unipocket.analysis.command.persistence.entity.AnalysisMetricType;
import com.genesis.unipocket.analysis.command.persistence.entity.AnalysisQualityType;
import com.genesis.unipocket.analysis.command.persistence.entity.PairMonthlyAggregateEntity;
import com.genesis.unipocket.analysis.command.persistence.entity.PairMonthlyCategoryAggregateEntity;
import com.genesis.unipocket.analysis.command.persistence.repository.AnalysisMonthlyDirtyRepository;
import com.genesis.unipocket.analysis.command.persistence.repository.PairMonthlyAggregateRepository;
import com.genesis.unipocket.analysis.command.persistence.repository.PairMonthlyCategoryAggregateRepository;
import com.genesis.unipocket.analysis.command.persistence.repository.support.AnalysisBatchAggregationRepository;
import com.genesis.unipocket.analysis.command.persistence.repository.support.AnalysisBatchAggregationRepository.CategoryAmountCount;
import com.genesis.unipocket.analysis.common.enums.CurrencyType;
import com.genesis.unipocket.analysis.common.util.OffsetDateTimeConverter;
import com.genesis.unipocket.analysis.query.persistence.repository.AnalysisQueryRepository;
import com.genesis.unipocket.analysis.query.persistence.response.AnalysisOverviewRes;
import com.genesis.unipocket.expense.command.facade.port.AccountBookOwnershipValidator;
import com.genesis.unipocket.global.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CountryCode;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.global.util.CountryCodeTimezoneMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalysisMonthlySummaryQueryService {

	private static final int AVG_SCALE = 10;
	private static final AnalysisQualityType PEER_QUALITY_TYPE = AnalysisQualityType.CLEANED;

	private final AnalysisQueryRepository analysisQueryRepository;
	private final AnalysisBatchAggregationRepository aggregationRepository;
	private final AnalysisMonthlyDirtyRepository monthlyDirtyRepository;
	private final PairMonthlyAggregateRepository pairMonthlyAggregateRepository;
	private final PairMonthlyCategoryAggregateRepository pairMonthlyCategoryAggregateRepository;
	private final AccountBookOwnershipValidator accountBookOwnershipValidator;

	public AnalysisOverviewRes getAnalysisOverview(
			UUID userId,
			Long accountBookId,
			String yearText,
			String monthText,
			CurrencyType currencyType) {
		accountBookOwnershipValidator.validateOwnership(accountBookId, userId.toString());

		int year = parseYear(yearText);
		int month = parseMonth(monthText);
		Object[] countryCodes = analysisQueryRepository.getAccountBookCountryCodes(accountBookId);
		CountryCode localCountryCode = (CountryCode) countryCodes[0];
		CountryCode baseCountryCode = (CountryCode) countryCodes[1];
		validateCountryCodes(localCountryCode, baseCountryCode, currencyType);
		ZoneId zoneId = CountryCodeTimezoneMapper.getZoneId(localCountryCode);
		validateNotFutureYearMonth(year, month, zoneId);

		MonthRange thisRange = buildMonthRange(year, month, zoneId, true);
		MonthRange prevRange =
				buildMonthRange(
						thisRange.yearMonth().minusMonths(1).getYear(),
						thisRange.yearMonth().minusMonths(1).getMonthValue(),
						zoneId,
						false);

		// 1. Monthly Summary Data
		DailySeries thisSeries =
				buildDailySeries(
						accountBookId,
						currencyType,
						thisRange.startUtc(),
						thisRange.endUtcExclusive(),
						thisRange.startLocalDate(),
						thisRange.endLocalDateInclusive(),
						zoneId);

		DailySeries prevSeries =
				buildDailySeries(
						accountBookId,
						currencyType,
						prevRange.startUtc(),
						prevRange.endUtcExclusive(),
						prevRange.startLocalDate(),
						prevRange.endLocalDateInclusive(),
						zoneId);

		// 2. Category Snapshot & Peer Context
		MyCategorySnapshot mySnapshot =
				resolveMyCategorySnapshot(accountBookId, localCountryCode, thisRange, currencyType);
		Map<Category, BigDecimal> myCategoryMap = mySnapshot.categoryMap();

		PairPeerContext peerContext =
				resolvePairPeerContext(
						localCountryCode,
						baseCountryCode,
						thisRange.yearMonth().atDay(1),
						toAmountMetricType(currencyType),
						thisSeries.total(),
						mySnapshot.monthlyBatchReady());

		Map<Category, BigDecimal> avgByCategory =
				resolvePairCategoryAverageMap(
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
			int month, DailySeries thisSeries, PairPeerContext peerContext) {
		String avgTotalStr =
				peerContext.peerAvailable() ? formatRoundedMoney(peerContext.avgTotal()) : "0";
		String diffTotalStr =
				peerContext.peerAvailable()
						? formatRoundedMoney(thisSeries.total().subtract(peerContext.avgTotal()))
						: "0";

		return new AnalysisOverviewRes.CompareWithAverage(
				month, formatRoundedMoney(thisSeries.total()), avgTotalStr, diffTotalStr);
	}

	private AnalysisOverviewRes.CompareWithLastMonth buildCompareWithLastMonth(
			int month, DailySeries thisSeries, MonthRange prevRange, DailySeries prevSeries) {
		int daysElapsed = thisSeries.items().size();
		int prevDays = prevSeries.items().size();
		BigDecimal prevMonthSameDayTotal = prevSeries.cumulativeAtSameDay(daysElapsed);
		BigDecimal diffWithLastMonth = thisSeries.total().subtract(prevMonthSameDayTotal);

		return new AnalysisOverviewRes.CompareWithLastMonth(
				formatRoundedMoney(diffWithLastMonth),
				month + "월",
				daysElapsed,
				prevRange.yearMonth().getMonthValue() + "월",
				prevDays,
				new AnalysisOverviewRes.CompareWithLastMonth.TotalSpent(
						formatRoundedMoney(thisSeries.total()),
						formatRoundedMoney(prevSeries.total())),
				formatRoundedMoney(thisSeries.total()),
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
							formatRoundedMoney(mySpend),
							formatRoundedMoney(avgSpend)));
		}

		String maxLabel = formatRoundedMoney(globalMaxAmount.multiply(BigDecimal.valueOf(1.2)));
		boolean isOverSpent = totalMySpend.compareTo(totalAvgSpend) > 0;
		return new AnalysisOverviewRes.CompareByCategory(
				maxDiffCategoryIndex, isOverSpent, maxLabel, categoryItems);
	}

	private List<AnalysisOverviewRes.DailySpentItem> toDailySpentItems(List<DailyRow> rows) {
		return rows.stream()
				.map(
						row ->
								new AnalysisOverviewRes.DailySpentItem(
										row.date(), formatRoundedMoney(row.cumulativeSpend())))
				.toList();
	}

	private Map<Category, BigDecimal> resolvePairCategoryAverageMap(
			CountryCode localCountryCode,
			CountryCode baseCountryCode,
			LocalDate monthStart,
			CurrencyType currencyType,
			PairPeerContext peerContext,
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

	private PairPeerContext resolvePairPeerContext(
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
			return PairPeerContext.unavailable();
		}

		PairMonthlyAggregateEntity row = optional.get();
		long includedAccountCount = row.getIncludedAccountCount();
		if (includedAccountCount <= 0L) {
			return PairPeerContext.unavailable();
		}

		boolean myIncluded =
				myMonthlyReady
						&& isWithinBounds(
								myTotalAmount, row.getIqrLowerBound(), row.getIqrUpperBound());

		BigDecimal totalSum = row.getTotalMetricSum();
		long effectivePeerCount = includedAccountCount;
		if (myIncluded) {
			if (includedAccountCount <= 1L) {
				return PairPeerContext.unavailable();
			}
			effectivePeerCount = includedAccountCount - 1L;
			totalSum = totalSum.subtract(myTotalAmount);
			if (totalSum.compareTo(BigDecimal.ZERO) < 0) {
				totalSum = BigDecimal.ZERO;
			}
		}
		if (effectivePeerCount <= 0L) {
			return PairPeerContext.unavailable();
		}
		return PairPeerContext.available(
				divide(totalSum, effectivePeerCount), effectivePeerCount, myIncluded);
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

	private MyCategorySnapshot resolveMyCategorySnapshot(
			Long accountBookId,
			CountryCode localCountryCode,
			MonthRange range,
			CurrencyType currencyType) {
		boolean monthlyBatchReady =
				isMonthlyBatchReady(accountBookId, localCountryCode, range, currencyType);
		if (monthlyBatchReady) {
			List<CategoryAmountCount> categoryRows =
					aggregationRepository.aggregateAccountMonthlyCategoryFromMonthly(
							accountBookId,
							range.yearMonth().atDay(1),
							PEER_QUALITY_TYPE,
							currencyType);
			Map<Category, BigDecimal> categoryMap = new EnumMap<>(Category.class);
			for (CategoryAmountCount row : categoryRows) {
				if (row.categoryOrdinal() == null) {
					continue;
				}
				if (row.categoryOrdinal() < 0
						|| row.categoryOrdinal() >= Category.values().length) {
					continue;
				}
				Category category = Category.values()[row.categoryOrdinal()];
				if (category == Category.INCOME || category == Category.UNCLASSIFIED) {
					continue;
				}
				categoryMap.put(category, row.totalAmount());
			}
			return new MyCategorySnapshot(categoryMap, true);
		}

		Map<Category, BigDecimal> realtimeCategoryMap =
				toMyCategoryMap(
						analysisQueryRepository.getMyCategorySpent(
								accountBookId,
								toOffsetUtc(range.startUtc()),
								toOffsetUtc(range.endUtcExclusive()),
								currencyType));
		return new MyCategorySnapshot(realtimeCategoryMap, false);
	}

	private boolean isMonthlyBatchReady(
			Long accountBookId,
			CountryCode localCountryCode,
			MonthRange range,
			CurrencyType currencyType) {
		if (isDirtyPending(accountBookId, localCountryCode, range.yearMonth().atDay(1))) {
			return false;
		}
		return aggregationRepository.hasAccountMonthlyAggregate(
				accountBookId,
				range.yearMonth().atDay(1),
				toAmountMetricType(currencyType),
				PEER_QUALITY_TYPE);
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

	private DailySeries buildDailySeries(
			Long accountBookId,
			CurrencyType currencyType,
			LocalDateTime startUtc,
			LocalDateTime endUtcExclusive,
			LocalDate startLocalDate,
			LocalDate endLocalDateInclusive,
			ZoneId zoneId) {
		Map<LocalDate, BigDecimal> dailyMap;
		try (Stream<Object[]> stream =
				analysisQueryRepository.getMySpendEvents(
						accountBookId,
						toOffsetUtc(startUtc),
						toOffsetUtc(endUtcExclusive),
						currencyType)) {
			dailyMap = toDailyAmountMap(stream, zoneId);
		}

		BigDecimal cumulative = BigDecimal.ZERO;
		List<DailyRow> items = new ArrayList<>();
		for (LocalDate date = startLocalDate;
				!date.isAfter(endLocalDateInclusive);
				date = date.plusDays(1)) {
			BigDecimal dailySpend = dailyMap.getOrDefault(date, BigDecimal.ZERO);
			cumulative = cumulative.add(dailySpend);
			items.add(new DailyRow(date.toString(), cumulative));
		}
		return new DailySeries(items, cumulative);
	}

	private Map<Category, BigDecimal> toMyCategoryMap(List<Object[]> rows) {
		Map<Category, BigDecimal> result = new EnumMap<>(Category.class);
		for (Object[] row : rows) {
			Category category = (Category) row[0];
			result.put(category, toBigDecimal(row[1]));
		}
		return result;
	}

	private Map<LocalDate, BigDecimal> toDailyAmountMap(Stream<Object[]> stream, ZoneId zoneId) {
		return stream.collect(
				Collectors.groupingBy(
						row -> toLocalDateInZone(row[0], zoneId),
						Collectors.reducing(
								BigDecimal.ZERO, row -> toBigDecimal(row[1]), BigDecimal::add)));
	}

	private LocalDate toLocalDateInZone(Object occurredAt, ZoneId zoneId) {
		OffsetDateTime offsetDateTime = OffsetDateTimeConverter.from(occurredAt);
		return offsetDateTime.atZoneSameInstant(zoneId).toLocalDate();
	}

	private OffsetDateTime toOffsetUtc(LocalDateTime localDateTime) {
		return localDateTime.atOffset(ZoneOffset.UTC);
	}

	private MonthRange buildMonthRange(
			int year, int month, ZoneId zoneId, boolean trimCurrentMonth) {
		YearMonth yearMonth = YearMonth.of(year, month);
		LocalDate startLocalDate = yearMonth.atDay(1);
		LocalDate endLocalDate = yearMonth.atEndOfMonth();
		LocalDate today = LocalDate.now(zoneId);
		if (trimCurrentMonth && year == today.getYear() && month == today.getMonthValue()) {
			endLocalDate = today;
		}

		LocalDateTime startUtc =
				startLocalDate
						.atStartOfDay(zoneId)
						.withZoneSameInstant(ZoneOffset.UTC)
						.toLocalDateTime();
		LocalDateTime endUtcExclusive =
				endLocalDate
						.plusDays(1)
						.atStartOfDay(zoneId)
						.withZoneSameInstant(ZoneOffset.UTC)
						.toLocalDateTime();

		return new MonthRange(yearMonth, startLocalDate, endLocalDate, startUtc, endUtcExclusive);
	}

	private int parseYear(String yearText) {
		if (yearText == null || yearText.isBlank()) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
		}
		String normalized = yearText.trim();
		if (!normalized.matches("\\d{4}")) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
		}
		try {
			return Integer.parseInt(normalized);
		} catch (NumberFormatException e) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
		}
	}

	private int parseMonth(String monthText) {
		if (monthText == null || monthText.isBlank()) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
		}
		String normalized = monthText.trim();
		if (!normalized.matches("\\d{1,2}")) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
		}
		try {
			int month = Integer.parseInt(normalized);
			if (month < 1 || month > 12) {
				throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
			}
			return month;
		} catch (NumberFormatException e) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
		}
	}

	private void validateNotFutureYearMonth(int year, int month, ZoneId zoneId) {
		YearMonth requested = YearMonth.of(year, month);
		YearMonth current = YearMonth.from(LocalDate.now(zoneId));
		if (requested.isAfter(current)) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
		}
	}

	private AnalysisMetricType toAmountMetricType(CurrencyType currencyType) {
		if (currencyType == null) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
		}
		return currencyType == CurrencyType.BASE
				? AnalysisMetricType.TOTAL_BASE_AMOUNT
				: AnalysisMetricType.TOTAL_LOCAL_AMOUNT;
	}

	private BigDecimal divide(BigDecimal amount, long divisor) {
		if (divisor <= 0L) {
			return BigDecimal.ZERO;
		}
		return amount.divide(BigDecimal.valueOf(divisor), AVG_SCALE, RoundingMode.HALF_UP);
	}

	private String formatRoundedMoney(BigDecimal amount) {
		if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) {
			return "0";
		}
		BigDecimal rounded = amount.setScale(2, RoundingMode.HALF_UP);
		if (rounded.stripTrailingZeros().scale() <= 0) {
			return rounded.toBigInteger().toString();
		}
		return rounded.toPlainString();
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

	private record MonthRange(
			YearMonth yearMonth,
			LocalDate startLocalDate,
			LocalDate endLocalDateInclusive,
			LocalDateTime startUtc,
			LocalDateTime endUtcExclusive) {}

	private record DailySeries(List<DailyRow> items, BigDecimal total) {
		private BigDecimal cumulativeAtSameDay(int daysElapsed) {
			if (items.isEmpty() || daysElapsed <= 0) {
				return BigDecimal.ZERO;
			}
			int targetIndex = Math.min(daysElapsed, items.size()) - 1;
			return items.get(targetIndex).cumulativeSpend();
		}
	}

	private record DailyRow(String date, BigDecimal cumulativeSpend) {}

	private record MyCategorySnapshot(
			Map<Category, BigDecimal> categoryMap, boolean monthlyBatchReady) {}

	private record PairPeerContext(
			boolean peerAvailable,
			BigDecimal avgTotal,
			long effectivePeerCount,
			boolean myIncluded) {
		private static PairPeerContext unavailable() {
			return new PairPeerContext(false, null, 0L, false);
		}

		private static PairPeerContext available(
				BigDecimal avgTotal, long effectivePeerCount, boolean myIncluded) {
			return new PairPeerContext(true, avgTotal, effectivePeerCount, myIncluded);
		}
	}
}
