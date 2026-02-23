package com.genesis.unipocket.tempexpense.command.facade.port.dto;

import com.genesis.unipocket.global.common.enums.CountryCode;
import com.genesis.unipocket.global.common.enums.CurrencyCode;

public record AccountBookRateInfo(
		CurrencyCode baseCurrencyCode,
		CurrencyCode localCurrencyCode,
		CountryCode localCountryCode) {}
