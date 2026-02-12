package com.genesis.unipocket.exchange.query.application;

import com.genesis.unipocket.exchange.command.persistence.entity.ExchangeRate;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import java.time.LocalDate;
import java.util.Optional;

public interface ExchangeRateQueryService {

	Optional<ExchangeRate> findRateOnDate(CurrencyCode currencyCode, LocalDate date);
}
