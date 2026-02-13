package com.genesis.unipocket.analysis.query.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

import com.genesis.unipocket.analysis.common.enums.CurrencyType;
import com.genesis.unipocket.analysis.query.persistence.repository.AnalysisQueryRepository;
import com.genesis.unipocket.analysis.query.persistence.response.AccountBookAnalysisRes;
import com.genesis.unipocket.analysis.query.persistence.response.CompareByCategoryRes;
import com.genesis.unipocket.analysis.query.persistence.response.CompareByCategoryRes.CategoryItem;
import com.genesis.unipocket.analysis.query.persistence.response.CompareWithAverageRes;
import com.genesis.unipocket.analysis.query.persistence.response.CompareWithLastMonthRes;
import com.genesis.unipocket.global.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CountryCode;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("AnalysisQueryService 단위 테스트")
class AnalysisQueryServiceTest {

	@Mock private AnalysisQueryRepository repository;

	@InjectMocks private AnalysisQueryService service;

	private static final UUID USER_ID = UUID.randomUUID();
	private static final Long ACCOUNT_BOOK_ID = 1L;
	private static final int YEAR = 2025;
	private static final int MONTH = 1;

	private void stubCommonRepository(CurrencyType currencyType) {
		when(repository.getAccountBookCountryCodes(ACCOUNT_BOOK_ID))
				.thenReturn(new Object[] {CountryCode.KR, CountryCode.US});

		CountryCode comparisonCode =
				currencyType == CurrencyType.BASE ? CountryCode.US : CountryCode.KR;

		when(repository.getOtherUsersTotalAndCount(
						eq(comparisonCode), anyString(), any(), any(), eq(currencyType)))
				.thenReturn(new Object[] {BigDecimal.valueOf(300000), 3L});

		when(repository.getMyMonthlyTotal(eq(ACCOUNT_BOOK_ID), any(), any(), eq(currencyType)))
				.thenReturn(BigDecimal.valueOf(150000));

		when(repository.getMyDailySpent(eq(ACCOUNT_BOOK_ID), any(), any(), eq(currencyType)))
				.thenReturn(Collections.emptyList());

		when(repository.getMyCategorySpent(eq(ACCOUNT_BOOK_ID), any(), any(), eq(currencyType)))
				.thenReturn(Collections.emptyList());

		when(repository.getOtherUsersCategoryTotal(
						eq(comparisonCode), anyString(), any(), any(), eq(currencyType)))
				.thenReturn(Collections.emptyList());
	}

	@Nested
	@DisplayName("CurrencyType에 따른 비교 국가 코드 선택")
	class ComparisonCountryCodeTests {

		@Test
		@DisplayName("CurrencyType.LOCAL이면 localCountryCode로 비교한다")
		void local_usesLocalCountryCode() {
			// given
			stubCommonRepository(CurrencyType.LOCAL);

			// when
			AccountBookAnalysisRes result =
					service.getAnalysis(USER_ID, ACCOUNT_BOOK_ID, CurrencyType.LOCAL, YEAR, MONTH);

			// then
			assertThat(result.countryCode()).isEqualTo(CountryCode.KR);
		}

		@Test
		@DisplayName("CurrencyType.BASE이면 baseCountryCode로 비교한다")
		void base_usesBaseCountryCode() {
			// given
			stubCommonRepository(CurrencyType.BASE);

			// when
			AccountBookAnalysisRes result =
					service.getAnalysis(USER_ID, ACCOUNT_BOOK_ID, CurrencyType.BASE, YEAR, MONTH);

			// then
			assertThat(result.countryCode()).isEqualTo(CountryCode.US);
		}
	}

	@Nested
	@DisplayName("타임존 기반 월 경계 계산")
	class TimezoneTests {

