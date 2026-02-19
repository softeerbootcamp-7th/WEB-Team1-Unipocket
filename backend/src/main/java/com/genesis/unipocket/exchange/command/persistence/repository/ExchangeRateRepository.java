package com.genesis.unipocket.exchange.command.persistence.repository;

import com.genesis.unipocket.exchange.command.persistence.entity.ExchangeRate;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// 스프링 Data Repository 빈으로 등록한다.
@Repository
// ExchangeRate 엔티티용 JPA 저장소 인터페이스다.
public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {

	// 통화 + 시각 구간 조건으로 최신 환율 1건을 조회한다.
	Optional<ExchangeRate>
			findTopByCurrencyCodeAndRecordedAtGreaterThanEqualAndRecordedAtLessThanOrderByRecordedAtDesc(
					CurrencyCode currencyCode,
					LocalDateTime startOfDay,
					LocalDateTime nextDayStart);
}
