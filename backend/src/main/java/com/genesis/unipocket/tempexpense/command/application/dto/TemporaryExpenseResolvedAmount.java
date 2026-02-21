package com.genesis.unipocket.tempexpense.command.application.dto;

import com.genesis.unipocket.global.common.enums.CurrencyCode;
import java.math.BigDecimal;

public record TemporaryExpenseResolvedAmount(
		BigDecimal baseCurrencyAmount,
		BigDecimal calculatedBaseCurrencyAmount,
		CurrencyCode calculatedBaseCurrencyCode,
		BigDecimal exchangeRate) {}
