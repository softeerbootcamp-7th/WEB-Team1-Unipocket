package com.genesis.unipocket.expense.presentation.support;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class AmountFormatters {

	private AmountFormatters() {}

	public static String toAmountString(BigDecimal amount) {
		if (amount == null) {
			return null;
		}
		return amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
	}
}
