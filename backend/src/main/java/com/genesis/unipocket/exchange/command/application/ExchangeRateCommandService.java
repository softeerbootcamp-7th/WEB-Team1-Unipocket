package com.genesis.unipocket.exchange.command.application;

import com.genesis.unipocket.global.common.enums.CurrencyCode;
import java.math.BigDecimal;
import java.time.LocalDate;

public interface ExchangeRateCommandService {

	BigDecimal resolveAndStoreUsdRelativeRate(CurrencyCode currencyCode, LocalDate targetDate);
}
