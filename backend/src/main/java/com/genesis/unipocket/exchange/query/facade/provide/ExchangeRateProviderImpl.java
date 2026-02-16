package com.genesis.unipocket.exchange.query.facade.provide;

import com.genesis.unipocket.exchange.query.application.ExchangeRateService;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.tempexpense.command.facade.port.ExchangeRateProvider;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * <b>Exchange 도메인 환율 조회 어댑터</b>
 */
@Component
@RequiredArgsConstructor
public class ExchangeRateProviderImpl implements ExchangeRateProvider {

	private final ExchangeRateService exchangeRateService;

	@Override
	public BigDecimal getExchangeRate(CurrencyCode from, CurrencyCode to, OffsetDateTime dateTime) {
		return exchangeRateService.getExchangeRate(from, to, dateTime);
	}
}
