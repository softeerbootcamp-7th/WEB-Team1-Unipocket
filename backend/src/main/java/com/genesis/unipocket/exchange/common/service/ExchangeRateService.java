package com.genesis.unipocket.exchange.common.service;

import com.genesis.unipocket.global.common.enums.CurrencyCode;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public interface ExchangeRateService {

	BigDecimal getExchangeRate(CurrencyCode from, CurrencyCode to, OffsetDateTime dateTime);

	BigDecimal convertAmount(
			BigDecimal amount, CurrencyCode from, CurrencyCode to, OffsetDateTime dateTime);
}
