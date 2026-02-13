package com.genesis.unipocket.exchange.query.application.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.genesis.unipocket.exchange.command.application.ExchangeRateCommandService;
import com.genesis.unipocket.exchange.command.persistence.entity.ExchangeRate;
import com.genesis.unipocket.exchange.query.application.ExchangeRateQueryService;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExchangeRateServiceImplTest {

	@Mock private ExchangeRateQueryService exchangeRateQueryService;
	@Mock private ExchangeRateCommandService exchangeRateCommandService;

	@InjectMocks private ExchangeRateServiceImpl exchangeRateService;

	@Test
	@DisplayName("동일 통화면 1 반환")
	void getExchangeRate_sameCurrency_returnsOne() {
		BigDecimal rate =
				exchangeRateService.getExchangeRate(
						CurrencyCode.KRW, CurrencyCode.KRW, LocalDateTime.now());

		assertThat(rate).isEqualTo(BigDecimal.ONE);
		verifyNoInteractions(exchangeRateQueryService, exchangeRateCommandService);
	}

	@Test
	@DisplayName("조회 데이터가 있으면 query만 사용")
	void getExchangeRate_usesQueryWhenExists() {
		LocalDateTime dateTime = LocalDateTime.of(2026, 2, 12, 10, 0);
		when(exchangeRateQueryService.findRateOnDate(eq(CurrencyCode.KRW), any(LocalDate.class)))
				.thenReturn(Optional.of(rate(CurrencyCode.KRW, dateTime, "1300.00")));
		when(exchangeRateQueryService.findRateOnDate(eq(CurrencyCode.GBP), any(LocalDate.class)))
				.thenReturn(Optional.of(rate(CurrencyCode.GBP, dateTime, "0.79")));

		BigDecimal result =
				exchangeRateService.getExchangeRate(CurrencyCode.KRW, CurrencyCode.GBP, dateTime);

		assertThat(result)
				.isEqualByComparingTo(
						new BigDecimal("0.79")
								.divide(
										new BigDecimal("1300.00"),
										10,
										java.math.RoundingMode.HALF_UP));
		verify(exchangeRateCommandService, never())
				.resolveAndStoreUsdRelativeRate(any(CurrencyCode.class), any(LocalDate.class));
	}

	@Test
	@DisplayName("조회 데이터가 없으면 command로 보정 후 계산")
	void getExchangeRate_usesCommandWhenMissing() {
		LocalDateTime dateTime = LocalDateTime.of(2026, 2, 12, 10, 0);
		when(exchangeRateQueryService.findRateOnDate(eq(CurrencyCode.KRW), any(LocalDate.class)))
				.thenReturn(Optional.empty());
		when(exchangeRateQueryService.findRateOnDate(eq(CurrencyCode.GBP), any(LocalDate.class)))
				.thenReturn(Optional.empty());
		when(exchangeRateCommandService.resolveAndStoreUsdRelativeRate(
						eq(CurrencyCode.KRW), eq(dateTime.toLocalDate())))
				.thenReturn(new BigDecimal("1300.00"));
		when(exchangeRateCommandService.resolveAndStoreUsdRelativeRate(
						eq(CurrencyCode.GBP), eq(dateTime.toLocalDate())))
				.thenReturn(new BigDecimal("0.79"));

		BigDecimal result =
				exchangeRateService.getExchangeRate(CurrencyCode.KRW, CurrencyCode.GBP, dateTime);

		assertThat(result)
				.isEqualByComparingTo(
						new BigDecimal("0.79")
								.divide(
										new BigDecimal("1300.00"),
										10,
										java.math.RoundingMode.HALF_UP));
		verify(exchangeRateCommandService)
				.resolveAndStoreUsdRelativeRate(CurrencyCode.KRW, dateTime.toLocalDate());
		verify(exchangeRateCommandService)
				.resolveAndStoreUsdRelativeRate(CurrencyCode.GBP, dateTime.toLocalDate());
	}

	@Test
	@DisplayName("금액 0 이하면 예외")
	void convertAmount_invalidAmount_throwsException() {
		assertThatThrownBy(
						() ->
								exchangeRateService.convertAmount(
										BigDecimal.ZERO,
										CurrencyCode.KRW,
										CurrencyCode.USD,
										LocalDateTime.now()))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("code", ErrorCode.EXPENSE_INVALID_AMOUNT);
	}

	private ExchangeRate rate(CurrencyCode code, LocalDateTime recordedAt, String rate) {
		return ExchangeRate.builder()
				.currencyCode(code)
				.recordedAt(recordedAt)
				.rate(new BigDecimal(rate))
				.build();
	}
}
