package com.genesis.unipocket.exchange.query.application;

import com.genesis.unipocket.exchange.command.persistence.entity.ExchangeRate;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import java.time.LocalDate;
import java.util.Optional;

// 환율 조회(Read) 계약 인터페이스다.
public interface ExchangeRateQueryService {

	// 통화와 날짜 범위(양끝 포함)에서 최신 환율 1건을 조회한다.
	Optional<ExchangeRate> findLatestRateInRange(
			CurrencyCode currencyCode, LocalDate startDate, LocalDate endDate);
}
