package com.genesis.unipocket.analysis.query.application;

import java.math.BigDecimal;
import java.math.RoundingMode;

final class AnalysisMoneyFormatter {

	private static final int MONEY_DISPLAY_SCALE = 2;

	private AnalysisMoneyFormatter() {}

	static String formatRoundedMoney(BigDecimal amount) {
		if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) {
			return "0";
		}
		BigDecimal rounded = amount.setScale(MONEY_DISPLAY_SCALE, RoundingMode.HALF_UP);
		if (rounded.stripTrailingZeros().scale() <= 0) {
			return rounded.toBigInteger().toString();
		}
		return rounded.toPlainString();
	}
}
