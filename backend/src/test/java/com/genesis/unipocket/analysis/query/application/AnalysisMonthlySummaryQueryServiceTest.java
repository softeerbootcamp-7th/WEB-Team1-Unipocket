package com.genesis.unipocket.analysis.query.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.genesis.unipocket.analysis.common.enums.CurrencyType;
import com.genesis.unipocket.analysis.query.facade.port.AccountBookOwnershipValidator;
import com.genesis.unipocket.analysis.query.persistence.repository.AnalysisQueryRepository;
import com.genesis.unipocket.analysis.query.persistence.response.AnalysisOverviewRes;
import com.genesis.unipocket.global.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CountryCode;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AnalysisMonthlySummaryQueryServiceTest {

	@Mock private AnalysisQueryRepository analysisQueryRepository;
	@Mock private AnalysisMonthlyDailySeriesBuilder dailySeriesBuilder;
	@Mock private AnalysisMonthlyCategorySnapshotResolver categorySnapshotResolver;
	@Mock private AnalysisPeerComparisonResolver peerComparisonResolver;
	@Mock private AccountBookOwnershipValidator accountBookOwnershipValidator;

	@InjectMocks private AnalysisMonthlySummaryQueryService service;

	@Test
	void getAnalysisOverview_invalidYear_throwsInvalidInputValue() {
		assertThatThrownBy(
						() ->
								service.getAnalysisOverview(
										UUID.randomUUID(), 1L, "20A6", "12", CurrencyType.BASE))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("code", ErrorCode.INVALID_INPUT_VALUE);
	}

	@Test
	void getAnalysisOverview_futureYearMonth_throwsInvalidInputValue() {
		UUID userId = UUID.randomUUID();
		Long accountBookId = 99L;
		YearMonth nextMonth = YearMonth.now(ZoneId.of("America/New_York")).plusMonths(1);

		when(analysisQueryRepository.getAccountBookCountryCodes(accountBookId))
				.thenReturn(new Object[] {CountryCode.US, CountryCode.KR});

		assertThatThrownBy(
						() ->
								service.getAnalysisOverview(
										userId,
										accountBookId,
										String.valueOf(nextMonth.getYear()),
										String.valueOf(nextMonth.getMonthValue()),
										CurrencyType.BASE))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("code", ErrorCode.INVALID_INPUT_VALUE);
	}

	@Test
	void getAnalysisOverview_baseCurrencyAndMissingBaseCountry_throwsInvalidInputValue() {
		Long accountBookId = 88L;

		when(analysisQueryRepository.getAccountBookCountryCodes(accountBookId))
				.thenReturn(new Object[] {CountryCode.US, null});

		assertThatThrownBy(
						() ->
								service.getAnalysisOverview(
										UUID.randomUUID(),
										accountBookId,
										"2025",
										"12",
										CurrencyType.BASE))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("code", ErrorCode.INVALID_INPUT_VALUE);
	}

	@Test
	void getAnalysisOverview_success_returnsCombinedData() {
		UUID userId = UUID.randomUUID();
		Long accountBookId = 12L;

		when(analysisQueryRepository.getAccountBookCountryCodes(accountBookId))
				.thenReturn(new Object[] {CountryCode.US, CountryCode.KR});

		AnalysisDailySeries thisSeries =
				new AnalysisDailySeries(
						List.of(
								new AnalysisDailyRow("2025-12-01", new BigDecimal("100")),
								new AnalysisDailyRow("2025-12-02", new BigDecimal("500"))),
						new BigDecimal("500"));
		AnalysisDailySeries prevSeries =
				new AnalysisDailySeries(
						List.of(
								new AnalysisDailyRow("2025-11-01", new BigDecimal("50")),
								new AnalysisDailyRow("2025-11-02", new BigDecimal("100"))),
						new BigDecimal("100"));

		when(dailySeriesBuilder.build(
						eq(accountBookId),
						eq(CurrencyType.BASE),
						any(),
						any(),
						any(),
						any(),
						any(),
						eq(CurrencyCode.USD)))
				.thenReturn(thisSeries, prevSeries);

		Map<Category, BigDecimal> myCategoryMap = new EnumMap<>(Category.class);
		myCategoryMap.put(Category.FOOD, new BigDecimal("200"));
		myCategoryMap.put(Category.LIVING, new BigDecimal("300"));
		when(categorySnapshotResolver.resolve(
						eq(accountBookId),
						eq(CountryCode.US),
						any(),
						eq(CurrencyType.BASE),
						eq(CurrencyCode.USD)))
				.thenReturn(new AnalysisMyCategorySnapshot(myCategoryMap, true));

		when(peerComparisonResolver.resolvePairPeerContext(
						eq(CountryCode.US),
						eq(CountryCode.KR),
						any(),
						any(),
						eq(new BigDecimal("500")),
						eq(true)))
				.thenReturn(AnalysisPairPeerContext.available(new BigDecimal("375"), 4L, true));

		Map<Category, BigDecimal> avgByCategory = new EnumMap<>(Category.class);
		avgByCategory.put(Category.FOOD, new BigDecimal("200"));
		avgByCategory.put(Category.LIVING, new BigDecimal("175"));
		when(peerComparisonResolver.resolvePairCategoryAverageMap(
						eq(CountryCode.US),
						eq(CountryCode.KR),
						any(),
						eq(CurrencyType.BASE),
						any(),
						anyMap()))
				.thenReturn(avgByCategory);

		AnalysisOverviewRes res =
				service.getAnalysisOverview(userId, accountBookId, "2025", "12", CurrencyType.BASE);

		assertThat(res.countryCode()).isEqualTo("KR");
		assertThat(res.compareWithAverage().mySpentAmount()).isEqualTo("500");
		assertThat(res.compareWithAverage().averageSpentAmount()).isEqualTo("375");
		assertThat(res.compareWithAverage().spentAmountDiff()).isEqualTo("125");
		assertThat(res.compareWithLastMonth().diff()).isEqualTo("400");
		assertThat(res.compareWithLastMonth().totalSpent().thisMonthToDate()).isEqualTo("500");
		assertThat(res.compareWithLastMonth().totalSpent().lastMonthTotal()).isEqualTo("100");
		assertThat(res.compareByCategory().isOverSpent()).isTrue();
		assertThat(res.compareByCategory().maxDiffCategoryIndex())
				.isEqualTo(Category.LIVING.ordinal());

		AnalysisOverviewRes.CompareByCategory.CategoryItem livingItem =
				res.compareByCategory().items().stream()
						.filter(i -> i.categoryIndex() == Category.LIVING.ordinal())
						.findFirst()
						.orElseThrow();
		assertThat(livingItem.mySpentAmount()).isEqualTo("300");
		assertThat(livingItem.averageSpentAmount()).isEqualTo("175");
	}

	@Test
	void getAnalysisOverview_localCurrencyType_usesResolvedSeriesAndSnapshot() {
		UUID userId = UUID.randomUUID();
		Long accountBookId = 30L;

		when(analysisQueryRepository.getAccountBookCountryCodes(accountBookId))
				.thenReturn(new Object[] {CountryCode.US, CountryCode.KR});

		AnalysisDailySeries thisSeries =
				new AnalysisDailySeries(
						List.of(new AnalysisDailyRow("2025-12-01", new BigDecimal("300"))),
						new BigDecimal("300"));
		AnalysisDailySeries prevSeries = new AnalysisDailySeries(List.of(), BigDecimal.ZERO);
		when(dailySeriesBuilder.build(
						eq(accountBookId),
						eq(CurrencyType.LOCAL),
						any(),
						any(),
						any(),
						any(),
						any(),
						eq(CurrencyCode.USD)))
				.thenReturn(thisSeries, prevSeries);

		Map<Category, BigDecimal> myCategoryMap = new EnumMap<>(Category.class);
		myCategoryMap.put(Category.FOOD, new BigDecimal("300"));
		when(categorySnapshotResolver.resolve(
						eq(accountBookId),
						eq(CountryCode.US),
						any(),
						eq(CurrencyType.LOCAL),
						eq(CurrencyCode.USD)))
				.thenReturn(new AnalysisMyCategorySnapshot(myCategoryMap, false));

		when(peerComparisonResolver.resolvePairPeerContext(
						any(), any(), any(), any(), any(), eq(false)))
				.thenReturn(AnalysisPairPeerContext.unavailable());
		when(peerComparisonResolver.resolvePairCategoryAverageMap(
						any(), any(), any(), any(), any(), anyMap()))
				.thenReturn(Map.of());

		AnalysisOverviewRes res =
				service.getAnalysisOverview(
						userId, accountBookId, "2025", "12", CurrencyType.LOCAL);

		assertThat(res.countryCode()).isEqualTo("US");
		assertThat(res.compareWithLastMonth().totalSpent().thisMonthToDate()).isEqualTo("300");
		AnalysisOverviewRes.CompareByCategory.CategoryItem foodItem =
				res.compareByCategory().items().stream()
						.filter(i -> i.categoryIndex() == Category.FOOD.ordinal())
						.findFirst()
						.orElseThrow();
		assertThat(foodItem.mySpentAmount()).isEqualTo("300");
		verify(categorySnapshotResolver)
				.resolve(
						eq(accountBookId),
						eq(CountryCode.US),
						any(),
						eq(CurrencyType.LOCAL),
						eq(CurrencyCode.USD));
	}

	@Test
	void getAnalysisOverview_mapsDailySeriesItemsWithoutChangingCumulativeValues() {
		Long accountBookId = 13L;
		when(analysisQueryRepository.getAccountBookCountryCodes(accountBookId))
				.thenReturn(new Object[] {CountryCode.US, CountryCode.KR});

		AnalysisDailySeries thisSeries =
				new AnalysisDailySeries(
						List.of(
								new AnalysisDailyRow("2025-12-05", new BigDecimal("50.00")),
								new AnalysisDailyRow("2025-12-06", new BigDecimal("50.00")),
								new AnalysisDailyRow("2025-12-07", new BigDecimal("50.00"))),
						new BigDecimal("50.00"));
		AnalysisDailySeries prevSeries = new AnalysisDailySeries(List.of(), BigDecimal.ZERO);
		when(dailySeriesBuilder.build(
						eq(accountBookId),
						eq(CurrencyType.BASE),
						any(),
						any(),
						any(),
						any(),
						any(),
						eq(CurrencyCode.USD)))
				.thenReturn(thisSeries, prevSeries);
		when(categorySnapshotResolver.resolve(any(), any(), any(), any(), any()))
				.thenReturn(new AnalysisMyCategorySnapshot(Map.of(), false));
		when(peerComparisonResolver.resolvePairPeerContext(
						any(), any(), any(), any(), any(), anyBoolean()))
				.thenReturn(AnalysisPairPeerContext.unavailable());
		when(peerComparisonResolver.resolvePairCategoryAverageMap(
						any(), any(), any(), any(), any(), anyMap()))
				.thenReturn(Map.of());

		AnalysisOverviewRes res =
				service.getAnalysisOverview(
						UUID.randomUUID(), accountBookId, "2025", "12", CurrencyType.BASE);

		List<AnalysisOverviewRes.DailySpentItem> items = res.compareWithLastMonth().thisMonthItem();
		assertThat(items.get(0).date()).isEqualTo("2025-12-05");
		assertThat(items.get(0).cumulatedAmount()).isEqualTo("50");
		assertThat(items.get(1).date()).isEqualTo("2025-12-06");
		assertThat(items.get(1).cumulatedAmount()).isEqualTo("50");
		assertThat(items.get(2).cumulatedAmount()).isEqualTo("50");
	}
}