		@Test
		@DisplayName("KR(Asia/Seoul) 기준 월 경계가 UTC로 변환되어 쿼리에 사용된다")
		void krTimezone_convertsToUtcBoundaries() {
			// given
			ZoneId seoulZone = ZoneId.of("Asia/Seoul");
			LocalDateTime expectedStart =
					ZonedDateTime.of(2025, 1, 1, 0, 0, 0, 0, seoulZone)
							.withZoneSameInstant(ZoneId.of("UTC"))
							.toLocalDateTime();
			LocalDateTime expectedEnd =
					ZonedDateTime.of(2025, 2, 1, 0, 0, 0, 0, seoulZone)
							.withZoneSameInstant(ZoneId.of("UTC"))
							.toLocalDateTime();

			when(repository.getAccountBookCountryCodes(ACCOUNT_BOOK_ID))
					.thenReturn(new Object[] {CountryCode.KR, CountryCode.US});
			when(repository.getOtherUsersTotalAndCount(
							eq(CountryCode.KR),
							anyString(),
							eq(expectedStart),
							eq(expectedEnd),
							eq(CurrencyType.LOCAL)))
					.thenReturn(new Object[] {BigDecimal.ZERO, 0L});
			when(repository.getMyMonthlyTotal(
							eq(ACCOUNT_BOOK_ID),
							eq(expectedStart),
							eq(expectedEnd),
							eq(CurrencyType.LOCAL)))
					.thenReturn(BigDecimal.ZERO);
			when(repository.getMyDailySpent(
							eq(ACCOUNT_BOOK_ID), any(), any(), eq(CurrencyType.LOCAL)))
					.thenReturn(Collections.emptyList());
			when(repository.getMyCategorySpent(
							eq(ACCOUNT_BOOK_ID),
							eq(expectedStart),
							eq(expectedEnd),
							eq(CurrencyType.LOCAL)))
					.thenReturn(Collections.emptyList());
			when(repository.getOtherUsersCategoryTotal(
							eq(CountryCode.KR),
							anyString(),
							eq(expectedStart),
							eq(expectedEnd),
							eq(CurrencyType.LOCAL)))
					.thenReturn(Collections.emptyList());

			// when & then (쿼리가 UTC 변환된 경계값으로 정확히 호출되면 예외 없이 통과)
			AccountBookAnalysisRes result =
					service.getAnalysis(USER_ID, ACCOUNT_BOOK_ID, CurrencyType.LOCAL, YEAR, MONTH);

			assertThat(result).isNotNull();
			// KR(UTC+9): 2025-01-01 00:00 KST = 2024-12-31 15:00 UTC
			assertThat(expectedStart).isEqualTo(LocalDateTime.of(2024, 12, 31, 15, 0));
			// KR(UTC+9): 2025-02-01 00:00 KST = 2025-01-31 15:00 UTC
			assertThat(expectedEnd).isEqualTo(LocalDateTime.of(2025, 1, 31, 15, 0));
		}
	}

	@Nested
	@DisplayName("평균 비교 (CompareWithAverage)")
	class CompareWithAverageTests {

		@Test
		@DisplayName("다른 사용자 평균과의 차이를 정확히 계산한다")
		void calculatesAverageAndDiff() {
			// given
			stubCommonRepository(CurrencyType.LOCAL);

			// when
			AccountBookAnalysisRes result =
					service.getAnalysis(USER_ID, ACCOUNT_BOOK_ID, CurrencyType.LOCAL, YEAR, MONTH);

			// then
			CompareWithAverageRes avg = result.compareWithAverage();
			assertThat(avg.month()).isEqualTo(MONTH);
			assertThat(avg.mySpentAmount()).isEqualTo("150000");
			// 300000 / 3 = 100000
			assertThat(avg.averageSpentAmount()).isEqualTo("100000");
			// 150000 - 100000 = 50000
			assertThat(avg.spentAmountDiff()).isEqualTo("50000");
		}

