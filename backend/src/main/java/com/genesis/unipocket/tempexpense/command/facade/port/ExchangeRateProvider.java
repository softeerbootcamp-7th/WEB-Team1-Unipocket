package com.genesis.unipocket.tempexpense.command.facade.port;

import com.genesis.unipocket.global.common.enums.CurrencyCode;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * <b>환율 조회 포트</b>
 */
public interface ExchangeRateProvider {
	BigDecimal getExchangeRate(CurrencyCode from, CurrencyCode to, OffsetDateTime dateTime);
}
