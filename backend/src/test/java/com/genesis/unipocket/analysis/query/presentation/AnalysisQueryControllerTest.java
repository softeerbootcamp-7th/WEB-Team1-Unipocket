package com.genesis.unipocket.analysis.query.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.genesis.unipocket.analysis.command.persistence.entity.AnalysisBatchJobStatus;
import com.genesis.unipocket.analysis.command.persistence.entity.AnalysisMetricType;
import com.genesis.unipocket.analysis.command.persistence.entity.AnalysisQualityType;
import com.genesis.unipocket.analysis.command.persistence.entity.PairMonthlyAggregateEntity;
import com.genesis.unipocket.analysis.command.persistence.entity.PairMonthlyCategoryAggregateEntity;
import com.genesis.unipocket.analysis.command.persistence.repository.AnalysisMonthlyDirtyRepository;
import com.genesis.unipocket.analysis.command.persistence.repository.PairMonthlyAggregateRepository;
import com.genesis.unipocket.analysis.command.persistence.repository.PairMonthlyCategoryAggregateRepository;
import com.genesis.unipocket.analysis.command.persistence.repository.support.AnalysisBatchAggregationRepository;
import com.genesis.unipocket.analysis.command.persistence.repository.support.AnalysisBatchAggregationRepository.AmountCount;
import com.genesis.unipocket.analysis.command.persistence.repository.support.AnalysisBatchAggregationRepository.CategoryAmountCount;
import com.genesis.unipocket.analysis.common.enums.CurrencyType;
import com.genesis.unipocket.analysis.query.persistence.repository.AnalysisQueryRepository;
import com.genesis.unipocket.analysis.query.service.AnalysisMonthlySummaryQueryService;
import com.genesis.unipocket.auth.command.application.JwtProvider;
import com.genesis.unipocket.auth.command.application.TokenBlacklistService;
import com.genesis.unipocket.auth.common.constant.AuthCookieConstants;
import com.genesis.unipocket.expense.command.facade.port.AccountBookOwnershipValidator;
import com.genesis.unipocket.global.common.enums.CountryCode;
import jakarta.servlet.http.Cookie;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AnalysisQueryController.class)
@Import(AnalysisMonthlySummaryQueryService.class)
class AnalysisQueryControllerTest {

	@Autowired private MockMvc mockMvc;

	@MockitoBean private AnalysisQueryRepository analysisQueryRepository;
	@MockitoBean private AnalysisBatchAggregationRepository aggregationRepository;
	@MockitoBean private AnalysisMonthlyDirtyRepository monthlyDirtyRepository;
	@MockitoBean private PairMonthlyAggregateRepository pairMonthlyAggregateRepository;
	@MockitoBean private PairMonthlyCategoryAggregateRepository pairMonthlyCategoryAggregateRepository;
	@MockitoBean private AccountBookOwnershipValidator accountBookOwnershipValidator;
	@MockitoBean private JwtProvider jwtProvider;
	@MockitoBean private TokenBlacklistService tokenBlacklistService;

	@SuppressWarnings("unused")
	@MockitoBean
	private JpaMetamodelMappingContext jpaMetamodelMappingContext;

