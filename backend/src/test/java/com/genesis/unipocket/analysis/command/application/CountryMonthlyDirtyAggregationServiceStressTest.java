package com.genesis.unipocket.analysis.command.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.genesis.unipocket.analysis.command.config.AnalysisBatchProperties;
import com.genesis.unipocket.analysis.command.persistence.repository.AnalysisBatchAggregationRepository;
import com.genesis.unipocket.analysis.command.persistence.repository.AnalysisBatchAggregationRepository.AccountAmountCount;
import com.genesis.unipocket.analysis.command.persistence.repository.PairMonthlyAggregateRepository;
import com.genesis.unipocket.analysis.command.persistence.repository.PairMonthlyCategoryAggregateRepository;
import com.genesis.unipocket.analysis.common.enums.AnalysisMetricType;
import com.genesis.unipocket.analysis.common.enums.CurrencyType;
import com.genesis.unipocket.global.common.enums.CountryCode;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("stress")
@ExtendWith(MockitoExtension.class)
class CountryMonthlyDirtyAggregationServiceStressTest {

	@Mock private AnalysisBatchAggregationRepository aggregationRepository;
	@Mock private PairMonthlyAggregateRepository pairMonthlyAggregateRepository;
	@Mock private PairMonthlyCategoryAggregateRepository pairMonthlyCategoryAggregateRepository;
	@Mock private AnalysisBatchProperties properties;

	@InjectMocks private PairMonthlyAggregateRefresher pairRefresher;

	@BeforeEach
	void setUp() {}

	@Test
	@DisplayName("Stress Test: refreshPairMonthlyAggregates with 100,000 records")
	void stressTest_largeDataSet() throws Exception {
		runStressTest(100_000);
	}

	@Test
	@DisplayName("Stress Test: refreshPairMonthlyAggregates with 1,000,000 records")
	void stressTest_hugeDataSet() throws Exception {
		runStressTest(1_000_000);
	}

	private void runStressTest(int recordCount) throws Exception {
		List<AccountAmountCount> largeDataSet = createMockRows(recordCount);

		given(properties.getPeerMinSampleSize()).willReturn(10);
		given(properties.getOutlierIqrMultiplier()).willReturn(1.5);

		given(
						aggregationRepository.aggregatePairMonthlyTotalByAccountFromMonthly(
								any(), any(), any(), any(), any()))
				.willReturn(largeDataSet);

		given(
						aggregationRepository.aggregatePairMonthlyCategoryByAccountFromMonthly(
								any(), any(), any(), any(), any()))
				.willReturn(Collections.emptyList());

		given(
						pairMonthlyAggregateRepository
								.findByLocalCountryCodeAndBaseCountryCodeAndTargetYearMonthAndQualityTypeAndMetricType(
										any(), any(), any(), any(), any()))
				.willReturn(Optional.empty());

		PairMonthKey key = new PairMonthKey(CountryCode.KR, CountryCode.US, LocalDate.now());

		long startTime = System.currentTimeMillis();
		invokeRefreshByCurrency(key);
		long endTime = System.currentTimeMillis();

		System.out.printf(
				"Stress Test (N=%d): Execution time = %d ms%n", recordCount, (endTime - startTime));
	}

	private List<AccountAmountCount> createMockRows(int count) {
		List<AccountAmountCount> rows = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			rows.add(
					new AccountAmountCount(
							(long) i, BigDecimal.valueOf(Math.random() * 10000), 1L));
		}
		return rows;
	}

	private void invokeRefreshByCurrency(PairMonthKey key) throws Exception {
		Method method =
				PairMonthlyAggregateRefresher.class.getDeclaredMethod(
						"refreshByCurrency",
						PairMonthKey.class,
						CurrencyType.class,
						AnalysisMetricType.class);
		method.setAccessible(true);
		method.invoke(
				pairRefresher, key, CurrencyType.LOCAL, AnalysisMetricType.TOTAL_LOCAL_AMOUNT);
	}
}