		@Test
		@DisplayName("다른 사용자가 없으면 평균은 0이다")
		void noOtherUsers_averageIsZero() {
			// given
			when(repository.getAccountBookCountryCodes(ACCOUNT_BOOK_ID))
					.thenReturn(new Object[] {CountryCode.KR, CountryCode.US});
			when(repository.getOtherUsersTotalAndCount(
							eq(CountryCode.KR), anyString(), any(), any(), eq(CurrencyType.LOCAL)))
					.thenReturn(new Object[] {BigDecimal.ZERO, 0L});
			when(repository.getMyMonthlyTotal(
							eq(ACCOUNT_BOOK_ID), any(), any(), eq(CurrencyType.LOCAL)))
					.thenReturn(BigDecimal.valueOf(50000));
			when(repository.getMyDailySpent(
							eq(ACCOUNT_BOOK_ID), any(), any(), eq(CurrencyType.LOCAL)))
					.thenReturn(Collections.emptyList());
			when(repository.getMyCategorySpent(
							eq(ACCOUNT_BOOK_ID), any(), any(), eq(CurrencyType.LOCAL)))
					.thenReturn(Collections.emptyList());
			when(repository.getOtherUsersCategoryTotal(
							eq(CountryCode.KR), anyString(), any(), any(), eq(CurrencyType.LOCAL)))
					.thenReturn(Collections.emptyList());

			// when
			AccountBookAnalysisRes result =
					service.getAnalysis(USER_ID, ACCOUNT_BOOK_ID, CurrencyType.LOCAL, YEAR, MONTH);

			// then
			CompareWithAverageRes avg = result.compareWithAverage();
			assertThat(avg.averageSpentAmount()).isEqualTo("0");
			assertThat(avg.spentAmountDiff()).isEqualTo("50000");
		}
	}

	@Nested
	@DisplayName("전월 비교 (CompareWithLastMonth)")
	class CompareWithLastMonthTests {

		@Test
		@DisplayName("일별 누적 금액을 정확히 계산한다")
		void computesCumulativeDaily() {
			// given
			when(repository.getAccountBookCountryCodes(ACCOUNT_BOOK_ID))
					.thenReturn(new Object[] {CountryCode.KR, CountryCode.US});
			when(repository.getOtherUsersTotalAndCount(
							eq(CountryCode.KR), anyString(), any(), any(), eq(CurrencyType.LOCAL)))
					.thenReturn(new Object[] {BigDecimal.ZERO, 0L});
			when(repository.getMyMonthlyTotal(
							eq(ACCOUNT_BOOK_ID), any(), any(), eq(CurrencyType.LOCAL)))
					.thenReturn(BigDecimal.valueOf(30000));

			// 이번 달 일별 지출: 1일 10000, 3일 20000
			List<Object[]> thisMonthDaily =
					List.of(
							new Object[] {LocalDate.of(2025, 1, 1), BigDecimal.valueOf(10000)},
							new Object[] {LocalDate.of(2025, 1, 3), BigDecimal.valueOf(20000)});
			// 지난 달 일별 지출: 없음
			List<Object[]> prevMonthDaily = Collections.emptyList();

			when(repository.getMyDailySpent(
							eq(ACCOUNT_BOOK_ID), any(), any(), eq(CurrencyType.LOCAL)))
					.thenReturn(thisMonthDaily)
					.thenReturn(prevMonthDaily);

			when(repository.getMyCategorySpent(
							eq(ACCOUNT_BOOK_ID), any(), any(), eq(CurrencyType.LOCAL)))
					.thenReturn(Collections.emptyList());
			when(repository.getOtherUsersCategoryTotal(
							eq(CountryCode.KR), anyString(), any(), any(), eq(CurrencyType.LOCAL)))
					.thenReturn(Collections.emptyList());

			// when
			AccountBookAnalysisRes result =
					service.getAnalysis(USER_ID, ACCOUNT_BOOK_ID, CurrencyType.LOCAL, YEAR, MONTH);

			// then
			CompareWithLastMonthRes lastMonth = result.compareWithLastMonth();
			assertThat(lastMonth.thisMonth()).isEqualTo("1");
			assertThat(lastMonth.lastMonth()).isEqualTo("12");

			// 1월은 31일이므로 31개 항목
			assertThat(lastMonth.thisMonthCount()).isEqualTo(31);

			List<CompareWithLastMonthRes.DailyItem> items = lastMonth.thisMonthItem();
			// 1일: 10000 누적
			assertThat(items.get(0).cumulatedAmount()).isEqualTo("10000");
			// 2일: 10000 누적 (지출 없음)
			assertThat(items.get(1).cumulatedAmount()).isEqualTo("10000");
			// 3일: 30000 누적
			assertThat(items.get(2).cumulatedAmount()).isEqualTo("30000");
		}

