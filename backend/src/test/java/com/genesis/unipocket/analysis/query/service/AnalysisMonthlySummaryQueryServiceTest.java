package com.genesis.unipocket.analysis.query.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

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
import com.genesis.unipocket.analysis.query.persistence.repository.AnalysisQueryRepository;
import com.genesis.unipocket.analysis.query.persistence.response.AnalysisOverviewRes;
import com.genesis.unipocket.expense.command.facade.port.AccountBookOwnershipValidator;
import com.genesis.unipocket.global.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CountryCode;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AnalysisMonthlySummaryQueryServiceTest {

	@Mock private AnalysisQueryRepository analysisQueryRepository;
	@Mock private AnalysisBatchAggregationRepository aggregationRepository;
	@Mock private AnalysisMonthlyDirtyRepository monthlyDirtyRepository;
	@Mock private PairMonthlyAggregateRepository pairMonthlyAggregateRepository;
	@Mock private PairMonthlyCategoryAggregateRepository pairMonthlyCategoryAggregateRepository;
	@Mock private AccountBookOwnershipValidator accountBookOwnershipValidator;

	@InjectMocks private AnalysisMonthlySummaryQueryService service;

	@Test
	void getAnalysisOverview_invalidYear_throwsInvalidInputValue() {
		UUID userId = UUID.randomUUID();

		assertThatThrownBy(
						() ->
								service.getAnalysisOverview(
										userId, 1L, "20A6", "12", CurrencyType.BASE))
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
	void getAnalysisOverview_success_returnsCombinedData() {
		UUID userId = UUID.randomUUID();
		Long accountBookId = 12L;

		// 1. Ownership & Country setup
		when(analysisQueryRepository.getAccountBookCountryCodes(accountBookId))
				.thenReturn(new Object[] {CountryCode.US, CountryCode.KR});

		// 2. Batch Status (assume data is ready for simplicity, or specific mock
		// needed)
		// For simplicity, let's say batch is ready so it uses aggregation repository,
		// OR say it's not ready and uses raw query.
		// Let's go with "Monthly Batch Ready" path for My Data and Peer Data available.

		when(monthlyDirtyRepository
						.existsByCountryCodeAndAccountBookIdAndTargetYearMonthAndStatusNot(
								eq(CountryCode.US),
								eq(accountBookId),
								eq(LocalDate.of(2025, 12, 1)),
								eq(AnalysisBatchJobStatus.SUCCESS)))
				.thenReturn(false);

		when(aggregationRepository.hasAccountMonthlyAggregate(
						eq(accountBookId),
						eq(LocalDate.of(2025, 12, 1)),
						eq(AnalysisMetricType.TOTAL_BASE_AMOUNT),
						eq(AnalysisQualityType.CLEANED)))
				.thenReturn(true);

		// Mock My Snapshot Data (Category)
		when(aggregationRepository.aggregateAccountMonthlyCategoryFromMonthly(
						eq(accountBookId),
						eq(LocalDate.of(2025, 12, 1)),
						eq(AnalysisQualityType.CLEANED),
						eq(CurrencyType.BASE)))
				.thenReturn(
						List.of(
								new CategoryAmountCount(
										Category.FOOD.ordinal(), new BigDecimal("200.00"), 3),
								new CategoryAmountCount(
										Category.LIVING.ordinal(), new BigDecimal("300.00"), 2)));

		// 3. Daily Series (This Month & Prev Month)
		// This Month
		when(analysisQueryRepository.getMySpendEvents(
						eq(accountBookId),
						any(OffsetDateTime.class), // this month range
						any(OffsetDateTime.class),
						eq(CurrencyType.BASE)))
				.thenReturn(
						java.util.stream.Stream.of(
								new Object[] {
									OffsetDateTime.parse("2025-12-01T10:00:00-05:00"),
									new BigDecimal("100")
								},
								new Object[] {
									OffsetDateTime.parse("2025-12-02T10:00:00-05:00"),
									new BigDecimal("400")
								}),
						java.util.stream.Stream.of( // Prev Month (November)
								new Object[] {
									OffsetDateTime.parse("2025-11-01T10:00:00-05:00"),
									new BigDecimal("50")
								},
								new Object[] {
									OffsetDateTime.parse("2025-11-02T10:00:00-05:00"),
									new BigDecimal("50")
								}));

		// 4. Peer Aggregate Data
		when(pairMonthlyAggregateRepository
						.findByLocalCountryCodeAndBaseCountryCodeAndTargetYearMonthAndQualityTypeAndMetricType(
								eq(CountryCode.US),
								eq(CountryCode.KR),
								eq(LocalDate.of(2025, 12, 1)),
								eq(AnalysisQualityType.CLEANED),
								eq(AnalysisMetricType.TOTAL_BASE_AMOUNT)))
				.thenReturn(
						Optional.of(
								PairMonthlyAggregateEntity.of(
										CountryCode.US,
										CountryCode.KR,
										LocalDate.of(2025, 12, 1),
										AnalysisQualityType.CLEANED,
										AnalysisMetricType.TOTAL_BASE_AMOUNT,
										5L, // Included accounts
										new BigDecimal("2000.00"), // Total Metric Sum
										new BigDecimal("400.00"), // Average (2000/5)
										new BigDecimal("100"), // IQR Lower
										new BigDecimal("1000")))); // IQR Upper

		// 5. Peer Category Aggregate Data
		when(pairMonthlyCategoryAggregateRepository
						.findAllByLocalCountryCodeAndBaseCountryCodeAndTargetYearMonthAndQualityTypeAndCurrencyType(
								eq(CountryCode.US),
								eq(CountryCode.KR),
								eq(LocalDate.of(2025, 12, 1)),
								eq(AnalysisQualityType.CLEANED),
								eq(CurrencyType.BASE)))
				.thenReturn(
						List.of(
								PairMonthlyCategoryAggregateEntity.of(
										CountryCode.US,
										CountryCode.KR,
										LocalDate.of(2025, 12, 1),
										AnalysisQualityType.CLEANED,
										CurrencyType.BASE,
										Category.FOOD,
										5L,
										new BigDecimal("1000.00"),
										new BigDecimal("200.00")),
								PairMonthlyCategoryAggregateEntity.of(
										CountryCode.US,
										CountryCode.KR,
										LocalDate.of(2025, 12, 1),
										AnalysisQualityType.CLEANED,
										CurrencyType.BASE,
										Category.LIVING,
										5L,
										new BigDecimal("1000.00"),
										new BigDecimal("200.00"))));

		// Act
		AnalysisOverviewRes res =
				service.getAnalysisOverview(userId, accountBookId, "2025", "12", CurrencyType.BASE);

		// Assert

		// a. Basic Info
		assertThat(res.countryCode()).isEqualTo("US");

		// b. CompareWithAverage
		// My Total: 500 (from snapshot)
		// Peer Total: 2000 total sum. My 500 is included.
		// Effective peer count = 4. Peer Sum = 1500. Average = 375.
		// Diff = 500 - 375 = 125.
		assertThat(res.compareWithAverage().mySpentAmount()).isEqualTo("500");
		assertThat(res.compareWithAverage().averageSpentAmount()).isEqualTo("375");
		assertThat(res.compareWithAverage().spentAmountDiff()).isEqualTo("125");

		// c. CompareWithLastMonth
		// Total: 500. Prev Total: 100.
		// This Month Items: day1(100, cum 100), day2(400, cum 500)
		// Prev Month Items: day1(50, cum 50), day2(50, cum 100)
		// Diff logic uses same day index.
		// This month has 2 days. Prev month has 2 days.
		// Last day index is 1. Prev cum at index 1 is 100.
		// This total 500 - Prev 100 = 400.
		assertThat(res.compareWithLastMonth().diff()).isEqualTo("400");
		assertThat(res.compareWithLastMonth().totalSpent().thisMonthToDate()).isEqualTo("500");
		assertThat(res.compareWithLastMonth().totalSpent().lastMonthTotal()).isEqualTo("100");

		// d. CompareByCategory
		// FOOD: My 200. Peer Total 1000. My included. Peer Sum 800. Peer Count 4. Avg
		// 200. Diff 0.
		// LIVING: My 300. Peer Total 1000. My included. Peer Sum 700. Peer Count 4. Avg
		// 175. Diff 125.
		// Max diff: LIVING (125). Category index for Living is... let's check.
		// Category.LIVING
		assertThat(res.compareByCategory().isOverSpent()).isTrue(); // 500 > (200 + 175 = 375) ??
		// My Total 500. Avg Total Sum: 200(Food) + 175(Living) + others 0 = 375.
		// Yes, overspent.

		AnalysisOverviewRes.CompareByCategory.CategoryItem livingItem =
				res.compareByCategory().items().stream()
						.filter(i -> i.categoryIndex() == Category.LIVING.ordinal())
						.findFirst()
						.orElseThrow();
		assertThat(livingItem.mySpentAmount()).isEqualTo("300");
		assertThat(livingItem.averageSpentAmount()).isEqualTo("175");

		assertThat(res.compareByCategory().maxDiffCategoryIndex())
				.isEqualTo(Category.LIVING.ordinal());
	}

	@Test
	void getAnalysisOverview_gapFilling_cumulativeMaintained() {
		UUID userId = UUID.randomUUID();
		Long accountBookId = 13L;
		// 1. Setup
		when(analysisQueryRepository.getAccountBookCountryCodes(accountBookId))
				.thenReturn(new Object[] {CountryCode.US, CountryCode.KR});
		when(monthlyDirtyRepository
						.existsByCountryCodeAndAccountBookIdAndTargetYearMonthAndStatusNot(
								eq(CountryCode.US),
								eq(accountBookId),
								any(LocalDate.class),
								eq(AnalysisBatchJobStatus.SUCCESS)))
				.thenReturn(false);

		// 2. Mock Daily Spend Events (Day 5 has 50, Day 6 has 0/missing)
		when(analysisQueryRepository.getMySpendEvents(
						eq(accountBookId),
						any(OffsetDateTime.class),
						any(OffsetDateTime.class),
						eq(CurrencyType.BASE)))
				.thenReturn(
						java.util.stream.Stream.<Object[]>of(
								new Object[] {
									OffsetDateTime.parse("2025-12-05T10:00:00-05:00"),
									new BigDecimal("50.00")
								}),
						java.util.stream.Stream.empty() // Prev month empty
						);

		when(aggregationRepository.hasAccountMonthlyAggregate(any(), any(), any(), any()))
				.thenReturn(false);
		when(analysisQueryRepository.getMyCategorySpent(any(), any(), any(), any()))
				.thenReturn(List.of());
		when(pairMonthlyAggregateRepository
						.findByLocalCountryCodeAndBaseCountryCodeAndTargetYearMonthAndQualityTypeAndMetricType(
								any(), any(), any(), any(), any()))
				.thenReturn(Optional.empty());

		// Act
		AnalysisOverviewRes res =
				service.getAnalysisOverview(userId, accountBookId, "2025", "12", CurrencyType.BASE);

		// Assert: Check Day 5 and Day 6
		List<AnalysisOverviewRes.DailySpentItem> items = res.compareWithLastMonth().thisMonthItem();

		// Day 5 (index 4)
		AnalysisOverviewRes.DailySpentItem day5 = items.get(4);
		assertThat(day5.date()).isEqualTo("2025-12-05");
		assertThat(day5.cumulatedAmount()).isEqualTo("50");

		// Day 6 (index 5) - Should be 50 (50 + 0)
		AnalysisOverviewRes.DailySpentItem day6 = items.get(5);
		assertThat(day6.date()).isEqualTo("2025-12-06");
		assertThat(day6.cumulatedAmount()).isEqualTo("50");

		// Day 7
		AnalysisOverviewRes.DailySpentItem day7 = items.get(6);
		assertThat(day7.cumulatedAmount()).isEqualTo("50");
	}
}
