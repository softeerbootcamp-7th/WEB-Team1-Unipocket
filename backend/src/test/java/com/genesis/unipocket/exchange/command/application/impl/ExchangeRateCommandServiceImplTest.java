package com.genesis.unipocket.exchange.command.application.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.genesis.unipocket.exchange.command.persistence.entity.ExchangeRate;
import com.genesis.unipocket.exchange.command.persistence.repository.ExchangeRateRepository;
import com.genesis.unipocket.exchange.query.application.ExchangeRateQueryService;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class ExchangeRateCommandServiceImplTest {

	@Mock private ExchangeRateRepository exchangeRateRepository;
	@Mock private ExchangeRateQueryService exchangeRateQueryService;
	@Mock private RestTemplate restTemplate;

	@InjectMocks private ExchangeRateCommandServiceImpl exchangeRateCommandService;

	@Test
	@DisplayName("DB에서 과거 날짜 값을 찾으면 targetDate로 저장 후 반환")
	void resolveAndStoreUsdRelativeRate_usesBacktrackedDbRate() {
		LocalDate targetDate = LocalDate.of(2026, 2, 12);

		when(exchangeRateQueryService.findRateOnDate(any(CurrencyCode.class), any(LocalDate.class)))
				.thenAnswer(
						invocation -> {
							LocalDate date = invocation.getArgument(1);
							if (date.equals(LocalDate.of(2026, 2, 10))) {
								return Optional.of(
										ExchangeRate.builder()
												.currencyCode(CurrencyCode.KRW)
												.recordedAt(LocalDateTime.of(2026, 2, 10, 0, 0))
												.rate(new BigDecimal("1300.00"))
												.build());
							}
							return Optional.empty();
						});
		when(restTemplate.getForObject(any(String.class), eq(String.class)))
				.thenReturn(yahooEmptyResponse(), yahooEmptyResponse());
		when(exchangeRateRepository.save(any(ExchangeRate.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		BigDecimal rate =
				exchangeRateCommandService.resolveAndStoreUsdRelativeRate(
						CurrencyCode.KRW, targetDate);

		assertThat(rate).isEqualByComparingTo("1300.00");
		ArgumentCaptor<ExchangeRate> captor = ArgumentCaptor.forClass(ExchangeRate.class);
		verify(exchangeRateRepository, times(1)).save(captor.capture());
		assertThat(captor.getValue().getRecordedAt().toLocalDate()).isEqualTo(targetDate);
	}

	@Test
	@DisplayName("DB에 없으면 Yahoo를 하루씩 조회해 찾은 날짜와 targetDate 모두 저장")
	void resolveAndStoreUsdRelativeRate_fetchesYahooByDayAndStoresBothDates() {
		LocalDate targetDate = LocalDate.of(2026, 2, 12);

		when(exchangeRateQueryService.findRateOnDate(eq(CurrencyCode.GBP), any(LocalDate.class)))
				.thenReturn(Optional.empty());
		when(restTemplate.getForObject(any(String.class), eq(String.class)))
				.thenReturn(
						yahooEmptyResponse(),
						yahooSingleDayResponse(LocalDate.of(2026, 2, 11), "0.79"));
		when(exchangeRateRepository.save(any(ExchangeRate.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		BigDecimal rate =
				exchangeRateCommandService.resolveAndStoreUsdRelativeRate(
						CurrencyCode.GBP, targetDate);

		assertThat(rate).isEqualByComparingTo("0.79");
		ArgumentCaptor<ExchangeRate> captor = ArgumentCaptor.forClass(ExchangeRate.class);
		verify(exchangeRateRepository, times(2)).save(captor.capture());
		assertThat(captor.getAllValues())
				.extracting(e -> e.getRecordedAt().toLocalDate())
				.containsExactlyInAnyOrder(LocalDate.of(2026, 2, 11), LocalDate.of(2026, 2, 12));
	}

	@Test
	@DisplayName("Yahoo가 미지원 심볼을 반환하면 EXCHANGE_RATE_NOT_FOUND")
	void resolveAndStoreUsdRelativeRate_unsupportedSymbol_throwsNotFound() {
		LocalDate targetDate = LocalDate.of(2026, 2, 12);

		when(exchangeRateQueryService.findRateOnDate(eq(CurrencyCode.KRW), any(LocalDate.class)))
				.thenReturn(Optional.empty());
		when(restTemplate.getForObject(any(String.class), eq(String.class)))
				.thenReturn(yahooUnsupportedSymbolResponse());

		assertThatThrownBy(
						() ->
								exchangeRateCommandService.resolveAndStoreUsdRelativeRate(
										CurrencyCode.KRW, targetDate))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("code", ErrorCode.EXCHANGE_RATE_NOT_FOUND);
	}

	@Test
	@DisplayName("Yahoo 통신 실패면 EXCHANGE_RATE_API_ERROR")
	void resolveAndStoreUsdRelativeRate_yahooFailure_throwsApiError() {
		LocalDate targetDate = LocalDate.of(2026, 2, 12);

		when(exchangeRateQueryService.findRateOnDate(eq(CurrencyCode.KRW), any(LocalDate.class)))
				.thenReturn(Optional.empty());
		when(restTemplate.getForObject(any(String.class), eq(String.class)))
				.thenThrow(new RuntimeException("timeout"));

		assertThatThrownBy(
						() ->
								exchangeRateCommandService.resolveAndStoreUsdRelativeRate(
										CurrencyCode.KRW, targetDate))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("code", ErrorCode.EXCHANGE_RATE_API_ERROR);
	}

	private String yahooEmptyResponse() {
		return """
				{
					"chart": {
						"result": [],
						"error": null
					}
				}
				""";
	}

	private String yahooSingleDayResponse(LocalDate date, String close) {
		long timestamp = date.atStartOfDay(ZoneOffset.UTC).toEpochSecond();
		return """
				{
					"chart": {
						"result": [
							{
								"timestamp": [%d],
								"indicators": { "quote": [ { "close": [%s] } ] }
							}
						],
						"error": null
					}
				}
				"""
				.formatted(timestamp, close);
	}

	private String yahooUnsupportedSymbolResponse() {
		return """
				{
					"chart": {
						"result": null,
						"error": {
							"code": "Not Found",
							"description": "No data found, symbol may be delisted"
						}
					}
				}
				""";
	}
}
