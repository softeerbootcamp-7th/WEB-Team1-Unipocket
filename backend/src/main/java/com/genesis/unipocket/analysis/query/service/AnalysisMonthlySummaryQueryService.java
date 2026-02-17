package com.genesis.unipocket.analysis.query.service;

import com.genesis.unipocket.analysis.command.config.AnalysisBatchProperties;
import com.genesis.unipocket.analysis.command.persistence.entity.AnalysisBatchJobStatus;
import com.genesis.unipocket.analysis.command.persistence.entity.AnalysisMetricType;
import com.genesis.unipocket.analysis.command.persistence.entity.AnalysisQualityType;
import com.genesis.unipocket.analysis.command.persistence.repository.AnalysisMonthlyDirtyRepository;
import com.genesis.unipocket.analysis.command.persistence.repository.support.AnalysisBatchAggregationRepository;
import com.genesis.unipocket.analysis.command.persistence.repository.support.AnalysisBatchAggregationRepository.AccountAmountCount;
import com.genesis.unipocket.analysis.command.persistence.repository.support.AnalysisBatchAggregationRepository.AccountCategoryAmountCount;
import com.genesis.unipocket.analysis.command.persistence.repository.support.AnalysisBatchAggregationRepository.AmountCount;
import com.genesis.unipocket.analysis.command.persistence.repository.support.AnalysisBatchAggregationRepository.CategoryAmountCount;
import com.genesis.unipocket.analysis.common.enums.CurrencyType;
import com.genesis.unipocket.analysis.common.util.QuantileUtil;
import com.genesis.unipocket.analysis.query.persistence.repository.AnalysisQueryRepository;
import com.genesis.unipocket.analysis.query.persistence.response.CategoryBreakdownRes;
import com.genesis.unipocket.analysis.query.persistence.response.MonthlySpendSummaryRes;
import com.genesis.unipocket.expense.command.facade.port.AccountBookOwnershipValidator;
import com.genesis.unipocket.global.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CountryCode;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.global.util.CountryCodeTimezoneMapper;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalysisMonthlySummaryQueryService {

	private static final int AVG_SCALE = 10;
	private static final MathContext MC = MathContext.DECIMAL64;
	private static final AnalysisQualityType PEER_QUALITY_TYPE = AnalysisQualityType.CLEANED;

	private final AnalysisQueryRepository analysisQueryRepository;
	private final AnalysisBatchAggregationRepository aggregationRepository;
	private final AnalysisMonthlyDirtyRepository monthlyDirtyRepository;
	private final AccountBookOwnershipValidator accountBookOwnershipValidator;
	private final AnalysisBatchProperties analysisBatchProperties;

	public MonthlySpendSummaryRes getMonthlySpendSummary(
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
		ZoneId zoneId = CountryCodeTimezoneMapper.getZoneId(localCountryCode);

		MonthRange thisRange = buildMonthRange(year, month, zoneId, true);
		MonthRange prevRange =
				buildMonthRange(
						thisRange.yearMonth().minusMonths(1).getYear(),
						thisRange.yearMonth().minusMonths(1).getMonthValue(),
						zoneId,
						false);

		DailySeries thisSeries =
				buildDailySeries(
						accountBookId,
						currencyType,
						thisRange.startUtc(),
						thisRange.endUtcExclusive(),
						thisRange.startLocalDate(),
						thisRange.endLocalDateInclusive());
		DailySeries prevSeries =
				buildDailySeries(
						accountBookId,
						currencyType,
						prevRange.startUtc(),
						prevRange.endUtcExclusive(),
						prevRange.startLocalDate(),
						prevRange.endLocalDateInclusive());

		PeerTotal peerTotal =
				resolvePeerAverageTotal(
						localCountryCode, baseCountryCode, thisRange, accountBookId, currencyType);
		String avgTotal =
				peerTotal.peerAvailable() ? formatRoundedMoney(peerTotal.avgTotal()) : null;
		String diff =
				peerTotal.peerAvailable()
						? formatRoundedMoney(thisSeries.total().subtract(peerTotal.avgTotal()))
						: null;

		return new MonthlySpendSummaryRes(
				accountBookId,
				String.valueOf(year),
				String.valueOf(month),
				currencyType,
				new MonthlySpendSummaryRes.MonthSection(
						thisRange.startLocalDate().toString(),
						thisRange.endLocalDateInclusive().toString(),
						thisSeries.items(),
						formatRoundedMoney(thisSeries.total())),
				new MonthlySpendSummaryRes.PrevMonthSection(
						String.valueOf(prevRange.yearMonth().getYear()),
						String.valueOf(prevRange.yearMonth().getMonthValue()),
						prevRange.startLocalDate().toString(),
						prevRange.endLocalDateInclusive().toString(),
						prevSeries.items(),
						formatRoundedMoney(prevSeries.total())),
				new MonthlySpendSummaryRes.Comparison(avgTotal, diff, peerTotal.peerAvailable()));
	}

	public CategoryBreakdownRes getCategoryBreakdown(
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
		ZoneId zoneId = CountryCodeTimezoneMapper.getZoneId(localCountryCode);

		MonthRange range = buildMonthRange(year, month, zoneId, true);
		MyCategorySnapshot mySnapshot =
				resolveMyCategorySnapshot(accountBookId, localCountryCode, range, currencyType);
		Map<Category, BigDecimal> myCategoryMap = mySnapshot.categoryMap();
		BigDecimal myTotal = mySnapshot.total();

		PeerCategoryAvg peerAvg =
				resolvePeerCategoryAverage(
						localCountryCode, baseCountryCode, range, accountBookId, currencyType);

		List<CategoryBreakdownRes.CategoryItem> categories = new ArrayList<>();
		for (Category category : Category.values()) {
			if (category == Category.INCOME) {
				continue;
			}
			BigDecimal mySpend = myCategoryMap.getOrDefault(category, BigDecimal.ZERO);
			String avgSpend =
					peerAvg.peerAvailable()
							? formatRoundedMoney(
									peerAvg.categoryAvgMap()
											.getOrDefault(category, BigDecimal.ZERO))
							: null;
			String diff =
					peerAvg.peerAvailable()
							? formatRoundedMoney(
									mySpend.subtract(
											peerAvg.categoryAvgMap()
													.getOrDefault(category, BigDecimal.ZERO)))
							: null;

			categories.add(
					new CategoryBreakdownRes.CategoryItem(
							category, formatRoundedMoney(mySpend), avgSpend, diff));
		}

		return new CategoryBreakdownRes(
				accountBookId,
				String.valueOf(year),
				String.valueOf(month),
				currencyType,
				categories,
				formatRoundedMoney(myTotal),
				peerAvg.peerAvailable() ? formatRoundedMoney(peerAvg.avgTotal()) : null,
				peerAvg.peerAvailable());
	}

	private PeerCategoryAvg resolvePeerCategoryAverage(
			CountryCode localCountryCode,
			CountryCode baseCountryCode,
			MonthRange range,
			Long accountBookId,
			CurrencyType currencyType) {
		AnalysisMetricType metricType = toAmountMetricType(currencyType);
		PeerAccountFilter peerFilter =
				resolvePeerAccountFilter(
						localCountryCode, baseCountryCode, range, accountBookId, metricType);
		if (!peerFilter.available()) {
			return PeerCategoryAvg.unavailable();
		}
		long peerAccountCount = peerFilter.filteredAccountIds().size();
		BigDecimal avgTotal = peerFilter.averageTotal();
		List<AccountCategoryAmountCount> peerCategoryRows =
				aggregationRepository.aggregatePeerMonthlyCategoryByAccountFromMonthly(
						localCountryCode,
						baseCountryCode,
						accountBookId,
						range.yearMonth().atDay(1),
						PEER_QUALITY_TYPE,
						currencyType);
		Map<Integer, BigDecimal> peerCategoryTotalMap = new HashMap<>();
		for (AccountCategoryAmountCount row : peerCategoryRows) {
			if (!peerFilter.filteredAccountIds().contains(row.accountBookId())) {
				continue;
			}
			if (row.categoryOrdinal() == null) {
				continue;
			}
			peerCategoryTotalMap.merge(row.categoryOrdinal(), row.totalAmount(), BigDecimal::add);
		}

		Map<Category, BigDecimal> avgByCategory = new EnumMap<>(Category.class);
		for (Category category : Category.values()) {
			if (category == Category.INCOME) {
				continue;
			}
			BigDecimal peerCategoryTotal =
					peerCategoryTotalMap.getOrDefault(category.ordinal(), BigDecimal.ZERO);
			avgByCategory.put(category, divide(peerCategoryTotal, peerAccountCount));
		}
		return PeerCategoryAvg.available(avgTotal, avgByCategory);
	}

	private PeerTotal resolvePeerAverageTotal(
			CountryCode localCountryCode,
			CountryCode baseCountryCode,
			MonthRange range,
			Long accountBookId,
			CurrencyType currencyType) {
		PeerAccountFilter peerFilter =
				resolvePeerAccountFilter(
						localCountryCode,
						baseCountryCode,
						range,
						accountBookId,
						toAmountMetricType(currencyType));
		if (!peerFilter.available()) {
			return PeerTotal.unavailable();
		}
		return PeerTotal.available(peerFilter.averageTotal());
	}

	private MyCategorySnapshot resolveMyCategorySnapshot(
			Long accountBookId,
			CountryCode localCountryCode,
			MonthRange range,
			CurrencyType currencyType) {
		if (isMonthlyBatchReady(accountBookId, localCountryCode, range, currencyType)) {
			AmountCount totalRow =
					aggregationRepository.aggregateAccountMonthlyFromMonthly(
							accountBookId,
							range.yearMonth().atDay(1),
							toAmountMetricType(currencyType),
							PEER_QUALITY_TYPE);
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
				if (category == Category.INCOME) {
					continue;
				}
				categoryMap.put(category, row.totalAmount());
			}
			return new MyCategorySnapshot(totalRow.totalAmount(), categoryMap);
		}

		Map<Category, BigDecimal> realtimeCategoryMap =
				toMyCategoryMap(
						analysisQueryRepository.getMyCategorySpent(
								accountBookId,
								range.startUtc(),
								range.endUtcExclusive(),
								currencyType));
		BigDecimal realtimeTotal =
				realtimeCategoryMap.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
		return new MyCategorySnapshot(realtimeTotal, realtimeCategoryMap);
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
			LocalDate endLocalDateInclusive) {
		List<Object[]> rows =
				analysisQueryRepository.getMyDailySpent(
						accountBookId, startUtc, endUtcExclusive, currencyType);
		Map<LocalDate, BigDecimal> dailyMap = toDailyAmountMap(rows);

		BigDecimal cumulative = BigDecimal.ZERO;
		List<MonthlySpendSummaryRes.DailyItem> items = new ArrayList<>();
		for (LocalDate date = startLocalDate;
				!date.isAfter(endLocalDateInclusive);
				date = date.plusDays(1)) {
			BigDecimal dailySpend = dailyMap.getOrDefault(date, BigDecimal.ZERO);
			cumulative = cumulative.add(dailySpend);
			items.add(
					new MonthlySpendSummaryRes.DailyItem(
							date.toString(),
							formatRoundedMoney(dailySpend),
							formatRoundedMoney(cumulative)));
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

	private Map<LocalDate, BigDecimal> toDailyAmountMap(List<Object[]> rows) {
		Map<LocalDate, BigDecimal> map = new HashMap<>();
		for (Object[] row : rows) {
			LocalDate date = toLocalDate(row[0]);
			map.put(date, toBigDecimal(row[1]));
		}
		return map;
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
						.withZoneSameInstant(ZoneId.of("UTC"))
						.toLocalDateTime();
		LocalDateTime endUtcExclusive =
				endLocalDate
						.plusDays(1)
						.atStartOfDay(zoneId)
						.withZoneSameInstant(ZoneId.of("UTC"))
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

	private AnalysisMetricType toAmountMetricType(CurrencyType currencyType) {
		if (currencyType == null) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
		}
		return currencyType == CurrencyType.BASE
				? AnalysisMetricType.TOTAL_BASE_AMOUNT
				: AnalysisMetricType.TOTAL_LOCAL_AMOUNT;
	}

	private PeerAccountFilter resolvePeerAccountFilter(
			CountryCode localCountryCode,
			CountryCode baseCountryCode,
			MonthRange range,
			Long accountBookId,
			AnalysisMetricType metricType) {
		List<AccountAmountCount> peerRows =
				aggregationRepository.aggregatePeerMonthlyTotalByAccountFromMonthly(
						localCountryCode,
						baseCountryCode,
						accountBookId,
						range.yearMonth().atDay(1),
						metricType,
						PEER_QUALITY_TYPE);
		if (peerRows.isEmpty()) {
			return PeerAccountFilter.unavailable();
		}
		List<BigDecimal> totals = peerRows.stream().map(AccountAmountCount::totalAmount).toList();
		Bounds bounds = computeIqrBounds(totals);
		List<AccountAmountCount> filteredRows =
				peerRows.stream()
						.filter(
								row ->
										bounds == null
												|| (row.totalAmount().compareTo(bounds.lower()) >= 0
														&& row.totalAmount()
																		.compareTo(bounds.upper())
																<= 0))
						.toList();
		List<AccountAmountCount> effectiveRows = filteredRows.isEmpty() ? peerRows : filteredRows;
		BigDecimal sum =
				effectiveRows.stream()
						.map(AccountAmountCount::totalAmount)
						.reduce(BigDecimal.ZERO, BigDecimal::add);
		BigDecimal average = divide(sum, effectiveRows.size());
		Set<Long> accountIds = new HashSet<>();
		for (AccountAmountCount row : effectiveRows) {
			accountIds.add(row.accountBookId());
		}
		return PeerAccountFilter.available(average, accountIds);
	}

	private BigDecimal divide(BigDecimal amount, long divisor) {
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

	private Bounds computeIqrBounds(List<BigDecimal> values) {
		if (values == null || values.size() < 4) {
			return null;
		}
		List<BigDecimal> sorted = new ArrayList<>(values);
		sorted.sort(Comparator.naturalOrder());
		BigDecimal q1 = QuantileUtil.linearInterpolatedQuantile(sorted, 0.25d, MC);
		BigDecimal q3 = QuantileUtil.linearInterpolatedQuantile(sorted, 0.75d, MC);
		BigDecimal iqr = q3.subtract(q1, MC);
		if (iqr.compareTo(BigDecimal.ZERO) < 0) {
			return null;
		}
		BigDecimal multiplier =
				BigDecimal.valueOf(analysisBatchProperties.getOutlierIqrMultiplier());
		BigDecimal delta = iqr.multiply(multiplier, MC);
		return new Bounds(q1.subtract(delta, MC), q3.add(delta, MC));
	}

	private LocalDate toLocalDate(Object value) {
		if (value instanceof LocalDate localDate) {
			return localDate;
		}
		if (value instanceof java.sql.Date sqlDate) {
			return sqlDate.toLocalDate();
		}
		return LocalDate.parse(value.toString());
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

	private record DailySeries(List<MonthlySpendSummaryRes.DailyItem> items, BigDecimal total) {}

	private record MyCategorySnapshot(BigDecimal total, Map<Category, BigDecimal> categoryMap) {}

	private record PeerCategoryAvg(
			boolean peerAvailable, BigDecimal avgTotal, Map<Category, BigDecimal> categoryAvgMap) {
		private static PeerCategoryAvg unavailable() {
			return new PeerCategoryAvg(false, null, Map.of());
		}

		private static PeerCategoryAvg available(
				BigDecimal avgTotal, Map<Category, BigDecimal> categoryAvgMap) {
			return new PeerCategoryAvg(true, avgTotal, categoryAvgMap);
		}
	}

	private record PeerTotal(boolean peerAvailable, BigDecimal avgTotal) {
		private static PeerTotal unavailable() {
			return new PeerTotal(false, null);
		}

		private static PeerTotal available(BigDecimal avgTotal) {
			return new PeerTotal(true, avgTotal);
		}
	}

	private record Bounds(BigDecimal lower, BigDecimal upper) {}

	private record PeerAccountFilter(
			boolean available, BigDecimal averageTotal, Set<Long> filteredAccountIds) {
		private static PeerAccountFilter unavailable() {
			return new PeerAccountFilter(false, null, Set.of());
		}

		private static PeerAccountFilter available(BigDecimal averageTotal, Set<Long> accountIds) {
			return new PeerAccountFilter(true, averageTotal, accountIds);
		}
	}
}
