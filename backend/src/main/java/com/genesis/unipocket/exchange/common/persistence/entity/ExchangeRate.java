package com.genesis.unipocket.exchange.common.persistence.entity;

import com.genesis.unipocket.global.common.enums.CurrencyCode;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
		name = "exchange_rate",
		uniqueConstraints = {
			@UniqueConstraint(
					name = "uk_exchange_rate_currency_recorded_at",
					columnNames = {"currency_code", "recorded_at"})
		})
public class ExchangeRate {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(nullable = false, name = "exchange_rate_id")
	Long id;

	@Column(nullable = false, columnDefinition = "CHAR(3)")
	@Enumerated(EnumType.STRING)
	CurrencyCode currencyCode;

	@Column(nullable = false)
	LocalDateTime recordedAt;

	@Column(nullable = false)
	BigDecimal rate;
}
