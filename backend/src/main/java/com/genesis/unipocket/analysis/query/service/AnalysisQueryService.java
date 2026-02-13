package com.genesis.unipocket.analysis.query.service;

import com.genesis.unipocket.analysis.common.enums.CurrencyType;
import com.genesis.unipocket.analysis.query.persistence.repository.AnalysisQueryRepository;
import com.genesis.unipocket.analysis.query.persistence.response.AccountBookAnalysisRes;
import com.genesis.unipocket.analysis.query.persistence.response.CompareByCategoryRes;
import com.genesis.unipocket.analysis.query.persistence.response.CompareByCategoryRes.CategoryItem;
import com.genesis.unipocket.analysis.query.persistence.response.CompareWithAverageRes;
import com.genesis.unipocket.analysis.query.persistence.response.CompareWithLastMonthRes;
import com.genesis.unipocket.analysis.query.persistence.response.CompareWithLastMonthRes.DailyItem;
import com.genesis.unipocket.global.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CountryCode;
import com.genesis.unipocket.global.util.AmountFormatUtil;
import com.genesis.unipocket.global.util.CountryCodeTimezoneMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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
public class AnalysisQueryService {

	private static final int AVG_SCALE = 10;

	private final AnalysisQueryRepository repository;

	public AccountBookAnalysisRes getAnalysis(
			UUID userId, Long accountBookId, CurrencyType currencyType, int year, int month) {

		Object[] countryCodes = repository.getAccountBookCountryCodes(accountBookId);
		CountryCode localCountryCode = (CountryCode) countryCodes[0];
		CountryCode baseCountryCode = (CountryCode) countryCodes[1];
		CountryCode comparisonCountryCode =
				currencyType == CurrencyType.BASE ? baseCountryCode : localCountryCode;
		String userIdStr = userId.toString();

		ZoneId zoneId = CountryCodeTimezoneMapper.getZoneId(localCountryCode);

		LocalDateTime start = toUtcDateTime(LocalDate.of(year, month, 1), zoneId);
		LocalDateTime end = toUtcDateTime(LocalDate.of(year, month, 1).plusMonths(1), zoneId);

		Object[] otherData =
				repository.getOtherUsersTotalAndCount(
						comparisonCountryCode, userIdStr, start, end, currencyType);
		BigDecimal otherTotal = toBigDecimal(otherData[0]);
		long otherCount = ((Number) otherData[1]).longValue();

		CompareWithAverageRes compareWithAverage =
				buildCompareWithAverage(
						accountBookId, start, end, month, currencyType, otherTotal, otherCount);

		CompareWithLastMonthRes compareWithLastMonth =
				buildCompareWithLastMonth(
						accountBookId, start, end, year, month, currencyType, zoneId);

		CompareByCategoryRes compareByCategory =
				buildCompareByCategory(
						accountBookId,
						comparisonCountryCode,
						userIdStr,
						start,
						end,
						currencyType,
						otherCount);

		return new AccountBookAnalysisRes(
				comparisonCountryCode, compareWithAverage, compareWithLastMonth, compareByCategory);
	}

	private CompareWithAverageRes buildCompareWithAverage(
			Long accountBookId,
			LocalDateTime start,
			LocalDateTime end,
			int month,
			CurrencyType currencyType,
			BigDecimal otherTotal,
			long otherCount) {

		BigDecimal myTotal = repository.getMyMonthlyTotal(accountBookId, start, end, currencyType);
		BigDecimal avgAmount =
				otherCount > 0
						? otherTotal.divide(
								BigDecimal.valueOf(otherCount), AVG_SCALE, RoundingMode.HALF_UP)
						: BigDecimal.ZERO;
		BigDecimal diff = myTotal.subtract(avgAmount);

		return new CompareWithAverageRes(
				month,
				AmountFormatUtil.format(myTotal),
				AmountFormatUtil.format(avgAmount),
				AmountFormatUtil.format(diff));
	}

