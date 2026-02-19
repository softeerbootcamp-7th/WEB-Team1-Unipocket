package com.genesis.unipocket.exchange.query.application.impl;

import com.genesis.unipocket.exchange.command.application.ExchangeRateCommandService;
import com.genesis.unipocket.exchange.query.application.ExchangeRateQueryService;
import com.genesis.unipocket.exchange.query.application.ExchangeRateService;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

// 스프링 서비스 빈으로 등록한다.
@Service
// final 필드 생성자를 자동 생성한다.
@RequiredArgsConstructor
// 외부 도메인에서 사용하는 환율 계산 서비스 구현체다.
public class ExchangeRateServiceImpl implements ExchangeRateService {

	// 환율 계산 중간값 정밀도를 위한 scale 값이다.
	private static final int RATE_SCALE = 10;

	// 저장된 환율을 조회하는 read 서비스다.
	private final ExchangeRateQueryService exchangeRateQueryService;
	// 조회 실패 시 보정/저장을 수행하는 command 서비스다.
	private final ExchangeRateCommandService exchangeRateCommandService;

	// 통화쌍 환율을 계산해 반환한다.
	@Override
	public BigDecimal getExchangeRate(CurrencyCode from, CurrencyCode to, OffsetDateTime dateTime) {
		// 동일 통화면 환율은 1이다.
		if (from == to) {
			return BigDecimal.ONE;
		}

		// from 통화의 USD 상대 환율을 확보한다.
		BigDecimal fromUsdRelativeRate = getUsdRelativeRate(from, dateTime);
		// to 통화의 USD 상대 환율을 확보한다.
		BigDecimal toUsdRelativeRate = getUsdRelativeRate(to, dateTime);

		// 교차 환율(to / from)을 지정 정밀도로 계산한다.
		return toUsdRelativeRate.divide(fromUsdRelativeRate, RATE_SCALE, RoundingMode.HALF_UP);
	}

	// 금액을 from 통화에서 to 통화로 변환한다.
	@Override
	public BigDecimal convertAmount(
			BigDecimal amount, CurrencyCode from, CurrencyCode to, OffsetDateTime dateTime) {
		// null 또는 0 이하 금액은 도메인 예외로 처리한다.
		if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new BusinessException(ErrorCode.EXPENSE_INVALID_AMOUNT);
		}

		// 변환에 사용할 환율을 계산한다.
		BigDecimal rate = getExchangeRate(from, to, dateTime);
		// 최종 금액은 통화 표시 기준으로 소수 둘째 자리 반올림한다.
		return amount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
	}

	// 단일 통화의 USD 상대 환율을 조회하거나 필요 시 보정한다.
	private BigDecimal getUsdRelativeRate(CurrencyCode currencyCode, OffsetDateTime dateTime) {
		// USD 자체는 USD 상대 환율이 1이다.
		if (currencyCode == CurrencyCode.USD) {
			return BigDecimal.ONE;
		}

		// Yahoo 일봉 종가 기준을 맞추기 위해 기준 일자를 전일로 맞춘다.
		LocalDate targetDate = dateTime.toLocalDate().minusDays(1);
		// 먼저 DB에서 targetDate 환율을 조회한다.
		return exchangeRateQueryService
				.findLatestRateInRange(currencyCode, targetDate, targetDate)
				.map(rate -> rate.getRate())
				.orElseGet(
						// 없으면 command 서비스로 보정 조회 후 저장한 값을 사용한다.
						() ->
								exchangeRateCommandService.resolveAndStoreUsdRelativeRate(
										currencyCode, targetDate));
	}
}
