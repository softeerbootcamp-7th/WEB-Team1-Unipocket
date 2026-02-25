package com.genesis.unipocket.accountbook.query.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.genesis.unipocket.accountbook.query.persistence.repository.AccountBookQueryRepository;
import com.genesis.unipocket.accountbook.query.persistence.response.AccountBookAmountResponse;
import com.genesis.unipocket.accountbook.query.persistence.response.AccountBookDetailResponse;
import com.genesis.unipocket.analysis.command.persistence.entity.AccountMonthlyAggregateEntity;
import com.genesis.unipocket.analysis.command.persistence.repository.AccountMonthlyAggregateRepository;
import com.genesis.unipocket.analysis.command.persistence.repository.AnalysisBatchAggregationRepository;
import com.genesis.unipocket.analysis.command.persistence.repository.AnalysisMonthlyDirtyRepository;
import com.genesis.unipocket.analysis.common.enums.AnalysisBatchJobStatus;
import com.genesis.unipocket.analysis.common.enums.AnalysisMetricType;
import com.genesis.unipocket.analysis.common.enums.AnalysisQualityType;
import com.genesis.unipocket.exchange.common.service.ExchangeRateService;
import com.genesis.unipocket.expense.command.persistence.repository.ExpenseRepository;
import com.genesis.unipocket.global.common.enums.CountryCode;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.global.util.CountryCodeTimezoneMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccountBookAmountQueryService 단위 테스트")
class AccountBookAmountQueryServiceTest {

	@Mock private AccountBookQueryRepository accountBookQueryRepository;
	@Mock private AccountMonthlyAggregateRepository accountMonthlyAggregateRepository;
	@Mock private AnalysisMonthlyDirtyRepository analysisMonthlyDirtyRepository;
	@Mock private AnalysisBatchAggregationRepository analysisBatchAggregationRepository;
	@Mock private ExchangeRateService exchangeRateService;
	@Mock private ExpenseRepository expenseRepository;

	@InjectMocks private AccountBookAmountQueryService service;

	private final String userId = UUID.randomUUID().toString();

	@Test
	@DisplayName("더티 월이 없으면 월 집계 합계와 이번달 집계를 그대로 반환")
	void getAccountBookAmount_usesMonthlyAggregates_whenNoDirtyMonths() {
		Long accountBookId = 1L;
		CountryCode localCountry = CountryCode.JP;
		CountryCode baseCountry = CountryCode.KR;
		ZoneId zoneId = CountryCodeTimezoneMapper.getZoneId(localCountry);
		LocalDate thisMonth = LocalDate.now(zoneId).withDayOfMonth(1);

		given(accountBookQueryRepository.findDetailById(UUID.fromString(userId), accountBookId))
				.willReturn(
						Optional.of(
								new AccountBookDetailResponse(
										accountBookId,
										"ab",
										localCountry,
										baseCountry,
										LocalDate.of(2026, 1, 1),
										LocalDate.of(2026, 12, 31))));
		given(
						analysisMonthlyDirtyRepository
								.findTargetYearMonthsByCountryCodeAndAccountBookIdAndStatusNot(
										localCountry,
										accountBookId,
										AnalysisBatchJobStatus.SUCCESS))
				.willReturn(List.of());

		given(
						accountMonthlyAggregateRepository
								.sumMetricValueByAccountBookIdAndMetricTypeAndQualityType(
										accountBookId,
										AnalysisMetricType.TOTAL_LOCAL_AMOUNT,
										AnalysisQualityType.CLEANED))
				.willReturn(new BigDecimal("5000.00"));
		given(
						accountMonthlyAggregateRepository
								.sumMetricValueByAccountBookIdAndMetricTypeAndQualityType(
										accountBookId,
										AnalysisMetricType.TOTAL_BASE_AMOUNT,
										AnalysisQualityType.CLEANED))
				.willReturn(new BigDecimal("47000.00"));

		given(
						accountMonthlyAggregateRepository
								.findByAccountBookIdAndTargetYearMonthAndMetricTypeAndQualityType(
										accountBookId,
										thisMonth,
										AnalysisMetricType.TOTAL_LOCAL_AMOUNT,
										AnalysisQualityType.CLEANED))
				.willReturn(
						Optional.of(
								AccountMonthlyAggregateEntity.of(
										accountBookId,
										localCountry,
										thisMonth,
										AnalysisMetricType.TOTAL_LOCAL_AMOUNT,
										AnalysisQualityType.CLEANED,
										new BigDecimal("1200.00"))));
		given(
						accountMonthlyAggregateRepository
								.findByAccountBookIdAndTargetYearMonthAndMetricTypeAndQualityType(
										accountBookId,
										thisMonth,
										AnalysisMetricType.TOTAL_BASE_AMOUNT,
										AnalysisQualityType.CLEANED))
				.willReturn(
						Optional.of(
								AccountMonthlyAggregateEntity.of(
										accountBookId,
										localCountry,
										thisMonth,
										AnalysisMetricType.TOTAL_BASE_AMOUNT,
										AnalysisQualityType.CLEANED,
										new BigDecimal("11000.00"))));

		AccountBookAmountResponse result = service.getAccountBookAmount(userId, accountBookId);

		assertThat(result.localCountryCode()).isEqualTo(localCountry);
		assertThat(result.localCurrencyCode()).isEqualTo(CurrencyCode.JPY);
		assertThat(result.baseCountryCode()).isEqualTo(baseCountry);
		assertThat(result.baseCurrencyCode()).isEqualTo(CurrencyCode.KRW);
		assertThat(result.totalLocalAmount()).isEqualByComparingTo("5000.00");
		assertThat(result.totalBaseAmount()).isEqualByComparingTo("47000.00");
		assertThat(result.thisMonthLocalAmount()).isEqualByComparingTo("1200.00");
		assertThat(result.thisMonthBaseAmount()).isEqualByComparingTo("11000.00");
		verifyNoInteractions(exchangeRateService);
	}

