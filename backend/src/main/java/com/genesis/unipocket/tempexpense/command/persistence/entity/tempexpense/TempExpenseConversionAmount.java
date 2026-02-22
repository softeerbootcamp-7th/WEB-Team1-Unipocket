package com.genesis.unipocket.tempexpense.command.persistence.entity.tempexpense;

import com.genesis.unipocket.global.common.enums.CurrencyCode;
import java.math.BigDecimal;

public record TempExpenseConversionAmount(
		CurrencyCode localCurrencyCode,
		CurrencyCode baseCurrencyCode,
		BigDecimal baseCurrencyAmount,
		BigDecimal calculatedBaseCurrencyAmount,
		BigDecimal exchangeRate) {}