		@Test
		@DisplayName("이번 달과 지난 달 월 표시 문자열이 정확하다")
		void monthLabelsAreCorrect() {
			// given
			stubCommonRepository(CurrencyType.LOCAL);

			// when
			AccountBookAnalysisRes result =
					service.getAnalysis(USER_ID, ACCOUNT_BOOK_ID, CurrencyType.LOCAL, YEAR, MONTH);

			// then
			CompareWithLastMonthRes lastMonth = result.compareWithLastMonth();
			assertThat(lastMonth.thisMonth()).isEqualTo("1");
			assertThat(lastMonth.lastMonth()).isEqualTo("12");
		}
	}

	@Nested
	@DisplayName("카테고리별 비교 (CompareByCategory)")
	class CompareByCategoryTests {

		@Test
		@DisplayName("INCOME 카테고리는 결과에서 제외된다")
		void incomeIsExcluded() {
			// given
			stubCommonRepository(CurrencyType.LOCAL);

			// when
			AccountBookAnalysisRes result =
					service.getAnalysis(USER_ID, ACCOUNT_BOOK_ID, CurrencyType.LOCAL, YEAR, MONTH);

			// then
			CompareByCategoryRes category = result.compareByCategory();
			List<CategoryItem> items = category.items();

			// Category.values()에서 INCOME을 제외하면 9개
			assertThat(items).hasSize(Category.values().length - 1);

			boolean hasIncome =
					items.stream().anyMatch(i -> i.categoryIndex() == Category.INCOME.ordinal());
			assertThat(hasIncome).isFalse();
		}

		@Test
		@DisplayName("UNCLASSIFIED 카테고리는 마지막에 정렬된다")
		void unclassifiedSortedLast() {
			// given
			// 미분류에 가장 큰 금액을 넣어도 마지막으로 가는지 확인
			List<Object[]> myCategory =
					List.of(
							new Object[] {Category.UNCLASSIFIED, BigDecimal.valueOf(99999)},
							new Object[] {Category.FOOD, BigDecimal.valueOf(50000)});

			when(repository.getAccountBookCountryCodes(ACCOUNT_BOOK_ID))
					.thenReturn(new Object[] {CountryCode.KR, CountryCode.US});
			when(repository.getOtherUsersTotalAndCount(
							eq(CountryCode.KR), anyString(), any(), any(), eq(CurrencyType.LOCAL)))
					.thenReturn(new Object[] {BigDecimal.ZERO, 0L});
			when(repository.getMyMonthlyTotal(
							eq(ACCOUNT_BOOK_ID), any(), any(), eq(CurrencyType.LOCAL)))
					.thenReturn(BigDecimal.ZERO);
			when(repository.getMyDailySpent(
							eq(ACCOUNT_BOOK_ID), any(), any(), eq(CurrencyType.LOCAL)))
					.thenReturn(Collections.emptyList());
			when(repository.getMyCategorySpent(
							eq(ACCOUNT_BOOK_ID), any(), any(), eq(CurrencyType.LOCAL)))
					.thenReturn(myCategory);
			when(repository.getOtherUsersCategoryTotal(
							eq(CountryCode.KR), anyString(), any(), any(), eq(CurrencyType.LOCAL)))
					.thenReturn(Collections.emptyList());

			// when
			AccountBookAnalysisRes result =
					service.getAnalysis(USER_ID, ACCOUNT_BOOK_ID, CurrencyType.LOCAL, YEAR, MONTH);

			// then
			List<CategoryItem> items = result.compareByCategory().items();
			CategoryItem lastItem = items.get(items.size() - 1);
			assertThat(lastItem.categoryIndex()).isEqualTo(Category.UNCLASSIFIED.ordinal());
		}

