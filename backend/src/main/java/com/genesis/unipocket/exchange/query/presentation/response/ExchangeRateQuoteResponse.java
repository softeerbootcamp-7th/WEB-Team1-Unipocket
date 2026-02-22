package com.genesis.unipocket.exchange.query.presentation.response;

import com.genesis.unipocket.global.common.enums.CurrencyCode;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ExchangeRateQuoteResponse(
		OffsetDateTime occurredAt,
		CurrencyCode baseCurrencyCode,
		CurrencyCode localCurrencyCode,
		BigDecimal exchangeRate) {}
