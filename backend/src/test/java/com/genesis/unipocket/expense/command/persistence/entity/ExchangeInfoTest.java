package com.genesis.unipocket.expense.command.persistence.entity;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.genesis.unipocket.global.common.enums.CurrencyCode;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ExchangeInfoTest {

	@Test
	@DisplayName("localCurrencyAmount가 0이면 예외가 발생한다")
	void of_throwsWhenLocalAmountIsZero() {
		assertThatThrownBy(
						() ->
								ExchangeInfo.of(
										CurrencyCode.KRW,
										CurrencyCode.KRW,
										BigDecimal.ZERO,
										new BigDecimal("1000.00"),
										null,
										null,
										BigDecimal.ONE))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("localCurrencyAmount must be greater than 0");
	}

	@Test
	@DisplayName("baseCurrencyAmount가 0이면 예외가 발생한다")
	void of_throwsWhenBaseAmountIsZero() {
		assertThatThrownBy(
						() ->
								ExchangeInfo.of(
										CurrencyCode.KRW,
										CurrencyCode.USD,
										new BigDecimal("1000.00"),
										BigDecimal.ZERO,
										null,
										null,
										new BigDecimal("0.0007")))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("baseCurrencyAmount must be greater than 0");
	}

	@Test
	@DisplayName("calculatedBaseCurrencyAmount가 0이면 예외가 발생한다")
	void of_throwsWhenCalculatedAmountIsZero() {
		assertThatThrownBy(
						() ->
								ExchangeInfo.of(
										CurrencyCode.KRW,
										CurrencyCode.USD,
										new BigDecimal("1000.00"),
										null,
										BigDecimal.ZERO,
										CurrencyCode.USD,
										new BigDecimal("0.0007")))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("calculatedBaseCurrencyAmount must be greater than 0");
	}
}
