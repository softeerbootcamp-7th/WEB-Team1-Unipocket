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
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * <b>환율 서비스 구현체</b>
 * <p>USD 기준 일별 환율 데이터를 사용해 통화 간 환산
 *
 * @author bluefishez
 * @since 2026-02-07
 */
@Service
@RequiredArgsConstructor
public class ExchangeRateServiceImpl implements ExchangeRateService {

	private static final int RATE_SCALE = 10;

	private final ExchangeRateQueryService exchangeRateQueryService;
	private final ExchangeRateCommandService exchangeRateCommandService;

	@Override
	public BigDecimal getExchangeRate(CurrencyCode from, CurrencyCode to, LocalDateTime dateTime) {
		if (from == to) {
			return BigDecimal.ONE;
		}

		BigDecimal fromUsdRelativeRate = getUsdRelativeRate(from, dateTime);
		BigDecimal toUsdRelativeRate = getUsdRelativeRate(to, dateTime);

		return toUsdRelativeRate.divide(fromUsdRelativeRate, RATE_SCALE, RoundingMode.HALF_UP);
	}

	@Override
	public BigDecimal convertAmount(
			BigDecimal amount, CurrencyCode from, CurrencyCode to, LocalDateTime dateTime) {
		if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new BusinessException(ErrorCode.EXPENSE_INVALID_AMOUNT);
		}

		BigDecimal rate = getExchangeRate(from, to, dateTime);
		return amount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
	}

	private BigDecimal getUsdRelativeRate(CurrencyCode currencyCode, LocalDateTime dateTime) {
		if (currencyCode == CurrencyCode.USD) {
			return BigDecimal.ONE;
		}

		LocalDate targetDate = dateTime.toLocalDate();
		return exchangeRateQueryService
				.findRateOnDate(currencyCode, targetDate)
				.map(rate -> rate.getRate())
				.orElseGet(
						() ->
								exchangeRateCommandService.resolveAndStoreUsdRelativeRate(
										currencyCode, targetDate));
	}
}
