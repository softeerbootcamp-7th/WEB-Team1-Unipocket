package com.genesis.unipocket.tempexpense.command.application.dto;

import java.math.BigDecimal;

public record TemporaryExpenseExchangeComputation(
		BigDecimal calculatedBaseAmount, BigDecimal exchangeRate) {

	public static TemporaryExpenseExchangeComputation empty() {
		return new TemporaryExpenseExchangeComputation(null, null);
	}
}
