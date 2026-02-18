package com.genesis.unipocket.analysis.common.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class QuantileUtilTest {

	@Test
	void linearInterpolatedQuantile_emptyList_returnsZero() {
		BigDecimal result = QuantileUtil.linearInterpolatedQuantile(List.of(), 0.25d);

		assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
	}

	@Test
	void linearInterpolatedQuantile_singleValue_returnsValue() {
		BigDecimal result =
				QuantileUtil.linearInterpolatedQuantile(List.of(new BigDecimal("9.99")), 0.75d);

		assertThat(result).isEqualByComparingTo("9.99");
	}

	@Test
	void linearInterpolatedQuantile_fractionalIndex_returnsInterpolatedValue() {
		BigDecimal result =
				QuantileUtil.linearInterpolatedQuantile(
						List.of(
								new BigDecimal("10"),
								new BigDecimal("20"),
								new BigDecimal("40"),
								new BigDecimal("70")),
						0.50d);

		assertThat(result).isEqualByComparingTo("30");
	}

	@Test
	void linearInterpolatedQuantile_probabilityOutOfRange_clampsToBounds() {
		List<BigDecimal> sorted =
				List.of(new BigDecimal("1"), new BigDecimal("2"), new BigDecimal("3"));

		BigDecimal belowZero = QuantileUtil.linearInterpolatedQuantile(sorted, -1.0d);
		BigDecimal aboveOne = QuantileUtil.linearInterpolatedQuantile(sorted, 2.0d);

		assertThat(belowZero).isEqualByComparingTo("1");
		assertThat(aboveOne).isEqualByComparingTo("3");
	}
}
