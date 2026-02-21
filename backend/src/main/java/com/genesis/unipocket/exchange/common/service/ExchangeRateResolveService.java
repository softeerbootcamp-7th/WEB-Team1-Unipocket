package com.genesis.unipocket.exchange.common.service;

import com.genesis.unipocket.global.common.enums.CurrencyCode;
import java.math.BigDecimal;
import java.time.LocalDate;

public interface ExchangeRateResolveService {

	BigDecimal resolveAndStoreUsdRelativeRate(CurrencyCode currencyCode, LocalDate targetDate);
}