		@Test
		@DisplayName("최대 차이 카테고리와 초과 여부를 정확히 판별한다")
		void detectsMaxDiffCategoryAndOverSpent() {
			// given
			// 내 식비: 80000, 평균 식비: 30000 → diff=50000 (초과)
			// 내 교통비: 10000, 평균 교통비: 50000 → diff=-40000 (절약)
			List<Object[]> myCategory =
					List.of(
							new Object[] {Category.FOOD, BigDecimal.valueOf(80000)},
							new Object[] {Category.TRANSPORT, BigDecimal.valueOf(10000)});
			List<Object[]> otherCategory =
					List.of(
							new Object[] {Category.FOOD.ordinal(), BigDecimal.valueOf(60000)},
							new Object[] {
								Category.TRANSPORT.ordinal(), BigDecimal.valueOf(100000)
							});

			when(repository.getAccountBookCountryCodes(ACCOUNT_BOOK_ID))
					.thenReturn(new Object[] {CountryCode.KR, CountryCode.US});
			when(repository.getOtherUsersTotalAndCount(
							eq(CountryCode.KR), anyString(), any(), any(), eq(CurrencyType.LOCAL)))
					.thenReturn(new Object[] {BigDecimal.valueOf(200000), 2L});
			when(repository.getMyMonthlyTotal(
							eq(ACCOUNT_BOOK_ID), any(), any(), eq(CurrencyType.LOCAL)))
					.thenReturn(BigDecimal.ZERO);
			when(repository.getMyDailySpent(
							eq(ACCOUNT_BOOK_ID), any(), any(), eq(CurrencyType.LOCAL)))
					.thenReturn(Collections.emptyList());
			when(repository.getMyCategorySpent(
							eq(ACCOUNT_BOOK_ID), any(), any(), eq(CurrencyType.LOCAL)))
					.thenReturn(myCategory);
			when(repository.getOtherUsersCategoryTotal(
							eq(CountryCode.KR), anyString(), any(), any(), eq(CurrencyType.LOCAL)))
					.thenReturn(otherCategory);

			// when
			AccountBookAnalysisRes result =
					service.getAnalysis(USER_ID, ACCOUNT_BOOK_ID, CurrencyType.LOCAL, YEAR, MONTH);

			// then
			CompareByCategoryRes category = result.compareByCategory();
			// 식비 diff=50000 > 교통비 diff=40000 → 식비가 maxDiff
			assertThat(category.maxDiffCategoryIndex()).isEqualTo(Category.FOOD.ordinal());
			// 내 식비 > 평균 식비 → 초과
			assertThat(category.isOverSpent()).isTrue();
		}
	}

	@Nested
	@DisplayName("금액 포맷팅")
	class AmountFormattingTests {

		@Test
		@DisplayName("소수점 이하가 있으면 둘째자리까지 내림 후 후행 0 제거")
		void formatsDecimalAmounts() {
			// given
			when(repository.getAccountBookCountryCodes(ACCOUNT_BOOK_ID))
					.thenReturn(new Object[] {CountryCode.KR, CountryCode.US});
			when(repository.getOtherUsersTotalAndCount(
							eq(CountryCode.KR), anyString(), any(), any(), eq(CurrencyType.LOCAL)))
					.thenReturn(new Object[] {BigDecimal.valueOf(100), 3L});
			// 100 / 3 = 33.3333... → 포맷: "33.33"
			when(repository.getMyMonthlyTotal(
							eq(ACCOUNT_BOOK_ID), any(), any(), eq(CurrencyType.LOCAL)))
					.thenReturn(BigDecimal.valueOf(50));
			when(repository.getMyDailySpent(
							eq(ACCOUNT_BOOK_ID), any(), any(), eq(CurrencyType.LOCAL)))
					.thenReturn(Collections.emptyList());
			when(repository.getMyCategorySpent(
							eq(ACCOUNT_BOOK_ID), any(), any(), eq(CurrencyType.LOCAL)))
					.thenReturn(Collections.emptyList());
			when(repository.getOtherUsersCategoryTotal(
							eq(CountryCode.KR), anyString(), any(), any(), eq(CurrencyType.LOCAL)))
					.thenReturn(Collections.emptyList());

			// when
			AccountBookAnalysisRes result =
					service.getAnalysis(USER_ID, ACCOUNT_BOOK_ID, CurrencyType.LOCAL, YEAR, MONTH);

			// then
			CompareWithAverageRes avg = result.compareWithAverage();
			assertThat(avg.averageSpentAmount()).isEqualTo("33.33");
		}
	}
}
