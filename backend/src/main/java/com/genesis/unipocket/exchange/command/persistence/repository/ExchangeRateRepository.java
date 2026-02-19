package com.genesis.unipocket.exchange.command.persistence.repository;

import com.genesis.unipocket.exchange.command.persistence.entity.ExchangeRate;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {

	Optional<ExchangeRate>
			findTopByCurrencyCodeAndRecordedAtGreaterThanEqualAndRecordedAtLessThanOrderByRecordedAtDesc(
					CurrencyCode currencyCode,
					LocalDateTime startOfDay,
					LocalDateTime nextDayStart);
}
