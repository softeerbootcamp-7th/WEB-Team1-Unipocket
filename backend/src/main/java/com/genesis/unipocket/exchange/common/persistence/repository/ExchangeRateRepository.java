package com.genesis.unipocket.exchange.common.persistence.repository;

import com.genesis.unipocket.exchange.common.persistence.entity.ExchangeRate;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {

	Optional<ExchangeRate>
			findTopByCurrencyCodeAndRecordedAtGreaterThanEqualAndRecordedAtLessThanOrderByRecordedAtDesc(
					CurrencyCode currencyCode,
					LocalDateTime startOfDay,
					LocalDateTime nextDayStart);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query(
			value =
					"""
					INSERT INTO exchange_rate(currency_code, recorded_at, rate)
					VALUES (:currencyCode, :recordedAt, :rate)
					ON DUPLICATE KEY UPDATE rate = VALUES(rate)
					""",
			nativeQuery = true)
	int upsertRate(
			@Param("currencyCode") String currencyCode,
			@Param("recordedAt") LocalDateTime recordedAt,
			@Param("rate") BigDecimal rate);
}
