package com.genesis.unipocket.accountbook.command.application.port;

import com.genesis.unipocket.global.common.enums.CurrencyCode;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public interface AccountBookExchangeRateReader {

	BigDecimal getExchangeRate(
			CurrencyCode baseCurrencyCode, CurrencyCode localCurrencyCode, OffsetDateTime quotedAt);
}