	private CompareWithLastMonthRes buildCompareWithLastMonth(
			Long accountBookId,
			LocalDateTime thisMonthStart,
			LocalDateTime thisMonthEnd,
			int year,
			int month,
			CurrencyType currencyType,
			ZoneId zoneId) {

		YearMonth thisYearMonth = YearMonth.of(year, month);
		YearMonth prevYearMonth = thisYearMonth.minusMonths(1);

		LocalDate prevLocalStart = prevYearMonth.atDay(1);
		LocalDateTime prevStart = toUtcDateTime(prevLocalStart, zoneId);
		LocalDateTime prevEnd = thisMonthStart;

		LocalDate today = LocalDate.now(zoneId);
		boolean isCurrentMonth = year == today.getYear() && month == today.getMonthValue();

		LocalDate thisEndDate = isCurrentMonth ? today : thisYearMonth.atEndOfMonth();

		LocalDateTime thisQueryEnd =
				isCurrentMonth ? toUtcDateTime(today.plusDays(1), zoneId) : thisMonthEnd;

		List<Object[]> thisRaw =
				repository.getMyDailySpent(
						accountBookId, thisMonthStart, thisQueryEnd, currencyType);
		List<Object[]> prevRaw =
				repository.getMyDailySpent(accountBookId, prevStart, prevEnd, currencyType);

		Map<LocalDate, BigDecimal> thisDailyMap = toDailyMap(thisRaw);
		Map<LocalDate, BigDecimal> prevDailyMap = toDailyMap(prevRaw);

		LocalDate thisStartDate = thisYearMonth.atDay(1);
		LocalDate prevStartDate = prevYearMonth.atDay(1);
		LocalDate prevEndDate = prevYearMonth.atEndOfMonth();

		List<BigDecimal> thisCum = computeCumulative(thisDailyMap, thisStartDate, thisEndDate);
		List<BigDecimal> prevCum = computeCumulative(prevDailyMap, prevStartDate, prevEndDate);

		List<DailyItem> thisMonthItems = buildDailyItems(thisCum, thisStartDate);
		List<DailyItem> prevMonthItems = buildDailyItems(prevCum, prevStartDate);

		BigDecimal rawThisMonthToDate =
				thisCum.isEmpty() ? BigDecimal.ZERO : thisCum.get(thisCum.size() - 1);

		int sameDay = isCurrentMonth ? today.getDayOfMonth() : thisEndDate.getDayOfMonth();
		int clampedDay = Math.min(sameDay, prevYearMonth.lengthOfMonth());
		BigDecimal rawLastMonthToSameDay =
				prevCum.isEmpty() ? BigDecimal.ZERO : prevCum.get(clampedDay - 1);

		BigDecimal rawDiff = rawThisMonthToDate.subtract(rawLastMonthToSameDay);

		String thisMonthStr = String.valueOf(month);
		String lastMonthStr = String.valueOf(prevYearMonth.getMonthValue());

		return new CompareWithLastMonthRes(
				AmountFormatUtil.format(rawDiff),
				thisMonthStr,
				thisMonthItems.size(),
				lastMonthStr,
				prevMonthItems.size(),
				AmountFormatUtil.format(rawThisMonthToDate),
				thisMonthItems,
				prevMonthItems);
	}

