package com.genesis.unipocket.accountbook.query.service.port;

import com.genesis.unipocket.global.common.enums.CurrencyCode;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public interface ExchangeRateReader {

	BigDecimal getExchangeRate(
			CurrencyCode baseCurrencyCode, CurrencyCode localCurrencyCode, OffsetDateTime quotedAt);
}
