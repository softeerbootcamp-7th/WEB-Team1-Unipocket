package com.genesis.unipocket.analysis.command.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.genesis.unipocket.analysis.command.config.AnalysisBatchProperties;
import com.genesis.unipocket.analysis.command.persistence.repository.support.AnalysisBatchAggregationRepository.AccountAmountCount;
import com.genesis.unipocket.analysis.command.persistence.repository.support.AnalysisBatchAggregationRepository.CategoryAmountPairCount;
import com.genesis.unipocket.analysis.command.persistence.repository.support.AnalysisBatchAggregationRepository.CategoryLocalCurrencyGroupRow;
import com.genesis.unipocket.analysis.command.persistence.repository.support.AnalysisBatchAggregationRepository.LocalCurrencyGroupRow;
import com.genesis.unipocket.exchange.common.service.ExchangeRateService;
import com.genesis.unipocket.global.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
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
	@Mock private ExchangeRateService exchangeRateService;

	@InjectMocks private CountryMonthlyDirtyAggregationService service;

	private static final OffsetDateTime REF_DATE_TIME =
			OffsetDateTime.of(2026, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);

	@BeforeEach
	void setUp() {
		// Default mock behavior if needed
	}

	// ─── computePairIqrBounds (기존 테스트) ──────────────────────────────────

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
		given(properties.getOutlierIqrMultiplier()).willReturn(1.5);

		List<AccountAmountCount> rows = createMockRows(minSampleSize);

		// When
		Object result = invokeComputePairIqrBounds(rows);

		// Then
		assertThat(result).isNotNull();
	}

	// ─── computeCorrectedLocalAmount ─────────────────────────────────────────

	@Test
	@DisplayName("동일 통화인 경우 환율 변환 없이 그대로 합산")
	void computeCorrectedLocalAmount_sameCurrency_sumsDirectly() throws Exception {
		// Given
		CurrencyCode target = CurrencyCode.USD;
		List<LocalCurrencyGroupRow> groups =
				List.of(new LocalCurrencyGroupRow("USD", new BigDecimal("150.00"), 2L));

		// When
		BigDecimal result = invokeComputeCorrectedLocalAmount(groups, target, REF_DATE_TIME);

		// Then
		assertThat(result).isEqualByComparingTo("150.00");
		verifyNoInteractions(exchangeRateService);
	}

	@Test
	@DisplayName("다른 통화인 경우 환율 변환 후 합산")
	void computeCorrectedLocalAmount_differentCurrency_convertsAndSums() throws Exception {
		// Given
		CurrencyCode target = CurrencyCode.USD;
		given(
						exchangeRateService.convertAmount(
								new BigDecimal("10000.00"),
								CurrencyCode.JPY,
								CurrencyCode.USD,
								REF_DATE_TIME))
				.willReturn(new BigDecimal("70.00"));

		List<LocalCurrencyGroupRow> groups =
				List.of(
						new LocalCurrencyGroupRow("USD", new BigDecimal("100.00"), 1L),
						new LocalCurrencyGroupRow("JPY", new BigDecimal("10000.00"), 1L));

		// When
		BigDecimal result = invokeComputeCorrectedLocalAmount(groups, target, REF_DATE_TIME);

		// Then
		assertThat(result).isEqualByComparingTo("170.00"); // 100 + 70
		verify(exchangeRateService)
				.convertAmount(
						new BigDecimal("10000.00"),
						CurrencyCode.JPY,
						CurrencyCode.USD,
						REF_DATE_TIME);
	}

	@Test
	@DisplayName("여러 통화 혼합 시 각각 변환 후 합산")
	void computeCorrectedLocalAmount_multipleDifferentCurrencies_convertsEachAndSums()
			throws Exception {
		// Given
		CurrencyCode target = CurrencyCode.KRW;
		given(
						exchangeRateService.convertAmount(
								new BigDecimal("100.00"),
								CurrencyCode.USD,
								CurrencyCode.KRW,
								REF_DATE_TIME))
				.willReturn(new BigDecimal("140000.00"));
		given(
						exchangeRateService.convertAmount(
								new BigDecimal("10000.00"),
								CurrencyCode.JPY,
								CurrencyCode.KRW,
								REF_DATE_TIME))
				.willReturn(new BigDecimal("90000.00"));

		List<LocalCurrencyGroupRow> groups =
				List.of(
						new LocalCurrencyGroupRow("KRW", new BigDecimal("50000.00"), 1L),
						new LocalCurrencyGroupRow("USD", new BigDecimal("100.00"), 1L),
						new LocalCurrencyGroupRow("JPY", new BigDecimal("10000.00"), 1L));

		// When
		BigDecimal result = invokeComputeCorrectedLocalAmount(groups, target, REF_DATE_TIME);

		// Then
		assertThat(result).isEqualByComparingTo("280000.00"); // 50000 + 140000 + 90000
	}

	@Test
	@DisplayName("localCurrencyCode가 null인 항목은 건너뜀")
	void computeCorrectedLocalAmount_nullCurrencyCode_skipsEntry() throws Exception {
		// Given
		CurrencyCode target = CurrencyCode.USD;
		List<LocalCurrencyGroupRow> groups =
				List.of(
						new LocalCurrencyGroupRow(null, new BigDecimal("999.00"), 1L),
						new LocalCurrencyGroupRow("USD", new BigDecimal("100.00"), 1L));

		// When
		BigDecimal result = invokeComputeCorrectedLocalAmount(groups, target, REF_DATE_TIME);

		// Then
		assertThat(result).isEqualByComparingTo("100.00");
		verifyNoInteractions(exchangeRateService);
	}

	// ─── computeCorrectedCategoryRows ────────────────────────────────────────

	@Test
	@DisplayName("카테고리 내 동일 통화 — base는 그대로, local은 변환 없이 교체")
	void computeCorrectedCategoryRows_sameCurrency_keepsBaseAndReplacesLocal() throws Exception {
		// Given
		CurrencyCode target = CurrencyCode.USD;
		int foodOrdinal = Category.FOOD.ordinal();

		List<CategoryAmountPairCount> rawRows =
				List.of(
						new CategoryAmountPairCount(
								foodOrdinal,
								new BigDecimal("999.00"), // 기존 잘못된 local (교체될 값)
								new BigDecimal("70.00"), // base — 변경 없어야 함
								1L));

		List<CategoryLocalCurrencyGroupRow> currencyRows =
				List.of(
						new CategoryLocalCurrencyGroupRow(
								foodOrdinal, "USD", new BigDecimal("100.00"), 1L));

		// When
		List<CategoryAmountPairCount> result =
				invokeComputeCorrectedCategoryRows(rawRows, currencyRows, target, REF_DATE_TIME);

		// Then
		assertThat(result).hasSize(1);
		assertThat(result.get(0).totalLocalAmount()).isEqualByComparingTo("100.00");
		assertThat(result.get(0).totalBaseAmount()).isEqualByComparingTo("70.00"); // base 불변
		verifyNoInteractions(exchangeRateService);
	}

	@Test
	@DisplayName("카테고리 내 다른 통화 — 환율 변환 후 local 교체, base는 불변")
	void computeCorrectedCategoryRows_differentCurrency_convertsLocalKeepsBase() throws Exception {
		// Given
		CurrencyCode target = CurrencyCode.USD;
		int foodOrdinal = Category.FOOD.ordinal();

		given(
						exchangeRateService.convertAmount(
								new BigDecimal("10000.00"),
								CurrencyCode.JPY,
								CurrencyCode.USD,
								REF_DATE_TIME))
				.willReturn(new BigDecimal("70.00"));

		List<CategoryAmountPairCount> rawRows =
				List.of(
						new CategoryAmountPairCount(
								foodOrdinal,
								new BigDecimal("10000.00"), // 잘못된 raw local
								new BigDecimal("67.00"), // base — 변경 없어야 함
								1L));

		List<CategoryLocalCurrencyGroupRow> currencyRows =
				List.of(
						new CategoryLocalCurrencyGroupRow(
								foodOrdinal, "JPY", new BigDecimal("10000.00"), 1L));

		// When
		List<CategoryAmountPairCount> result =
				invokeComputeCorrectedCategoryRows(rawRows, currencyRows, target, REF_DATE_TIME);

		// Then
		assertThat(result).hasSize(1);
		assertThat(result.get(0).totalLocalAmount()).isEqualByComparingTo("70.00");
		assertThat(result.get(0).totalBaseAmount()).isEqualByComparingTo("67.00");
	}

	@Test
	@DisplayName("카테고리 내 혼합 통화 — 각 통화를 변환 후 합산한 local 반영")
	void computeCorrectedCategoryRows_mixedCurrenciesInCategory_mergesCorrectly() throws Exception {
		// Given
		CurrencyCode target = CurrencyCode.USD;
		int foodOrdinal = Category.FOOD.ordinal();

		given(
						exchangeRateService.convertAmount(
								new BigDecimal("10000.00"),
								CurrencyCode.JPY,
								CurrencyCode.USD,
								REF_DATE_TIME))
				.willReturn(new BigDecimal("70.00"));

		List<CategoryAmountPairCount> rawRows =
				List.of(
						new CategoryAmountPairCount(
								foodOrdinal,
								new BigDecimal("999.00"),
								new BigDecimal("120.00"),
								2L));

		// 같은 카테고리 내 USD + JPY 혼합
		List<CategoryLocalCurrencyGroupRow> currencyRows =
				List.of(
						new CategoryLocalCurrencyGroupRow(
								foodOrdinal, "USD", new BigDecimal("50.00"), 1L),
						new CategoryLocalCurrencyGroupRow(
								foodOrdinal, "JPY", new BigDecimal("10000.00"), 1L));

		// When
		List<CategoryAmountPairCount> result =
				invokeComputeCorrectedCategoryRows(rawRows, currencyRows, target, REF_DATE_TIME);

		// Then
		assertThat(result).hasSize(1);
		assertThat(result.get(0).totalLocalAmount()).isEqualByComparingTo("120.00"); // 50 + 70
		assertThat(result.get(0).totalBaseAmount()).isEqualByComparingTo("120.00"); // base 불변
	}

	// ─── Reflection helpers ───────────────────────────────────────────────────

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

	@SuppressWarnings("unchecked")
	private BigDecimal invokeComputeCorrectedLocalAmount(
			List<LocalCurrencyGroupRow> groups, CurrencyCode target, OffsetDateTime refDateTime)
			throws Exception {
		Method method =
				CountryMonthlyDirtyAggregationService.class.getDeclaredMethod(
						"computeCorrectedLocalAmount",
						List.class,
						CurrencyCode.class,
						OffsetDateTime.class);
		method.setAccessible(true);
		return (BigDecimal) method.invoke(service, groups, target, refDateTime);
	}

	@SuppressWarnings("unchecked")
	private List<CategoryAmountPairCount> invokeComputeCorrectedCategoryRows(
			List<CategoryAmountPairCount> rawRows,
			List<CategoryLocalCurrencyGroupRow> currencyRows,
			CurrencyCode target,
			OffsetDateTime refDateTime)
			throws Exception {
		Method method =
				CountryMonthlyDirtyAggregationService.class.getDeclaredMethod(
						"computeCorrectedCategoryRows",
						List.class,
						List.class,
						CurrencyCode.class,
						OffsetDateTime.class);
		method.setAccessible(true);
		return (List<CategoryAmountPairCount>)
				method.invoke(service, rawRows, currencyRows, target, refDateTime);
	}
}
