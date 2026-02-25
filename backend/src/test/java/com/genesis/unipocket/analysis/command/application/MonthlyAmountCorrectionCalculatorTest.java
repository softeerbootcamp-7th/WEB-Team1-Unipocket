package com.genesis.unipocket.analysis.command.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.genesis.unipocket.analysis.command.persistence.repository.AnalysisBatchAggregationRepository.CategoryAmountPairCount;
import com.genesis.unipocket.analysis.command.persistence.repository.AnalysisBatchAggregationRepository.CategoryLocalCurrencyGroupRow;
import com.genesis.unipocket.analysis.command.persistence.repository.AnalysisBatchAggregationRepository.LocalCurrencyGroupRow;
import com.genesis.unipocket.exchange.common.service.ExchangeRateService;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MonthlyAmountCorrectionCalculatorTest {

	@Mock private ExchangeRateService exchangeRateService;

	@InjectMocks private MonthlyAmountCorrectionCalculator calculator;

	private static final OffsetDateTime REF = OffsetDateTime.parse("2025-12-01T00:00:00Z");

	// ===== computeCorrectedLocalAmount =====

	@Test
	@DisplayName("동일 통화 그룹은 환율 변환 없이 그대로 합산된다")
	void computeCorrectedLocalAmount_sameCurrency_noConversion() {
		List<LocalCurrencyGroupRow> groups =
				List.of(
						new LocalCurrencyGroupRow(
								CurrencyCode.KRW.name(), new BigDecimal("10000"), 2),
						new LocalCurrencyGroupRow(
								CurrencyCode.KRW.name(), new BigDecimal("5000"), 1));

		BigDecimal result = calculator.computeCorrectedLocalAmount(groups, CurrencyCode.KRW, REF);

		assertThat(result).isEqualByComparingTo("15000");
		verify(exchangeRateService, never()).convertAmount(any(), any(), any(), any());
	}

	@Test
	@DisplayName("다른 통화 그룹은 환율 서비스를 통해 변환된다")
	void computeCorrectedLocalAmount_differentCurrency_convertsViaExchangeService() {
		when(exchangeRateService.convertAmount(
						eq(new BigDecimal("100")),
						eq(CurrencyCode.USD),
						eq(CurrencyCode.KRW),
						eq(REF)))
				.thenReturn(new BigDecimal("135000"));

		List<LocalCurrencyGroupRow> groups =
				List.of(
						new LocalCurrencyGroupRow(
								CurrencyCode.USD.name(), new BigDecimal("100"), 1));

		BigDecimal result = calculator.computeCorrectedLocalAmount(groups, CurrencyCode.KRW, REF);

		assertThat(result).isEqualByComparingTo("135000");
	}

	@Test
	@DisplayName("혼합 통화 그룹은 동일 통화는 직접 합산, 다른 통화는 변환 후 합산한다")
	void computeCorrectedLocalAmount_mixedCurrencies_correctlyAggregated() {
		when(exchangeRateService.convertAmount(
						eq(new BigDecimal("50")),
						eq(CurrencyCode.USD),
						eq(CurrencyCode.KRW),
						any()))
				.thenReturn(new BigDecimal("67500"));

		List<LocalCurrencyGroupRow> groups =
				List.of(
						new LocalCurrencyGroupRow(
								CurrencyCode.KRW.name(), new BigDecimal("10000"), 1),
						new LocalCurrencyGroupRow(
								CurrencyCode.USD.name(), new BigDecimal("50"), 1));

		BigDecimal result = calculator.computeCorrectedLocalAmount(groups, CurrencyCode.KRW, REF);

		assertThat(result).isEqualByComparingTo("77500");
	}

	@Test
	@DisplayName("null 통화 코드 그룹은 무시된다")
	void computeCorrectedLocalAmount_nullCurrencyCode_skipped() {
		List<LocalCurrencyGroupRow> groups =
				List.of(new LocalCurrencyGroupRow(null, new BigDecimal("9999"), 1));

		BigDecimal result = calculator.computeCorrectedLocalAmount(groups, CurrencyCode.KRW, REF);

		assertThat(result).isEqualByComparingTo("0");
		verify(exchangeRateService, never()).convertAmount(any(), any(), any(), any());
	}

	@Test
	@DisplayName("유효하지 않은 통화 코드 문자열은 무시된다")
	void computeCorrectedLocalAmount_invalidCurrencyString_skipped() {
		List<LocalCurrencyGroupRow> groups =
				List.of(new LocalCurrencyGroupRow("INVALID_CURRENCY", new BigDecimal("5000"), 1));

		BigDecimal result = calculator.computeCorrectedLocalAmount(groups, CurrencyCode.KRW, REF);

		assertThat(result).isEqualByComparingTo("0");
	}

	@Test
	@DisplayName("빈 그룹 리스트는 0을 반환한다")
	void computeCorrectedLocalAmount_emptyList_returnsZero() {
		BigDecimal result =
				calculator.computeCorrectedLocalAmount(List.of(), CurrencyCode.KRW, REF);

		assertThat(result).isEqualByComparingTo("0");
	}

	@Test
	@DisplayName("ordinal 숫자 문자열로 통화 코드를 파싱할 수 있다")
	void computeCorrectedLocalAmount_ordinalCurrencyCode_parsed() {
		String krwOrdinal = String.valueOf(CurrencyCode.KRW.ordinal());
		List<LocalCurrencyGroupRow> groups =
				List.of(new LocalCurrencyGroupRow(krwOrdinal, new BigDecimal("3000"), 1));

		BigDecimal result = calculator.computeCorrectedLocalAmount(groups, CurrencyCode.KRW, REF);

		assertThat(result).isEqualByComparingTo("3000");
		verify(exchangeRateService, never()).convertAmount(any(), any(), any(), any());
	}

	// ===== computeCorrectedCategoryRows =====

	@Test
	@DisplayName("동일 통화 카테고리 행은 변환 없이 로컬 금액을 유지한다")
	void computeCorrectedCategoryRows_sameCurrency_noConversion() {
		List<CategoryAmountPairCount> rawRows =
				List.of(
						new CategoryAmountPairCount(
								1, new BigDecimal("5000"), new BigDecimal("50"), 2));
		List<CategoryLocalCurrencyGroupRow> currencyRows =
				List.of(
						new CategoryLocalCurrencyGroupRow(
								1, CurrencyCode.KRW.name(), new BigDecimal("5000"), 2));

		List<CategoryAmountPairCount> result =
				calculator.computeCorrectedCategoryRows(
						rawRows, currencyRows, CurrencyCode.KRW, REF);

		assertThat(result).hasSize(1);
		assertThat(result.get(0).totalLocalAmount()).isEqualByComparingTo("5000");
		assertThat(result.get(0).totalBaseAmount()).isEqualByComparingTo("50"); // 그대로 유지
		verify(exchangeRateService, never()).convertAmount(any(), any(), any(), any());
	}

	@Test
	@DisplayName("다른 통화 카테고리 행은 환율 변환 후 로컬 금액이 교체된다")
	void computeCorrectedCategoryRows_differentCurrency_localAmountConverted() {
		when(exchangeRateService.convertAmount(
						eq(new BigDecimal("100")),
						eq(CurrencyCode.USD),
						eq(CurrencyCode.KRW),
						eq(REF)))
				.thenReturn(new BigDecimal("135000"));

		List<CategoryAmountPairCount> rawRows =
				List.of(
						new CategoryAmountPairCount(
								1, new BigDecimal("999999"), new BigDecimal("100"), 1));
		List<CategoryLocalCurrencyGroupRow> currencyRows =
				List.of(
						new CategoryLocalCurrencyGroupRow(
								1, CurrencyCode.USD.name(), new BigDecimal("100"), 1));

		List<CategoryAmountPairCount> result =
				calculator.computeCorrectedCategoryRows(
						rawRows, currencyRows, CurrencyCode.KRW, REF);

		assertThat(result).hasSize(1);
		assertThat(result.get(0).totalLocalAmount()).isEqualByComparingTo("135000");
	}

	@Test
	@DisplayName("null 카테고리 ordinal 통화 행은 무시된다")
	void computeCorrectedCategoryRows_nullCategoryOrdinal_skipped() {
		List<CategoryAmountPairCount> rawRows =
				List.of(
						new CategoryAmountPairCount(
								1, new BigDecimal("5000"), new BigDecimal("50"), 1));
		List<CategoryLocalCurrencyGroupRow> currencyRows =
				List.of(
						new CategoryLocalCurrencyGroupRow(
								null, CurrencyCode.KRW.name(), new BigDecimal("1000"), 1));

		List<CategoryAmountPairCount> result =
				calculator.computeCorrectedCategoryRows(
						rawRows, currencyRows, CurrencyCode.KRW, REF);

		// currencyRows의 null 카테고리가 무시되어 correctedLocalByCategory가 비어 있음
		assertThat(result.get(0).totalLocalAmount()).isEqualByComparingTo("0");
	}

	@Test
	@DisplayName("null 통화 코드 카테고리 행은 무시된다")
	void computeCorrectedCategoryRows_nullCurrencyCode_skipped() {
		List<CategoryAmountPairCount> rawRows =
				List.of(
						new CategoryAmountPairCount(
								1, new BigDecimal("5000"), new BigDecimal("50"), 1));
		List<CategoryLocalCurrencyGroupRow> currencyRows =
				List.of(new CategoryLocalCurrencyGroupRow(1, null, new BigDecimal("5000"), 1));

		List<CategoryAmountPairCount> result =
				calculator.computeCorrectedCategoryRows(
						rawRows, currencyRows, CurrencyCode.KRW, REF);

		// null 통화 코드가 무시되어 보정 금액 없음 → 0 반환
		assertThat(result.get(0).totalLocalAmount()).isEqualByComparingTo("0");
	}
}
