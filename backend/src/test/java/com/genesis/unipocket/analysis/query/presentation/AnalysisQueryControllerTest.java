package com.genesis.unipocket.analysis.query.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.genesis.unipocket.analysis.command.persistence.entity.AnalysisBatchJobStatus;
import com.genesis.unipocket.analysis.command.persistence.entity.AnalysisMetricType;
import com.genesis.unipocket.analysis.command.persistence.entity.AnalysisQualityType;
import com.genesis.unipocket.analysis.command.persistence.repository.AnalysisMonthlyDirtyRepository;
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
import java.util.List;
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

		List<Object[]> thisMonthRows =
				List.of(
						new Object[] {LocalDate.of(2025, 12, 1), new BigDecimal("100")},
						new Object[] {LocalDate.of(2025, 12, 3), new BigDecimal("50")});
		List<Object[]> prevMonthRows =
				List.of(
						new Object[] {LocalDate.of(2025, 11, 1), new BigDecimal("20")},
						new Object[] {LocalDate.of(2025, 11, 2), new BigDecimal("30")});
		org.mockito.BDDMockito.given(
						analysisQueryRepository.getMyDailySpent(
								eq(accountBookId),
								any(LocalDateTime.class),
								any(LocalDateTime.class),
								eq(CurrencyType.BASE)))
				.willReturn(thisMonthRows, prevMonthRows);

		org.mockito.BDDMockito.given(
						aggregationRepository.aggregatePeerMonthlyTotalFromMonthly(
								eq(CountryCode.US),
								eq(CountryCode.KR),
								eq(accountBookId),
								eq(LocalDate.of(2025, 12, 1)),
								eq(AnalysisMetricType.TOTAL_BASE_AMOUNT),
								eq(AnalysisQualityType.CLEANED)))
				.willReturn(new AmountCount(new BigDecimal("900"), 3L));

		mockMvc.perform(
						get(
										"/account-books/{accountBookId}/analysis/monthly-summary",
										accountBookId)
								.param("year", "2025")
								.param("month", "12월")
								.param("currencyView", "BASE")
								.cookie(new Cookie(AuthCookieConstants.ACCESS_TOKEN, accessToken)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.thisMonth.total").value("150"))
				.andExpect(jsonPath("$.thisMonth.daily.length()").value(31))
				.andExpect(jsonPath("$.thisMonth.daily[0].dailySpend").value("100"))
				.andExpect(jsonPath("$.thisMonth.daily[1].dailySpend").value("0"))
				.andExpect(jsonPath("$.thisMonth.daily[2].cumulativeSpend").value("150"))
				.andExpect(jsonPath("$.prevMonth.total").value("50"))
				.andExpect(jsonPath("$.comparison.peerAvailable").value(true))
				.andExpect(jsonPath("$.comparison.avgTotal").value("300"))
				.andExpect(jsonPath("$.comparison.diff").value("-150"));
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
				.willReturn(new AmountCount(new BigDecimal("500"), 1L));
		org.mockito.BDDMockito.given(
						aggregationRepository.aggregateAccountMonthlyCategoryFromMonthly(
								eq(accountBookId),
								eq(LocalDate.of(2025, 12, 1)),
								eq(AnalysisQualityType.CLEANED),
								eq(CurrencyType.BASE)))
				.willReturn(
						List.of(
								new CategoryAmountCount(2, new BigDecimal("200"), 3L),
								new CategoryAmountCount(4, new BigDecimal("300"), 2L)));
		org.mockito.BDDMockito.given(
						aggregationRepository.aggregatePeerMonthlyTotalFromMonthly(
								eq(CountryCode.US),
								eq(CountryCode.KR),
								eq(accountBookId),
								eq(LocalDate.of(2025, 12, 1)),
								eq(AnalysisMetricType.TOTAL_BASE_AMOUNT),
								eq(AnalysisQualityType.CLEANED)))
				.willReturn(new AmountCount(new BigDecimal("600"), 2L));
		org.mockito.BDDMockito.given(
						aggregationRepository.aggregatePeerMonthlyCategoryFromMonthly(
								eq(CountryCode.US),
								eq(CountryCode.KR),
								eq(accountBookId),
								eq(LocalDate.of(2025, 12, 1)),
								eq(AnalysisQualityType.CLEANED),
								eq(CurrencyType.BASE)))
				.willReturn(
						List.of(
								new CategoryAmountCount(2, new BigDecimal("500"), 10L),
								new CategoryAmountCount(4, new BigDecimal("100"), 4L)));

		mockMvc.perform(
						get(
										"/account-books/{accountBookId}/analysis/category-breakdown",
										accountBookId)
								.param("year", "2025")
								.param("month", "12월")
								.param("currencyView", "BASE")
								.cookie(new Cookie(AuthCookieConstants.ACCESS_TOKEN, accessToken)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.myTotal").value("500"))
				.andExpect(jsonPath("$.avgTotal").value("300"))
				.andExpect(jsonPath("$.peerAvailable").value(true))
				.andExpect(jsonPath("$.categories.length()").value(9))
				.andExpect(jsonPath("$.categories[2].category").value(2))
				.andExpect(jsonPath("$.categories[2].mySpend").value("200"))
				.andExpect(jsonPath("$.categories[2].avgSpend").value("250"))
				.andExpect(jsonPath("$.categories[2].diff").value("-50"))
				.andExpect(jsonPath("$.categories[3].mySpend").value("0"))
				.andExpect(jsonPath("$.categories[3].avgSpend").value("0"))
				.andExpect(jsonPath("$.categories[3].diff").value("0"))
				.andExpect(jsonPath("$.categories[4].category").value(4))
				.andExpect(jsonPath("$.categories[4].mySpend").value("300"))
				.andExpect(jsonPath("$.categories[4].avgSpend").value("50"))
				.andExpect(jsonPath("$.categories[4].diff").value("250"));
	}

	private void mockAuthentication(String accessToken, UUID userId) {
		org.mockito.BDDMockito.given(jwtProvider.validateToken(accessToken)).willReturn(true);
		org.mockito.BDDMockito.given(jwtProvider.getJti(accessToken)).willReturn("jti");
		org.mockito.BDDMockito.given(tokenBlacklistService.isBlacklisted("jti")).willReturn(false);
		org.mockito.BDDMockito.given(jwtProvider.getUserId(accessToken)).willReturn(userId);
	}
}
