package com.genesis.unipocket.analysis.query.service;

import com.genesis.unipocket.analysis.command.persistence.entity.AnalysisBatchJobStatus;
import com.genesis.unipocket.analysis.command.persistence.entity.AnalysisMetricType;
import com.genesis.unipocket.analysis.command.persistence.entity.AnalysisQualityType;
import com.genesis.unipocket.analysis.command.persistence.repository.AnalysisMonthlyDirtyRepository;
import com.genesis.unipocket.analysis.command.persistence.repository.support.AnalysisBatchAggregationRepository;
import com.genesis.unipocket.analysis.command.persistence.repository.support.AnalysisBatchAggregationRepository.AmountCount;
import com.genesis.unipocket.analysis.command.persistence.repository.support.AnalysisBatchAggregationRepository.CategoryAmountCount;
import com.genesis.unipocket.analysis.common.enums.CurrencyType;
import com.genesis.unipocket.analysis.query.persistence.repository.AnalysisQueryRepository;
import com.genesis.unipocket.analysis.query.persistence.response.CategoryBreakdownRes;
import com.genesis.unipocket.analysis.query.persistence.response.MonthlySpendSummaryRes;
import com.genesis.unipocket.expense.command.facade.port.AccountBookOwnershipValidator;
import com.genesis.unipocket.global.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CountryCode;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.global.util.AmountFormatUtil;
import com.genesis.unipocket.global.util.CountryCodeTimezoneMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
	private final AccountBookOwnershipValidator accountBookOwnershipValidator;

	public MonthlySpendSummaryRes getMonthlySpendSummary(
			UUID userId,
			Long accountBookId,
			int year,
			String monthText,
			CurrencyType currencyView) {
		accountBookOwnershipValidator.validateOwnership(accountBookId, userId.toString());

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
						currencyView,
						thisRange.startUtc(),
						thisRange.endUtcExclusive(),
						thisRange.startLocalDate(),
						thisRange.endLocalDateInclusive());
		DailySeries prevSeries =
				buildDailySeries(
						accountBookId,
						currencyView,
						prevRange.startUtc(),
						prevRange.endUtcExclusive(),
						prevRange.startLocalDate(),
						prevRange.endLocalDateInclusive());

		PeerTotal peerTotal =
				resolvePeerAverageTotal(
						localCountryCode, baseCountryCode, thisRange, accountBookId, currencyView);
		String avgTotal =
				peerTotal.peerAvailable() ? AmountFormatUtil.format(peerTotal.avgTotal()) : null;
		String diff =
				peerTotal.peerAvailable()
						? AmountFormatUtil.format(thisSeries.total().subtract(peerTotal.avgTotal()))
						: null;

		return new MonthlySpendSummaryRes(
				accountBookId,
				year,
				month + "월",
				currencyView,
				new MonthlySpendSummaryRes.MonthSection(
						thisRange.startLocalDate().toString(),
						thisRange.endLocalDateInclusive().toString(),
						thisSeries.items(),
						AmountFormatUtil.format(thisSeries.total())),
				new MonthlySpendSummaryRes.PrevMonthSection(
						prevRange.yearMonth().getYear(),
						prevRange.yearMonth().getMonthValue() + "월",
						prevRange.startLocalDate().toString(),
						prevRange.endLocalDateInclusive().toString(),
						prevSeries.items(),
						AmountFormatUtil.format(prevSeries.total())),
				new MonthlySpendSummaryRes.Comparison(avgTotal, diff, peerTotal.peerAvailable()));
	}

	public CategoryBreakdownRes getCategoryBreakdown(
			UUID userId,
			Long accountBookId,
			int year,
			String monthText,
			CurrencyType currencyView) {
		accountBookOwnershipValidator.validateOwnership(accountBookId, userId.toString());

		int month = parseMonth(monthText);
		Object[] countryCodes = analysisQueryRepository.getAccountBookCountryCodes(accountBookId);
		CountryCode localCountryCode = (CountryCode) countryCodes[0];
		CountryCode baseCountryCode = (CountryCode) countryCodes[1];
		ZoneId zoneId = CountryCodeTimezoneMapper.getZoneId(localCountryCode);

		MonthRange range = buildMonthRange(year, month, zoneId, true);
		MyCategorySnapshot mySnapshot =
				resolveMyCategorySnapshot(accountBookId, localCountryCode, range, currencyView);
		Map<Category, BigDecimal> myCategoryMap = mySnapshot.categoryMap();
		BigDecimal myTotal = mySnapshot.total();

		PeerCategoryAvg peerAvg =
				resolvePeerCategoryAverage(
						localCountryCode, baseCountryCode, range, accountBookId, currencyView);

		List<CategoryBreakdownRes.CategoryItem> categories = new ArrayList<>();
		for (Category category : Category.values()) {
			if (category == Category.INCOME) {
				continue;
			}
			BigDecimal mySpend = myCategoryMap.getOrDefault(category, BigDecimal.ZERO);
			String avgSpend =
					peerAvg.peerAvailable()
							? AmountFormatUtil.format(
									peerAvg.categoryAvgMap()
											.getOrDefault(category, BigDecimal.ZERO))
							: null;
			String diff =
					peerAvg.peerAvailable()
							? AmountFormatUtil.format(
									mySpend.subtract(
											peerAvg.categoryAvgMap()
													.getOrDefault(category, BigDecimal.ZERO)))
							: null;

			categories.add(
					new CategoryBreakdownRes.CategoryItem(
							category, AmountFormatUtil.format(mySpend), avgSpend, diff));
		}

		return new CategoryBreakdownRes(
				accountBookId,
				year,
				month + "월",
				currencyView,
				categories,
				AmountFormatUtil.format(myTotal),
				peerAvg.peerAvailable() ? AmountFormatUtil.format(peerAvg.avgTotal()) : null,
				peerAvg.peerAvailable());
	}

	private PeerCategoryAvg resolvePeerCategoryAverage(
			CountryCode localCountryCode,
			CountryCode baseCountryCode,
			MonthRange range,
			Long accountBookId,
			CurrencyType currencyView) {
		AnalysisMetricType metricType = toAmountMetricType(currencyView);
		AmountCount peerTotalRow =
				aggregationRepository.aggregatePeerMonthlyTotalFromMonthly(
						localCountryCode,
						baseCountryCode,
						accountBookId,
						range.yearMonth().atDay(1),
						metricType,
						PEER_QUALITY_TYPE);
		long peerAccountCount = peerTotalRow.expenseCount();
		if (peerAccountCount <= 0L) {
			return PeerCategoryAvg.unavailable();
		}
		BigDecimal avgTotal = divide(peerTotalRow.totalAmount(), peerAccountCount);

		List<CategoryAmountCount> peerCategoryRows =
				aggregationRepository.aggregatePeerMonthlyCategoryFromMonthly(
						localCountryCode,
						baseCountryCode,
						accountBookId,
						range.yearMonth().atDay(1),
						PEER_QUALITY_TYPE,
						currencyView);
		Map<Integer, BigDecimal> peerCategoryTotalMap = new HashMap<>();
		for (CategoryAmountCount row : peerCategoryRows) {
			if (row.categoryOrdinal() == null) {
				continue;
			}
			peerCategoryTotalMap.put(row.categoryOrdinal(), row.totalAmount());
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
			CurrencyType currencyView) {
		AmountCount peerTotalRow =
				aggregationRepository.aggregatePeerMonthlyTotalFromMonthly(
						localCountryCode,
						baseCountryCode,
						accountBookId,
						range.yearMonth().atDay(1),
						toAmountMetricType(currencyView),
						PEER_QUALITY_TYPE);
		long peerAccountCount = peerTotalRow.expenseCount();
		if (peerAccountCount <= 0L) {
			return PeerTotal.unavailable();
		}
		return PeerTotal.available(divide(peerTotalRow.totalAmount(), peerAccountCount));
	}

	private MyCategorySnapshot resolveMyCategorySnapshot(
			Long accountBookId,
			CountryCode localCountryCode,
			MonthRange range,
			CurrencyType currencyView) {
		if (isMonthlyBatchReady(accountBookId, localCountryCode, range, currencyView)) {
			AmountCount totalRow =
					aggregationRepository.aggregateAccountMonthlyFromMonthly(
							accountBookId,
							range.yearMonth().atDay(1),
							toAmountMetricType(currencyView),
							PEER_QUALITY_TYPE);
			List<CategoryAmountCount> categoryRows =
					aggregationRepository.aggregateAccountMonthlyCategoryFromMonthly(
							accountBookId,
							range.yearMonth().atDay(1),
							PEER_QUALITY_TYPE,
							currencyView);
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
								currencyView));
		BigDecimal realtimeTotal =
				realtimeCategoryMap.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
		return new MyCategorySnapshot(realtimeTotal, realtimeCategoryMap);
	}

	private boolean isMonthlyBatchReady(
			Long accountBookId,
			CountryCode localCountryCode,
			MonthRange range,
			CurrencyType currencyView) {
		if (isDirtyPending(accountBookId, localCountryCode, range.yearMonth().atDay(1))) {
			return false;
		}
		return aggregationRepository.hasAccountMonthlyAggregate(
				accountBookId,
				range.yearMonth().atDay(1),
				toAmountMetricType(currencyView),
				PEER_QUALITY_TYPE);
	}

	private boolean isDirtyPending(
			Long accountBookId, CountryCode localCountryCode, LocalDate monthStart) {
		return monthlyDirtyRepository.existsByCountryCodeAndAccountBookIdAndTargetYearMonthAndStatusNot(
				localCountryCode, accountBookId, monthStart, AnalysisBatchJobStatus.SUCCESS);
	}

	private DailySeries buildDailySeries(
			Long accountBookId,
			CurrencyType currencyView,
			LocalDateTime startUtc,
			LocalDateTime endUtcExclusive,
			LocalDate startLocalDate,
			LocalDate endLocalDateInclusive) {
		List<Object[]> rows =
				analysisQueryRepository.getMyDailySpent(
						accountBookId, startUtc, endUtcExclusive, currencyView);
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
							AmountFormatUtil.format(dailySpend),
							AmountFormatUtil.format(cumulative)));
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

	private int parseMonth(String monthText) {
		if (monthText == null || monthText.isBlank()) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
		}
		String normalized = monthText.trim();
		if (normalized.endsWith("월")) {
			normalized = normalized.substring(0, normalized.length() - 1);
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

	private AnalysisMetricType toAmountMetricType(CurrencyType currencyView) {
		if (currencyView == null) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
		}
		return currencyView == CurrencyType.BASE
				? AnalysisMetricType.TOTAL_BASE_AMOUNT
				: AnalysisMetricType.TOTAL_LOCAL_AMOUNT;
	}

	private BigDecimal divide(BigDecimal amount, long divisor) {
		return amount.divide(BigDecimal.valueOf(divisor), AVG_SCALE, RoundingMode.HALF_UP);
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
}
