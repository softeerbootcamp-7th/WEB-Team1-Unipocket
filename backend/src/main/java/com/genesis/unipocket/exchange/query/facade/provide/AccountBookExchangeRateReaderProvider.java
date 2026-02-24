package com.genesis.unipocket.exchange.query.facade.provide;

import com.genesis.unipocket.accountbook.query.service.port.ExchangeRateReader;
import com.genesis.unipocket.exchange.common.service.ExchangeRateService;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountBookExchangeRateReaderProvider implements ExchangeRateReader {

	private final ExchangeRateService exchangeRateService;

	@Override
	public BigDecimal getExchangeRate(
			CurrencyCode baseCurrencyCode,
			CurrencyCode localCurrencyCode,
			OffsetDateTime quotedAt) {
		return exchangeRateService.getExchangeRate(baseCurrencyCode, localCurrencyCode, quotedAt);
	}
}
