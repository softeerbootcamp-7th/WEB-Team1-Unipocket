package com.genesis.unipocket.analysis.command.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.genesis.unipocket.analysis.command.config.AnalysisBatchProperties;
import com.genesis.unipocket.analysis.command.persistence.repository.AccountMonthlyAggregateRepository;
import com.genesis.unipocket.analysis.command.persistence.repository.AccountMonthlyCategoryAggregateRepository;
import com.genesis.unipocket.analysis.command.persistence.repository.AnalysisBatchAggregationRepository;
import com.genesis.unipocket.analysis.command.persistence.repository.AnalysisBatchAggregationRepository.AccountAmountCount;
import com.genesis.unipocket.analysis.command.persistence.repository.AnalysisMonthlyDirtyRepository;
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
import org.springframework.transaction.PlatformTransactionManager;

@Tag("stress")
@ExtendWith(MockitoExtension.class)
class CountryMonthlyDirtyAggregationServiceStressTest {

	@Mock private AnalysisMonthlyDirtyRepository monthlyDirtyRepository;
	@Mock private AnalysisBatchAggregationRepository aggregationRepository;
	@Mock private PairMonthlyAggregateRepository pairMonthlyAggregateRepository;
	@Mock private PairMonthlyCategoryAggregateRepository pairMonthlyCategoryAggregateRepository;
	@Mock private AnalysisBatchProperties properties;
	@Mock private PlatformTransactionManager transactionManager;
	@Mock private AccountMonthlyAggregateRepository accountMonthlyAggregateRepository;

	@Mock
	private AccountMonthlyCategoryAggregateRepository accountMonthlyCategoryAggregateRepository;

	@Mock
	private com.genesis.unipocket.accountbook.command.persistence.repository
					.AccountBookCommandRepository
			accountBookRepository;

	@InjectMocks private CountryMonthlyDirtyAggregationService service;

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
		// Given
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

		// When
		long startTime = System.currentTimeMillis();
		invokeRefreshPairMonthlyAggregatesByCurrency();
		long endTime = System.currentTimeMillis();

		// Then
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

	private void invokeRefreshPairMonthlyAggregatesByCurrency() throws Exception {
		Class<?> pairMonthKeyClass =
				Class.forName(
						"com.genesis.unipocket.analysis.command.application.CountryMonthlyDirtyAggregationService$PairMonthKey");
		var constructor =
				pairMonthKeyClass.getDeclaredConstructor(
						CountryCode.class, CountryCode.class, LocalDate.class);
		constructor.setAccessible(true);
		Object key = constructor.newInstance(CountryCode.KR, CountryCode.US, LocalDate.now());

		Method method =
				CountryMonthlyDirtyAggregationService.class.getDeclaredMethod(
						"refreshPairMonthlyAggregatesByCurrency",
						pairMonthKeyClass,
						CurrencyType.class,
						AnalysisMetricType.class);
		method.setAccessible(true);

		method.invoke(service, key, CurrencyType.LOCAL, AnalysisMetricType.TOTAL_LOCAL_AMOUNT);
	}
}
