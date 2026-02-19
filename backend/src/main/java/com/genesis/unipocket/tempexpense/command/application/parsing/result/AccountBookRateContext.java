package com.genesis.unipocket.tempexpense.command.application.parsing.result;

import com.genesis.unipocket.global.common.enums.CurrencyCode;

public record AccountBookRateContext(
		CurrencyCode baseCurrencyCode, CurrencyCode defaultLocalCurrencyCode) {}
