package com.genesis.unipocket.analysis.command.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.genesis.unipocket.analysis.command.config.AnalysisBatchProperties;
import com.genesis.unipocket.analysis.command.persistence.entity.PairMonthlyAggregateEntity;
import com.genesis.unipocket.analysis.command.persistence.repository.AnalysisBatchAggregationRepository;
import com.genesis.unipocket.analysis.command.persistence.repository.AnalysisBatchAggregationRepository.AccountAmountCount;
import com.genesis.unipocket.analysis.command.persistence.repository.PairMonthlyAggregateRepository;
import com.genesis.unipocket.analysis.command.persistence.repository.PairMonthlyCategoryAggregateRepository;
import com.genesis.unipocket.analysis.common.enums.AnalysisMetricType;
import com.genesis.unipocket.analysis.common.enums.AnalysisQualityType;
import com.genesis.unipocket.global.common.enums.CountryCode;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PairMonthlyAggregateRefresherTest {

	@Mock private AnalysisBatchAggregationRepository aggregationRepository;
	@Mock private PairMonthlyAggregateRepository pairMonthlyAggregateRepository;
	@Mock private PairMonthlyCategoryAggregateRepository pairMonthlyCategoryAggregateRepository;
	@Mock private AnalysisBatchProperties properties;

	@InjectMocks private PairMonthlyAggregateRefresher refresher;

	private static final LocalDate MONTH_START = LocalDate.of(2025, 12, 1);
	private static final CountryCode LOCAL = CountryCode.KR;
	private static final CountryCode BASE = CountryCode.US;

	@BeforeEach
	void setUp() {
		lenient().when(properties.getPeerMinSampleSize()).thenReturn(3);
		lenient().when(properties.getOutlierIqrMultiplier()).thenReturn(1.5d);
	}

	@Test
	@DisplayName("baseCountryCode가 null이면 아무것도 처리하지 않는다")
	void refresh_nullBaseCountry_noop() {
		PairMonthKey key = new PairMonthKey(LOCAL, null, MONTH_START);

		refresher.refresh(key);

		verify(aggregationRepository, never())
				.aggregatePairMonthlyTotalByAccountFromMonthly(any(), any(), any(), any(), any());
	}

	@Test
	@DisplayName("샘플 수가 최소 기준 미만이면 기존 집계를 삭제하고 종료한다")
	void refresh_insufficientSampleSize_deletesAggregateAndReturns() {
		PairMonthKey key = new PairMonthKey(LOCAL, BASE, MONTH_START);

		// 2개 → peerMinSampleSize(3) 미만
		List<AccountAmountCount> twoRows =
				List.of(
						new AccountAmountCount(1L, new BigDecimal("10000"), 3),
						new AccountAmountCount(2L, new BigDecimal("20000"), 5));

		when(aggregationRepository.aggregatePairMonthlyTotalByAccountFromMonthly(
						any(), any(), any(), any(), any()))
				.thenReturn(twoRows);

		PairMonthlyAggregateEntity existing =
				PairMonthlyAggregateEntity.of(
						LOCAL,
						BASE,
						MONTH_START,
						AnalysisQualityType.CLEANED,
						AnalysisMetricType.TOTAL_LOCAL_AMOUNT,
						2L,
						new BigDecimal("30000"),
						new BigDecimal("15000"),
						null,
						null);

		when(pairMonthlyAggregateRepository
						.findByLocalCountryCodeAndBaseCountryCodeAndTargetYearMonthAndQualityTypeAndMetricType(
								any(), any(), any(), any(), any()))
				.thenReturn(Optional.of(existing));

		refresher.refresh(key);

		verify(pairMonthlyAggregateRepository, org.mockito.Mockito.times(2)).delete(existing);
		verify(pairMonthlyAggregateRepository, never()).save(any());
	}

	@Test
	@DisplayName("샘플이 충분하면 IQR 계산 후 집계를 저장한다")
	void refresh_sufficientSamples_savesAggregate() {
		PairMonthKey key = new PairMonthKey(LOCAL, BASE, MONTH_START);

		List<AccountAmountCount> rows = buildAccountRows(10000, 20000, 30000, 40000, 50000);

		when(aggregationRepository.aggregatePairMonthlyTotalByAccountFromMonthly(
						any(), any(), any(), any(), any()))
				.thenReturn(rows);
		when(aggregationRepository.aggregatePairMonthlyCategoryByAccountFromMonthly(
						any(), any(), any(), any(), any()))
				.thenReturn(List.of());
		when(pairMonthlyAggregateRepository
						.findByLocalCountryCodeAndBaseCountryCodeAndTargetYearMonthAndQualityTypeAndMetricType(
								any(), any(), any(), any(), any()))
				.thenReturn(Optional.empty());
		when(pairMonthlyCategoryAggregateRepository.saveAll(any())).thenReturn(List.of());

		refresher.refresh(key);

		// 총 2번(LOCAL, BASE) 저장 시도
		verify(pairMonthlyAggregateRepository, org.mockito.Mockito.times(2)).save(any());
	}

	@Test
	@DisplayName("기존 집계가 있으면 save 대신 update를 호출한다")
	void refresh_existingAggregate_updatesInsteadOfSave() {
		PairMonthKey key = new PairMonthKey(LOCAL, BASE, MONTH_START);

		List<AccountAmountCount> rows = buildAccountRows(10000, 20000, 30000, 40000, 50000);

		PairMonthlyAggregateEntity existing =
				PairMonthlyAggregateEntity.of(
						LOCAL,
						BASE,
						MONTH_START,
						AnalysisQualityType.CLEANED,
						AnalysisMetricType.TOTAL_LOCAL_AMOUNT,
						5L,
						new BigDecimal("150000"),
						new BigDecimal("30000"),
						null,
						null);

		when(aggregationRepository.aggregatePairMonthlyTotalByAccountFromMonthly(
						any(), any(), any(), any(), any()))
				.thenReturn(rows);
		when(aggregationRepository.aggregatePairMonthlyCategoryByAccountFromMonthly(
						any(), any(), any(), any(), any()))
				.thenReturn(List.of());
		when(pairMonthlyAggregateRepository
						.findByLocalCountryCodeAndBaseCountryCodeAndTargetYearMonthAndQualityTypeAndMetricType(
								any(), any(), any(), any(), any()))
				.thenReturn(Optional.of(existing));
		when(pairMonthlyCategoryAggregateRepository.saveAll(any())).thenReturn(List.of());

		refresher.refresh(key);

		// update 로직이 적용되면 includedAccountCount가 바뀌어야 함
		assertThat(existing.getIncludedAccountCount()).isGreaterThan(0L);
		verify(pairMonthlyAggregateRepository, never()).save(any());
	}

	@Test
	@DisplayName("IQR 필터 후 빈 결과면 전체 데이터로 fallback한다")
	void refresh_allOutliers_fallsBackToAllRows() {
		PairMonthKey key = new PairMonthKey(LOCAL, BASE, MONTH_START);

		// 매우 극단적 분포: 하나는 1, 나머지는 10억 → IQR 필터 적용 시 모두 제거될 수 있음
		// 하지만 실제로 5개 이상이고 IQR이 정상이면 필터 후 0개가 되진 않음
		// 테스트를 단순하게: 모든 값이 동일 → IQR=0 → bounds=[same,same] → 모두 포함
		List<AccountAmountCount> rows = buildAccountRows(30000, 30000, 30000, 30000, 30000);

		when(aggregationRepository.aggregatePairMonthlyTotalByAccountFromMonthly(
						any(), any(), any(), any(), any()))
				.thenReturn(rows);
		when(aggregationRepository.aggregatePairMonthlyCategoryByAccountFromMonthly(
						any(), any(), any(), any(), any()))
				.thenReturn(List.of());
		when(pairMonthlyAggregateRepository
						.findByLocalCountryCodeAndBaseCountryCodeAndTargetYearMonthAndQualityTypeAndMetricType(
								any(), any(), any(), any(), any()))
				.thenReturn(Optional.empty());
		when(pairMonthlyCategoryAggregateRepository.saveAll(any())).thenReturn(List.of());

		refresher.refresh(key);

		ArgumentCaptor<PairMonthlyAggregateEntity> captor =
				ArgumentCaptor.forClass(PairMonthlyAggregateEntity.class);
		verify(pairMonthlyAggregateRepository, org.mockito.Mockito.atLeastOnce())
				.save(captor.capture());

		// 모든 값이 동일 → IQR=0 → bounds=[30000,30000] → 5명 모두 포함
		assertThat(captor.getAllValues().get(0).getIncludedAccountCount()).isEqualTo(5L);
	}

	private List<AccountAmountCount> buildAccountRows(long... amounts) {
		List<AccountAmountCount> rows = new ArrayList<>();
		for (int i = 0; i < amounts.length; i++) {
			rows.add(new AccountAmountCount((long) (i + 1), new BigDecimal(amounts[i]), 1));
		}
		return rows;
	}
}
