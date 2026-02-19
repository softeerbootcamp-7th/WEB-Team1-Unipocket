package com.genesis.unipocket.analysis.command.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.genesis.unipocket.analysis.command.config.AnalysisBatchProperties;
import com.genesis.unipocket.analysis.command.persistence.repository.support.AnalysisBatchAggregationRepository.AccountAmountCount;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CountryMonthlyDirtyAggregationServiceTest {

	@Mock private AnalysisBatchProperties properties;

	@InjectMocks private CountryMonthlyDirtyAggregationService service;

	@BeforeEach
	void setUp() {
		// Default mock behavior if needed
	}

	@Test
	@DisplayName("Should return null bounds when sample size is less than configured minimum")
	void computePairIqrBounds_insufficientSamples_returnsNull() throws Exception {
		// Given
		int minSampleSize = 10;
		given(properties.getPeerMinSampleSize()).willReturn(minSampleSize);

		List<AccountAmountCount> rows = createMockRows(minSampleSize - 1);

		// When
		Object result = invokeComputePairIqrBounds(rows);

		// Then
		assertThat(result).isNull();
	}

	@Test
	@DisplayName("Should return valid bounds when sample size meets configured minimum")
	void computePairIqrBounds_sufficientSamples_returnsBounds() throws Exception {
		// Given
		int minSampleSize = 10;
		given(properties.getPeerMinSampleSize()).willReturn(minSampleSize);
		// Also need these for calculation
		given(properties.getOutlierIqrMultiplier()).willReturn(1.5);

		List<AccountAmountCount> rows = createMockRows(minSampleSize);

		// When
		Object result = invokeComputePairIqrBounds(rows);

		// Then
		assertThat(result).isNotNull();
	}

	private List<AccountAmountCount> createMockRows(int count) {
		List<AccountAmountCount> rows = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			rows.add(new AccountAmountCount((long) i, createMockAmount(i), 1L));
		}
		return rows;
	}

	private BigDecimal createMockAmount(int index) {
		if (index % 11 == 0) {
			return BigDecimal.valueOf(4000 + index);
		}
		if (index % 7 == 0) {
			return BigDecimal.valueOf(60 + index);
		}
		if (index % 5 == 0) {
			return BigDecimal.valueOf(210);
		}
		return BigDecimal.valueOf(150 + (index % 6) * 35L);
	}

	private Object invokeComputePairIqrBounds(List<AccountAmountCount> rows) throws Exception {
		Method method =
				CountryMonthlyDirtyAggregationService.class.getDeclaredMethod(
						"computePairIqrBounds", List.class);
		method.setAccessible(true);
		return method.invoke(service, rows);
	}
}
