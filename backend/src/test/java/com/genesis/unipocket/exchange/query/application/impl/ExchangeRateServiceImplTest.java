package com.genesis.unipocket.exchange.query.application.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * query 우선 조회/fallback 동작과 금액 변환 검증 로직을 테스트한다.
 */
@ExtendWith(MockitoExtension.class)
class ExchangeRateServiceImplTest {

	@Mock private ExchangeRateQueryService exchangeRateQueryService;
	@Mock private ExchangeRateCommandService exchangeRateCommandService;

	@InjectMocks private ExchangeRateServiceImpl exchangeRateService;

	@Test
	@DisplayName("동일 통화면 1 반환")
	void getExchangeRate_sameCurrency_returnsOne() {
		// 실행
		BigDecimal rate =
				exchangeRateService.getExchangeRate(
						CurrencyCode.KRW, CurrencyCode.KRW, OffsetDateTime.now());

		// 검증
		assertThat(rate).isEqualTo(BigDecimal.ONE);
		verifyNoInteractions(exchangeRateQueryService, exchangeRateCommandService);
	}

	@Test
	@DisplayName("조회 데이터가 있으면 query만 사용")
	void getExchangeRate_usesQueryWhenExists() {
		// 준비
		OffsetDateTime dateTime = OffsetDateTime.of(2026, 2, 12, 10, 0, 0, 0, ZoneOffset.UTC);
		LocalDate targetDate = dateTime.toLocalDate().minusDays(1);
		when(exchangeRateQueryService.findLatestRateInRange(
						eq(CurrencyCode.KRW), eq(targetDate), eq(targetDate)))
				.thenReturn(
						Optional.of(rate(CurrencyCode.KRW, dateTime.toLocalDateTime(), "1300.00")));
		when(exchangeRateQueryService.findLatestRateInRange(
						eq(CurrencyCode.GBP), eq(targetDate), eq(targetDate)))
				.thenReturn(
						Optional.of(rate(CurrencyCode.GBP, dateTime.toLocalDateTime(), "0.79")));

		// 실행
		BigDecimal result =
				exchangeRateService.getExchangeRate(CurrencyCode.KRW, CurrencyCode.GBP, dateTime);

		// 검증
		assertThat(result)
				.isEqualByComparingTo(
						new BigDecimal("0.79")
								.divide(
										new BigDecimal("1300.00"),
										10,
										java.math.RoundingMode.HALF_UP));
		verify(exchangeRateCommandService, never())
				.resolveAndStoreUsdRelativeRate(any(CurrencyCode.class), any(LocalDate.class));
		verify(exchangeRateQueryService, times(1))
				.findLatestRateInRange(CurrencyCode.KRW, targetDate, targetDate);
		verify(exchangeRateQueryService, times(1))
				.findLatestRateInRange(CurrencyCode.GBP, targetDate, targetDate);
	}

	@Test
	@DisplayName("조회 데이터가 없으면 command로 보정 후 계산")
	void getExchangeRate_usesCommandWhenMissing() {
		// 준비
		OffsetDateTime dateTime = OffsetDateTime.of(2026, 2, 12, 10, 0, 0, 0, ZoneOffset.UTC);
		LocalDate targetDate = dateTime.toLocalDate().minusDays(1);
		when(exchangeRateQueryService.findLatestRateInRange(
						eq(CurrencyCode.KRW), eq(targetDate), eq(targetDate)))
				.thenReturn(Optional.empty());
		when(exchangeRateQueryService.findLatestRateInRange(
						eq(CurrencyCode.GBP), eq(targetDate), eq(targetDate)))
				.thenReturn(Optional.empty());
		when(exchangeRateCommandService.resolveAndStoreUsdRelativeRate(
						eq(CurrencyCode.KRW), eq(targetDate)))
				.thenReturn(new BigDecimal("1300.00"));
		when(exchangeRateCommandService.resolveAndStoreUsdRelativeRate(
						eq(CurrencyCode.GBP), eq(targetDate)))
				.thenReturn(new BigDecimal("0.79"));

		// 실행
		BigDecimal result =
				exchangeRateService.getExchangeRate(CurrencyCode.KRW, CurrencyCode.GBP, dateTime);

		// 검증
		assertThat(result)
				.isEqualByComparingTo(
						new BigDecimal("0.79")
								.divide(
										new BigDecimal("1300.00"),
										10,
										java.math.RoundingMode.HALF_UP));
		verify(exchangeRateCommandService)
				.resolveAndStoreUsdRelativeRate(CurrencyCode.KRW, targetDate);
		verify(exchangeRateCommandService)
				.resolveAndStoreUsdRelativeRate(CurrencyCode.GBP, targetDate);
	}

	@Test
	@DisplayName("금액 0 이하면 예외")
	void convertAmount_invalidAmount_throwsException() {
		// 실행 + 검증
		assertThatThrownBy(
						() ->
								exchangeRateService.convertAmount(
										BigDecimal.ZERO,
										CurrencyCode.KRW,
										CurrencyCode.USD,
										OffsetDateTime.now()))
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