	@Test
	@DisplayName("더티 이번달은 raw fallback으로 재계산하고 total에는 stale 집계를 대체 반영")
	void getAccountBookAmount_usesDirtyRawFallback_withMixedCurrencies() {
		Long accountBookId = 2L;
		CountryCode localCountry = CountryCode.JP;
		CountryCode baseCountry = CountryCode.KR;
		ZoneId zoneId = CountryCodeTimezoneMapper.getZoneId(localCountry);
		LocalDate thisMonth = LocalDate.now(zoneId).withDayOfMonth(1);

		given(accountBookQueryRepository.findDetailById(UUID.fromString(userId), accountBookId))
				.willReturn(
						Optional.of(
								new AccountBookDetailResponse(
										accountBookId,
										"ab",
										localCountry,
										baseCountry,
										LocalDate.of(2026, 1, 1),
										LocalDate.of(2026, 12, 31))));
		given(
						analysisMonthlyDirtyRepository
								.findTargetYearMonthsByCountryCodeAndAccountBookIdAndStatusNot(
										localCountry,
										accountBookId,
										AnalysisBatchJobStatus.SUCCESS))
				.willReturn(List.of(thisMonth));

		given(
						accountMonthlyAggregateRepository
								.sumMetricValueByAccountBookIdAndMetricTypeAndQualityType(
										accountBookId,
										AnalysisMetricType.TOTAL_LOCAL_AMOUNT,
										AnalysisQualityType.CLEANED))
				.willReturn(new BigDecimal("5000.00"));
		given(
						accountMonthlyAggregateRepository
								.sumMetricValueByAccountBookIdAndMetricTypeAndQualityType(
										accountBookId,
										AnalysisMetricType.TOTAL_BASE_AMOUNT,
										AnalysisQualityType.CLEANED))
				.willReturn(new BigDecimal("47000.00"));
		given(
						accountMonthlyAggregateRepository
								.sumMetricValueByAccountBookIdAndTargetYearMonthInAndMetricTypeAndQualityType(
										accountBookId,
										List.of(thisMonth),
										AnalysisMetricType.TOTAL_LOCAL_AMOUNT,
										AnalysisQualityType.CLEANED))
				.willReturn(new BigDecimal("300.00"));
		given(
						accountMonthlyAggregateRepository
								.sumMetricValueByAccountBookIdAndTargetYearMonthInAndMetricTypeAndQualityType(
										accountBookId,
										List.of(thisMonth),
										AnalysisMetricType.TOTAL_BASE_AMOUNT,
										AnalysisQualityType.CLEANED))
				.willReturn(new BigDecimal("3000.00"));

		given(
						analysisBatchAggregationRepository.aggregateAccountBookMonthlyRaw(
								eq(accountBookId), any(), any()))
				.willReturn(
						new AnalysisBatchAggregationRepository.AmountPairCount(
								new BigDecimal("999.00"), new BigDecimal("3500.00"), 2L));
		given(
						analysisBatchAggregationRepository.aggregateLocalAmountGroupedByCurrency(
								eq(accountBookId), any(), any()))
				.willReturn(
						List.of(
								new AnalysisBatchAggregationRepository.LocalCurrencyGroupRow(
										"JPY", new BigDecimal("100.00"), 1L),
								new AnalysisBatchAggregationRepository.LocalCurrencyGroupRow(
										"USD", new BigDecimal("10.00"), 1L)));
		given(
						exchangeRateService.convertAmount(
								new BigDecimal("10.00"),
								CurrencyCode.USD,
								CurrencyCode.JPY,
								OffsetDateTime.of(
										thisMonth.getYear(),
										thisMonth.getMonthValue(),
										thisMonth.getDayOfMonth(),
										0,
										0,
										0,
										0,
										ZoneOffset.UTC)))
				.willReturn(new BigDecimal("1500.00"));

		AccountBookAmountResponse result = service.getAccountBookAmount(userId, accountBookId);

		assertThat(result.totalLocalAmount()).isEqualByComparingTo("6300.00");
		assertThat(result.totalBaseAmount()).isEqualByComparingTo("47500.00");
		assertThat(result.thisMonthLocalAmount()).isEqualByComparingTo("1600.00");
		assertThat(result.thisMonthBaseAmount()).isEqualByComparingTo("3500.00");
		verify(exchangeRateService, org.mockito.Mockito.times(2))
				.convertAmount(
						new BigDecimal("10.00"),
						CurrencyCode.USD,
						CurrencyCode.JPY,
						OffsetDateTime.of(
								thisMonth.getYear(),
								thisMonth.getMonthValue(),
								thisMonth.getDayOfMonth(),
								0,
								0,
								0,
								0,
								ZoneOffset.UTC));
	}

	@Test
	@DisplayName("가계부 접근 권한이 없으면 ACCOUNT_BOOK_NOT_FOUND")
	void getAccountBookAmount_notFound() {
		Long accountBookId = 99L;
		given(accountBookQueryRepository.findDetailById(UUID.fromString(userId), accountBookId))
				.willReturn(Optional.empty());

		assertThatThrownBy(() -> service.getAccountBookAmount(userId, accountBookId))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("code", ErrorCode.ACCOUNT_BOOK_NOT_FOUND);
	}
}
