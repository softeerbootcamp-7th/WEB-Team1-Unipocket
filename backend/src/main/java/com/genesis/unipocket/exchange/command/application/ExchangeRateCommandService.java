package com.genesis.unipocket.exchange.command.application;

import com.genesis.unipocket.global.common.enums.CurrencyCode;
import java.math.BigDecimal;
import java.time.LocalDate;

// Command 계층에서 환율 보정/저장을 수행하는 계약 인터페이스다.
public interface ExchangeRateCommandService {

	// 지정 통화의 targetDate 환율을 보정 탐색하고, 필요하면 저장한 뒤 결과를 반환한다.
	BigDecimal resolveAndStoreUsdRelativeRate(CurrencyCode currencyCode, LocalDate targetDate);
}
