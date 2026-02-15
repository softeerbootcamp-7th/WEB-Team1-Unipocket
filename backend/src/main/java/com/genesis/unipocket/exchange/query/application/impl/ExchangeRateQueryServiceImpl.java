package com.genesis.unipocket.exchange.query.application.impl;

import com.genesis.unipocket.exchange.command.persistence.entity.ExchangeRate;
import com.genesis.unipocket.exchange.command.persistence.repository.ExchangeRateRepository;
import com.genesis.unipocket.exchange.query.application.ExchangeRateQueryService;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExchangeRateQueryServiceImpl implements ExchangeRateQueryService {

	private final ExchangeRateRepository exchangeRateRepository;

	@Override
	public Optional<ExchangeRate> findRateOnDate(CurrencyCode currencyCode, LocalDate date) {
		LocalDateTime startOfDay = date.atStartOfDay();
		LocalDateTime nextDayStart = date.plusDays(1).atStartOfDay();
		return exchangeRateRepository
				.findTopByCurrencyCodeAndRecordedAtGreaterThanEqualAndRecordedAtLessThanOrderByRecordedAtDesc(
						currencyCode, startOfDay, nextDayStart);
	}
}