	private CompareByCategoryRes buildCompareByCategory(
			Long accountBookId,
			CountryCode countryCode,
			String userId,
			LocalDateTime start,
			LocalDateTime end,
			CurrencyType currencyType,
			long otherCount) {

		List<Object[]> myRows =
				repository.getMyCategorySpent(accountBookId, start, end, currencyType);
		Map<Category, BigDecimal> myMap = new EnumMap<>(Category.class);
		for (Object[] row : myRows) {
			myMap.put((Category) row[0], toBigDecimal(row[1]));
		}

		List<Object[]> otherRows =
				repository.getOtherUsersCategoryTotal(
						countryCode, userId, start, end, currencyType);
		Map<Integer, BigDecimal> otherTotalMap = new HashMap<>();
		for (Object[] row : otherRows) {
			otherTotalMap.put(((Number) row[0]).intValue(), toBigDecimal(row[1]));
		}

		List<CategoryItem> items = new ArrayList<>();
		BigDecimal maxVal = BigDecimal.ZERO;
		BigDecimal maxAbsDiff = BigDecimal.ZERO;
		int maxDiffCategoryIndex = 0;
		boolean isOverSpent = false;
		boolean first = true;

		for (Category cat : Category.values()) {
			if (cat == Category.INCOME) {
				continue;
			}

			int catIndex = cat.ordinal();
			BigDecimal myAmount = myMap.getOrDefault(cat, BigDecimal.ZERO);
			BigDecimal otherCatTotal = otherTotalMap.getOrDefault(catIndex, BigDecimal.ZERO);
			BigDecimal avgAmount =
					otherCount > 0
							? otherCatTotal.divide(
									BigDecimal.valueOf(otherCount), AVG_SCALE, RoundingMode.HALF_UP)
							: BigDecimal.ZERO;

			items.add(
					new CategoryItem(
							catIndex,
							AmountFormatUtil.format(myAmount),
							AmountFormatUtil.format(avgAmount)));

			BigDecimal diff = myAmount.subtract(avgAmount);
			BigDecimal absDiff = diff.abs();
			if (first || absDiff.compareTo(maxAbsDiff) > 0) {
				maxAbsDiff = absDiff;
				maxDiffCategoryIndex = catIndex;
				isOverSpent = diff.compareTo(BigDecimal.ZERO) > 0;
				first = false;
			}

			BigDecimal localMax = myAmount.max(avgAmount);
			if (localMax.compareTo(maxVal) > 0) {
				maxVal = localMax;
			}
		}

		items.sort(
				(a, b) -> {
					boolean aUnc = a.categoryIndex() == Category.UNCLASSIFIED.ordinal();
					boolean bUnc = b.categoryIndex() == Category.UNCLASSIFIED.ordinal();
					if (aUnc && !bUnc) return 1;
					if (!aUnc && bUnc) return -1;
					return new BigDecimal(b.mySpentAmount())
							.compareTo(new BigDecimal(a.mySpentAmount()));
				});

		BigDecimal maxLabelBase = maxVal.divide(BigDecimal.valueOf(5), 0, RoundingMode.DOWN);
		String maxLabel = maxLabelBase.multiply(BigDecimal.valueOf(6)).toPlainString();

		return new CompareByCategoryRes(maxDiffCategoryIndex, isOverSpent, maxLabel, items);
	}

	// ── helpers ──────────────────────────────────────────

	private static LocalDateTime toUtcDateTime(LocalDate localDate, ZoneId zoneId) {
		ZonedDateTime zoned = localDate.atStartOfDay(zoneId);
		return zoned.withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime();
	}

	private Map<LocalDate, BigDecimal> toDailyMap(List<Object[]> rows) {
		Map<LocalDate, BigDecimal> map = new HashMap<>();
		for (Object[] row : rows) {
			LocalDate date = toLocalDate(row[0]);
			map.put(date, toBigDecimal(row[1]));
		}
		return map;
	}

	private List<BigDecimal> computeCumulative(
			Map<LocalDate, BigDecimal> dailyMap, LocalDate startDate, LocalDate endDate) {
		List<BigDecimal> cumulated = new ArrayList<>();
		BigDecimal running = BigDecimal.ZERO;
		for (LocalDate d = startDate; !d.isAfter(endDate); d = d.plusDays(1)) {
			running = running.add(dailyMap.getOrDefault(d, BigDecimal.ZERO));
			cumulated.add(running);
		}
		return cumulated;
	}

	private List<DailyItem> buildDailyItems(List<BigDecimal> cumulated, LocalDate startDate) {
		List<DailyItem> items = new ArrayList<>(cumulated.size());
		for (int i = 0; i < cumulated.size(); i++) {
			LocalDate date = startDate.plusDays(i);
			items.add(new DailyItem(date.toString(), AmountFormatUtil.format(cumulated.get(i))));
		}
		return items;
	}

	private LocalDate toLocalDate(Object value) {
		if (value instanceof LocalDate ld) {
			return ld;
		}
		if (value instanceof java.sql.Date d) {
			return d.toLocalDate();
		}
		return LocalDate.parse(value.toString());
	}

	private BigDecimal toBigDecimal(Object value) {
		if (value instanceof BigDecimal bd) {
			return bd;
		}
		return new BigDecimal(value.toString());
	}
}
