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

// 스프링 서비스 빈으로 등록한다.
@Service
// final 필드 생성자를 자동 생성한다.
@RequiredArgsConstructor
// 조회 전용 트랜잭션으로 실행한다.
@Transactional(readOnly = true)
// 환율 조회 계약의 JPA 구현체다.
public class ExchangeRateQueryServiceImpl implements ExchangeRateQueryService {

	// 실제 DB 조회를 수행하는 저장소다.
	private final ExchangeRateRepository exchangeRateRepository;

	// 날짜 범위 내 최신 환율을 조회한다.
	@Override
	public Optional<ExchangeRate> findLatestRateInRange(
			CurrencyCode currencyCode, LocalDate startDate, LocalDate endDate) {
		// 시작 날짜를 당일 00:00:00으로 변환한다.
		LocalDateTime startOfRange = startDate.atStartOfDay();
		// 종료 날짜의 다음날 00:00:00을 배타 상한으로 계산한다.
		LocalDateTime endExclusive = endDate.plusDays(1).atStartOfDay();
		// 저장소에서 조건에 맞는 최신 1건을 조회해 반환한다.
		return exchangeRateRepository
				.findTopByCurrencyCodeAndRecordedAtGreaterThanEqualAndRecordedAtLessThanOrderByRecordedAtDesc(
						currencyCode, startOfRange, endExclusive);
	}
}