	@Test
	@DisplayName("월 지출 요약 API는 일별 누적(CDF)과 평균 비교값을 반환한다")
	void getMonthlySpendSummary_returnsCumulativeAndAverage() throws Exception {
		UUID userId = UUID.randomUUID();
		String accessToken = "valid_token";
		Long accountBookId = 1L;

		mockAuthentication(accessToken, userId);

		org.mockito.BDDMockito.given(
						analysisQueryRepository.getAccountBookCountryCodes(accountBookId))
				.willReturn(new Object[] {CountryCode.US, CountryCode.KR});
		org.mockito.BDDMockito.given(
						monthlyDirtyRepository
								.existsByCountryCodeAndAccountBookIdAndTargetYearMonthAndStatusNot(
										eq(CountryCode.US),
										eq(accountBookId),
										eq(LocalDate.of(2025, 12, 1)),
										eq(AnalysisBatchJobStatus.SUCCESS)))
				.willReturn(false);
		org.mockito.BDDMockito.given(
						aggregationRepository.hasAccountMonthlyAggregate(
								eq(accountBookId),
								eq(LocalDate.of(2025, 12, 1)),
								eq(AnalysisMetricType.TOTAL_BASE_AMOUNT),
								eq(AnalysisQualityType.CLEANED)))
				.willReturn(true);

		List<Object[]> thisMonthRows =
				List.of(
						new Object[] {
							OffsetDateTime.parse("2025-12-01T12:00:00-05:00"),
							new BigDecimal("100.129")
						},
						new Object[] {
							OffsetDateTime.parse("2025-12-03T12:00:00-05:00"),
							new BigDecimal("50.126")
						});
		List<Object[]> prevMonthRows =
				List.of(
						new Object[] {
							OffsetDateTime.parse("2025-11-01T12:00:00-05:00"),
							new BigDecimal("20.111")
						},
						new Object[] {
							OffsetDateTime.parse("2025-11-02T12:00:00-05:00"),
							new BigDecimal("30.114")
						});
		org.mockito.BDDMockito.given(
						analysisQueryRepository.getMySpendEvents(
								eq(accountBookId),
								any(LocalDateTime.class),
								any(LocalDateTime.class),
								eq(CurrencyType.BASE)))
				.willReturn(thisMonthRows, prevMonthRows);

		org.mockito.BDDMockito.given(
						pairMonthlyAggregateRepository
								.findByLocalCountryCodeAndBaseCountryCodeAndTargetYearMonthAndQualityTypeAndMetricType(
								eq(CountryCode.US),
								eq(CountryCode.KR),
								eq(LocalDate.of(2025, 12, 1)),
								eq(AnalysisQualityType.CLEANED),
								eq(AnalysisMetricType.TOTAL_BASE_AMOUNT)))
				.willReturn(
						Optional.of(
								PairMonthlyAggregateEntity.of(
										CountryCode.US,
										CountryCode.KR,
										LocalDate.of(2025, 12, 1),
										AnalysisQualityType.CLEANED,
										AnalysisMetricType.TOTAL_BASE_AMOUNT,
										3L,
										new BigDecimal("750.4750"),
										new BigDecimal("250.1583"),
										null,
										null)));

		mockMvc.perform(
						get(
										"/account-books/{accountBookId}/analysis/monthly-summary",
										accountBookId)
								.param("year", "2025")
								.param("month", "12")
								.param("currencyType", "BASE")
								.cookie(new Cookie(AuthCookieConstants.ACCESS_TOKEN, accessToken)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.year").value("2025"))
				.andExpect(jsonPath("$.month").value("12"))
				.andExpect(jsonPath("$.currencyType").value("BASE"))
				.andExpect(jsonPath("$.thisMonth.total").value("150.26"))
				.andExpect(jsonPath("$.thisMonth.daily.length()").value(31))
				.andExpect(jsonPath("$.thisMonth.daily[0].dailySpend").value("100.13"))
				.andExpect(jsonPath("$.thisMonth.daily[1].dailySpend").value("0"))
				.andExpect(jsonPath("$.thisMonth.daily[2].cumulativeSpend").value("150.26"))
				.andExpect(jsonPath("$.prevMonth.total").value("50.23"))
				.andExpect(jsonPath("$.comparison.peerAvailable").value(true))
				.andExpect(jsonPath("$.comparison.avgTotal").value("300.11"))
				.andExpect(jsonPath("$.comparison.diff").value("-149.86"));
	}

	@Test
	@DisplayName("카테고리 분해 API는 배치값 기준 내 지출/평균/차이를 반환한다")
	void getCategoryBreakdown_returnsCategoryAndAverage() throws Exception {
		UUID userId = UUID.randomUUID();
		String accessToken = "valid_token";
		Long accountBookId = 1L;

		mockAuthentication(accessToken, userId);

		org.mockito.BDDMockito.given(
						analysisQueryRepository.getAccountBookCountryCodes(accountBookId))
				.willReturn(new Object[] {CountryCode.US, CountryCode.KR});
		org.mockito.BDDMockito.given(
						monthlyDirtyRepository
								.existsByCountryCodeAndAccountBookIdAndTargetYearMonthAndStatusNot(
										eq(CountryCode.US),
										eq(accountBookId),
										eq(LocalDate.of(2025, 12, 1)),
										eq(AnalysisBatchJobStatus.SUCCESS)))
				.willReturn(false);
		org.mockito.BDDMockito.given(
						aggregationRepository.hasAccountMonthlyAggregate(
								eq(accountBookId),
								eq(LocalDate.of(2025, 12, 1)),
								eq(AnalysisMetricType.TOTAL_BASE_AMOUNT),
								eq(AnalysisQualityType.CLEANED)))
				.willReturn(true);
		org.mockito.BDDMockito.given(
						aggregationRepository.aggregateAccountMonthlyFromMonthly(
								eq(accountBookId),
								eq(LocalDate.of(2025, 12, 1)),
								eq(AnalysisMetricType.TOTAL_BASE_AMOUNT),
								eq(AnalysisQualityType.CLEANED)))
				.willReturn(new AmountCount(new BigDecimal("500.555"), 1L));
		org.mockito.BDDMockito.given(
						aggregationRepository.aggregateAccountMonthlyCategoryFromMonthly(
								eq(accountBookId),
								eq(LocalDate.of(2025, 12, 1)),
								eq(AnalysisQualityType.CLEANED),
								eq(CurrencyType.BASE)))
				.willReturn(
						List.of(
								new CategoryAmountCount(2, new BigDecimal("200.225"), 3L),
								new CategoryAmountCount(4, new BigDecimal("300.335"), 2L)));
		org.mockito.BDDMockito.given(
						pairMonthlyAggregateRepository
								.findByLocalCountryCodeAndBaseCountryCodeAndTargetYearMonthAndQualityTypeAndMetricType(
								eq(CountryCode.US),
								eq(CountryCode.KR),
								eq(LocalDate.of(2025, 12, 1)),
								eq(AnalysisQualityType.CLEANED),
								eq(AnalysisMetricType.TOTAL_BASE_AMOUNT)))
				.willReturn(
						Optional.of(
								PairMonthlyAggregateEntity.of(
										CountryCode.US,
										CountryCode.KR,
										LocalDate.of(2025, 12, 1),
										AnalysisQualityType.CLEANED,
										AnalysisMetricType.TOTAL_BASE_AMOUNT,
										3L,
										new BigDecimal("1100.7750"),
										new BigDecimal("366.9250"),
										null,
										null)));
		org.mockito.BDDMockito.given(
						pairMonthlyCategoryAggregateRepository
								.findAllByLocalCountryCodeAndBaseCountryCodeAndTargetYearMonthAndQualityTypeAndCurrencyType(
								eq(CountryCode.US),
								eq(CountryCode.KR),
								eq(LocalDate.of(2025, 12, 1)),
								eq(AnalysisQualityType.CLEANED),
								eq(CurrencyType.BASE)))
				.willReturn(
						List.of(
								PairMonthlyCategoryAggregateEntity.of(
										CountryCode.US,
										CountryCode.KR,
										LocalDate.of(2025, 12, 1),
										AnalysisQualityType.CLEANED,
										CurrencyType.BASE,
										com.genesis.unipocket.global.common.enums.Category.FOOD,
										3L,
										new BigDecimal("700.4750"),
										new BigDecimal("233.4917")),
								PairMonthlyCategoryAggregateEntity.of(
										CountryCode.US,
										CountryCode.KR,
										LocalDate.of(2025, 12, 1),
										AnalysisQualityType.CLEANED,
										CurrencyType.BASE,
										com.genesis.unipocket.global.common.enums.Category.LIVING,
										3L,
										new BigDecimal("400.4550"),
										new BigDecimal("133.4850"))));

		mockMvc.perform(
						get(
										"/account-books/{accountBookId}/analysis/category-breakdown",
										accountBookId)
								.param("year", "2025")
								.param("month", "12")
								.param("currencyType", "BASE")
								.cookie(new Cookie(AuthCookieConstants.ACCESS_TOKEN, accessToken)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.year").value("2025"))
				.andExpect(jsonPath("$.month").value("12"))
				.andExpect(jsonPath("$.currencyType").value("BASE"))
				.andExpect(jsonPath("$.myTotal").value("500.56"))
				.andExpect(jsonPath("$.avgTotal").value("300.11"))
				.andExpect(jsonPath("$.peerAvailable").value(true))
				.andExpect(jsonPath("$.categories.length()").value(9))
				.andExpect(jsonPath("$.categories[2].category").value(2))
				.andExpect(jsonPath("$.categories[2].mySpend").value("200.23"))
				.andExpect(jsonPath("$.categories[2].avgSpend").value("250.13"))
				.andExpect(jsonPath("$.categories[2].diff").value("-49.90"))
				.andExpect(jsonPath("$.categories[3].mySpend").value("0"))
				.andExpect(jsonPath("$.categories[3].avgSpend").value("0"))
				.andExpect(jsonPath("$.categories[3].diff").value("0"))
				.andExpect(jsonPath("$.categories[4].category").value(4))
				.andExpect(jsonPath("$.categories[4].mySpend").value("300.34"))
				.andExpect(jsonPath("$.categories[4].avgSpend").value("50.06"))
				.andExpect(jsonPath("$.categories[4].diff").value("250.28"));
	}

	private void mockAuthentication(String accessToken, UUID userId) {
		org.mockito.BDDMockito.given(jwtProvider.validateToken(accessToken)).willReturn(true);
		org.mockito.BDDMockito.given(jwtProvider.getJti(accessToken)).willReturn("jti");
		org.mockito.BDDMockito.given(tokenBlacklistService.isBlacklisted("jti")).willReturn(false);
		org.mockito.BDDMockito.given(jwtProvider.getUserId(accessToken)).willReturn(userId);
	}
}
