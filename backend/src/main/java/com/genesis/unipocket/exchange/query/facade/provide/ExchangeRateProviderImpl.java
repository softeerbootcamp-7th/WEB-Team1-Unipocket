package com.genesis.unipocket.exchange.query.facade.provide;

import com.genesis.unipocket.exchange.query.application.ExchangeRateService;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.tempexpense.command.facade.port.ExchangeRateProvider;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

// 스프링 컴포넌트로 등록한다.
@Component
// final 필드 생성자를 자동 생성한다.
@RequiredArgsConstructor
// temp-expense 포트에 exchange 도메인을 연결하는 어댑터다.
public class ExchangeRateProviderImpl implements ExchangeRateProvider {

	// 실제 환율 계산을 수행하는 서비스다.
	private final ExchangeRateService exchangeRateService;

	// 포트 메서드 호출을 exchange 서비스로 위임한다.
	@Override
	public BigDecimal getExchangeRate(CurrencyCode from, CurrencyCode to, OffsetDateTime dateTime) {
		return exchangeRateService.getExchangeRate(from, to, dateTime);
	}
}
