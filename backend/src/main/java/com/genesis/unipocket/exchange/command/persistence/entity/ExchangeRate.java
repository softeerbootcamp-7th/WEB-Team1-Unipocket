package com.genesis.unipocket.exchange.command.persistence.entity;

import com.genesis.unipocket.global.common.enums.CurrencyCode;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

// JPA 엔티티임을 선언한다.
@Entity
// 필드 기반 getter를 생성한다.
@Getter
// 빌더 패턴 생성 코드를 만든다.
@SuperBuilder
// JPA용 protected 기본 생성자를 만든다.
@NoArgsConstructor(access = AccessLevel.PROTECTED)
// 실제 DB 테이블명을 지정한다.
@Table(name = "exchange_rate")
// 일별 환율 저장 엔티티다.
public class ExchangeRate {

	// 기본키 필드임을 선언한다.
	@Id
	// PK는 DB IDENTITY 전략으로 자동 증가한다.
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	// 컬럼명/nullable 제약을 지정한다.
	@Column(nullable = false, name = "exchange_rate_id")
	Long id;

	// NOT NULL + CHAR(3) 통화 코드 컬럼으로 매핑한다.
	@Column(nullable = false, columnDefinition = "CHAR(3)")
	// enum 값을 문자열로 저장한다.
	@Enumerated(EnumType.STRING)
	CurrencyCode currencyCode;

	// 환율 효력 시각(일자 시작 시각)을 저장한다.
	@Column(nullable = false)
	LocalDateTime recordedAt;

	// USD 대비 대상 통화 환율 값을 저장한다.
	@Column(nullable = false)
	BigDecimal rate;
}
