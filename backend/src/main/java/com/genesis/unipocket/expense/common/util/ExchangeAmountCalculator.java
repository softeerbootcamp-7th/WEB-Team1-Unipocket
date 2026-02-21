package com.genesis.unipocket.expense.common.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class ExchangeAmountCalculator {

	private static final int AMOUNT_SCALE = 2;
	private static final int RATE_SCALE = 4;

	private ExchangeAmountCalculator() {}

	public static BigDecimal calculateBaseAmount(BigDecimal localAmount, BigDecimal exchangeRate) {
		return localAmount.multiply(exchangeRate).setScale(AMOUNT_SCALE, RoundingMode.HALF_UP);
	}

	public static BigDecimal calculateLocalAmount(BigDecimal baseAmount, BigDecimal exchangeRate) {
		return baseAmount.divide(exchangeRate, AMOUNT_SCALE, RoundingMode.HALF_UP);
	}

	public static BigDecimal deriveExchangeRate(BigDecimal baseAmount, BigDecimal localAmount) {
		return baseAmount.divide(localAmount, RATE_SCALE, RoundingMode.HALF_UP);
	}

	public static BigDecimal scaleAmount(BigDecimal amount) {
		return amount.setScale(AMOUNT_SCALE, RoundingMode.HALF_UP);
	}
}
