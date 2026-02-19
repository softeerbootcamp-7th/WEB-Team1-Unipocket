package com.genesis.unipocket.exchange.query.application;

import com.genesis.unipocket.global.common.enums.CurrencyCode;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

// 외부 도메인에서 사용하는 환율 서비스 계약이다.
public interface ExchangeRateService {

	// from 1단위가 to 통화에서 얼마인지 환율을 반환한다.
	BigDecimal getExchangeRate(CurrencyCode from, CurrencyCode to, OffsetDateTime dateTime);

	// 금액을 from 통화에서 to 통화로 환산해 반환한다.
	BigDecimal convertAmount(
			BigDecimal amount, CurrencyCode from, CurrencyCode to, OffsetDateTime dateTime);
}
