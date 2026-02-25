package com.genesis.unipocket.analysis.query.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AnalysisMonthlyCategorySnapshotResolverTest {

	@Mock private AnalysisQueryRepository analysisQueryRepository;
	@Mock private AnalysisBatchAggregationRepository aggregationRepository;
	@Mock private AnalysisMonthlyDirtyRepository monthlyDirtyRepository;
	@Mock private ExchangeRateService exchangeRateService;

	@InjectMocks private AnalysisMonthlyCategorySnapshotResolver resolver;

	private static final Long ACCOUNT_BOOK_ID = 1L;
	private static final CountryCode LOCAL_COUNTRY = CountryCode.KR;
	private static final CurrencyCode LOCAL_CURRENCY = CurrencyCode.KRW;

	/**
	 * record 필드 순서: yearMonth, startLocalDate, endLocalDateInclusive, startUtc, endUtcExclusive
	 */
	private AnalysisMonthRange buildRange(int year, int month) {
		YearMonth ym = YearMonth.of(year, month);
		ZoneId zone = ZoneId.of("Asia/Seoul");
		LocalDate startLocal = ym.atDay(1);
		LocalDate endLocal = ym.atEndOfMonth();
		LocalDateTime startUtc =
				startLocal.atStartOfDay(zone).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
		LocalDateTime endUtc =
				endLocal.plusDays(1)
						.atStartOfDay(zone)
						.withZoneSameInstant(ZoneOffset.UTC)
						.toLocalDateTime();
		return new AnalysisMonthRange(ym, startLocal, endLocal, startUtc, endUtc);
	}

	/** Object[] 한 개를 원소로 갖는 List&lt;Object[]&gt;를 생성한다 */
	private static List<Object[]> singleRow(Object... cols) {
		List<Object[]> list = new ArrayList<>();
		list.add(cols);
		return list;
	}

	@Test
	@DisplayName("배치가 준비된 경우 월별 집계 테이블에서 카테고리 맵을 로드한다")
	void resolve_batchReady_loadsFromMonthlyAggregate() {
		AnalysisMonthRange range = buildRange(2025, 12);

		when(monthlyDirtyRepository
						.existsByCountryCodeAndAccountBookIdAndTargetYearMonthAndStatusNot(
								LOCAL_COUNTRY,
								ACCOUNT_BOOK_ID,
								range.yearMonth().atDay(1),
								AnalysisBatchJobStatus.SUCCESS))
				.thenReturn(false);
		when(aggregationRepository.hasAccountMonthlyAggregate(
						ACCOUNT_BOOK_ID,
						range.yearMonth().atDay(1),
						AnalysisMetricType.TOTAL_BASE_AMOUNT,
						AnalysisQualityType.CLEANED))
				.thenReturn(true);
		when(aggregationRepository.aggregateAccountMonthlyCategoryFromMonthly(
						eq(ACCOUNT_BOOK_ID), any(), any(), eq(CurrencyType.BASE)))
				.thenReturn(
						List.of(
								new CategoryAmountCount(
										Category.FOOD.ordinal(), new BigDecimal("5000"), 2)));

		AnalysisMyCategorySnapshot snapshot =
				resolver.resolve(
						ACCOUNT_BOOK_ID, LOCAL_COUNTRY, range, CurrencyType.BASE, LOCAL_CURRENCY);

		assertThat(snapshot.monthlyBatchReady()).isTrue();
		assertThat(snapshot.categoryMap()).containsKey(Category.FOOD);
		assertThat(snapshot.categoryMap().get(Category.FOOD)).isEqualByComparingTo("5000");
		verify(analysisQueryRepository, never()).getMyCategorySpent(any(), any(), any(), any());
	}

	@Test
	@DisplayName("dirty가 PENDING이면 배치 미준비로 판단해 실시간 BASE 쿼리를 사용한다")
	void resolve_dirtyPending_usesRealtimeBaseQuery() {
		AnalysisMonthRange range = buildRange(2025, 12);

		when(monthlyDirtyRepository
						.existsByCountryCodeAndAccountBookIdAndTargetYearMonthAndStatusNot(
								LOCAL_COUNTRY,
								ACCOUNT_BOOK_ID,
								range.yearMonth().atDay(1),
								AnalysisBatchJobStatus.SUCCESS))
				.thenReturn(true);

		when(analysisQueryRepository.getMyCategorySpent(
						eq(ACCOUNT_BOOK_ID),
						any(OffsetDateTime.class),
						any(OffsetDateTime.class),
						eq(CurrencyType.BASE)))
				.thenReturn(singleRow(Category.LIVING, new BigDecimal("3000")));

		AnalysisMyCategorySnapshot snapshot =
				resolver.resolve(
						ACCOUNT_BOOK_ID, LOCAL_COUNTRY, range, CurrencyType.BASE, LOCAL_CURRENCY);

		assertThat(snapshot.monthlyBatchReady()).isFalse();
		assertThat(snapshot.categoryMap()).containsKey(Category.LIVING);
		verify(aggregationRepository, never())
				.aggregateAccountMonthlyCategoryFromMonthly(any(), any(), any(), any());
	}

	@Test
	@DisplayName("LOCAL 통화 타입이고 배치 미준비이면 통화별 그룹 쿼리를 사용한다 (동일 통화 → 변환 없음)")
	void resolve_localCurrencyAndBatchNotReady_usesGroupedCurrencyQuery() {
		AnalysisMonthRange range = buildRange(2025, 12);

		when(monthlyDirtyRepository
						.existsByCountryCodeAndAccountBookIdAndTargetYearMonthAndStatusNot(
								any(), any(), any(), any()))
				.thenReturn(true);

		when(analysisQueryRepository.getMyCategorySpentGroupedByCurrency(
						eq(ACCOUNT_BOOK_ID), any(OffsetDateTime.class), any(OffsetDateTime.class)))
				.thenReturn(singleRow(Category.FOOD, CurrencyCode.KRW, new BigDecimal("10000")));

		AnalysisMyCategorySnapshot snapshot =
				resolver.resolve(
						ACCOUNT_BOOK_ID, LOCAL_COUNTRY, range, CurrencyType.LOCAL, LOCAL_CURRENCY);

		assertThat(snapshot.monthlyBatchReady()).isFalse();
		assertThat(snapshot.categoryMap()).containsKey(Category.FOOD);
		assertThat(snapshot.categoryMap().get(Category.FOOD)).isEqualByComparingTo("10000");
		verify(exchangeRateService, never()).convertAmount(any(), any(), any(), any());
	}

	@Test
	@DisplayName("LOCAL 통화 타입이고 다른 통화가 섞이면 환율 변환이 수행된다")
	void resolve_localCurrencyMixed_convertsOtherCurrencies() {
		AnalysisMonthRange range = buildRange(2025, 12);

		when(monthlyDirtyRepository
						.existsByCountryCodeAndAccountBookIdAndTargetYearMonthAndStatusNot(
								any(), any(), any(), any()))
				.thenReturn(true);

		when(analysisQueryRepository.getMyCategorySpentGroupedByCurrency(
						eq(ACCOUNT_BOOK_ID), any(OffsetDateTime.class), any(OffsetDateTime.class)))
				.thenReturn(singleRow(Category.FOOD, CurrencyCode.USD, new BigDecimal("100")));

		when(exchangeRateService.convertAmount(
						eq(new BigDecimal("100")),
						eq(CurrencyCode.USD),
						eq(CurrencyCode.KRW),
						any(OffsetDateTime.class)))
				.thenReturn(new BigDecimal("135000"));

		AnalysisMyCategorySnapshot snapshot =
				resolver.resolve(
						ACCOUNT_BOOK_ID, LOCAL_COUNTRY, range, CurrencyType.LOCAL, LOCAL_CURRENCY);

		assertThat(snapshot.categoryMap().get(Category.FOOD)).isEqualByComparingTo("135000");
	}

	@Test
	@DisplayName("INCOME 및 UNCLASSIFIED 카테고리는 배치 결과에서 제외된다")
	void resolve_batchReady_excludesIncomeAndUnclassified() {
		AnalysisMonthRange range = buildRange(2025, 12);

		when(monthlyDirtyRepository
						.existsByCountryCodeAndAccountBookIdAndTargetYearMonthAndStatusNot(
								any(), any(), any(), any()))
				.thenReturn(false);
		when(aggregationRepository.hasAccountMonthlyAggregate(any(), any(), any(), any()))
				.thenReturn(true);
		when(aggregationRepository.aggregateAccountMonthlyCategoryFromMonthly(
						any(), any(), any(), any()))
				.thenReturn(
						List.of(
								new CategoryAmountCount(
										Category.INCOME.ordinal(), new BigDecimal("99999"), 1),
								new CategoryAmountCount(
										Category.UNCLASSIFIED.ordinal(), new BigDecimal("1000"), 1),
								new CategoryAmountCount(
										Category.FOOD.ordinal(), new BigDecimal("5000"), 2)));

		AnalysisMyCategorySnapshot snapshot =
				resolver.resolve(
						ACCOUNT_BOOK_ID, LOCAL_COUNTRY, range, CurrencyType.BASE, LOCAL_CURRENCY);

		Map<Category, BigDecimal> map = snapshot.categoryMap();
		assertThat(map).doesNotContainKey(Category.INCOME);
		assertThat(map).doesNotContainKey(Category.UNCLASSIFIED);
		assertThat(map).containsKey(Category.FOOD);
	}
}
