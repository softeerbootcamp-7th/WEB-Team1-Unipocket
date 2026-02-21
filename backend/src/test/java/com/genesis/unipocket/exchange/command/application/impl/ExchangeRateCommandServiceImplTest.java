package com.genesis.unipocket.exchange.command.application.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.genesis.unipocket.exchange.common.persistence.entity.ExchangeRate;
import com.genesis.unipocket.exchange.common.persistence.repository.ExchangeRateRepository;
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
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DeadlockLoserDataAccessException;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class ExchangeRateCommandServiceImplTest {

	@Mock private ExchangeRateRepository exchangeRateRepository;
	@Mock private RestTemplate restTemplate;
	@Spy private ObjectMapper objectMapper = new ObjectMapper();

	@InjectMocks private ExchangeRateCommandServiceImpl exchangeRateCommandService;

	@Test
	@DisplayName("DB에서 과거 날짜 값을 찾으면 targetDate로 upsert 후 반환")
	void resolveAndStoreUsdRelativeRate_usesBacktrackedDbRate() {
		LocalDate targetDate = LocalDate.of(2026, 2, 12);

		ExchangeRate backtrackedRate =
				ExchangeRate.builder()
						.currencyCode(CurrencyCode.KRW)
						.recordedAt(LocalDateTime.of(2026, 2, 10, 0, 0))
						.rate(new BigDecimal("1300.00"))
						.build();

		when(exchangeRateRepository
						.findTopByCurrencyCodeAndRecordedAtGreaterThanEqualAndRecordedAtLessThanOrderByRecordedAtDesc(
								eq(CurrencyCode.KRW),
								any(LocalDateTime.class),
								any(LocalDateTime.class)))
				.thenAnswer(
						invocation -> {
							LocalDateTime start = invocation.getArgument(1);
							LocalDateTime end = invocation.getArgument(2);
							boolean isSingleDayLookup = start.plusDays(1).equals(end);
							if (isSingleDayLookup && start.toLocalDate().equals(targetDate)) {
								return Optional.empty();
							}
							return Optional.of(backtrackedRate);
						});
		when(exchangeRateRepository.upsertRate(
						eq(CurrencyCode.KRW.name()),
						any(LocalDateTime.class),
						any(BigDecimal.class)))
				.thenReturn(1);

		BigDecimal rate =
				exchangeRateCommandService.resolveAndStoreUsdRelativeRate(
						CurrencyCode.KRW, targetDate);

		assertThat(rate).isEqualByComparingTo("1300.00");
		ArgumentCaptor<LocalDateTime> captor = ArgumentCaptor.forClass(LocalDateTime.class);
		verify(exchangeRateRepository, times(1))
				.upsertRate(
						eq(CurrencyCode.KRW.name()),
						captor.capture(),
						eq(new BigDecimal("1300.00")));
		assertThat(captor.getValue().toLocalDate()).isEqualTo(targetDate);
		verify(restTemplate, never()).getForObject(any(String.class), eq(String.class));
	}

	@Test
	@DisplayName("targetDate 환율이 DB에 이미 있으면 외부 호출/upsert 없이 반환")
	void resolveAndStoreUsdRelativeRate_targetExistsInDb_returnsWithoutFetch() {
		LocalDate targetDate = LocalDate.of(2026, 2, 12);
		ExchangeRate targetRate =
				ExchangeRate.builder()
						.currencyCode(CurrencyCode.KRW)
						.recordedAt(targetDate.atStartOfDay())
						.rate(new BigDecimal("1310.00"))
						.build();
		when(exchangeRateRepository
						.findTopByCurrencyCodeAndRecordedAtGreaterThanEqualAndRecordedAtLessThanOrderByRecordedAtDesc(
								eq(CurrencyCode.KRW),
								any(LocalDateTime.class),
								any(LocalDateTime.class)))
				.thenReturn(Optional.of(targetRate));

		BigDecimal rate =
				exchangeRateCommandService.resolveAndStoreUsdRelativeRate(
						CurrencyCode.KRW, targetDate);

		assertThat(rate).isEqualByComparingTo("1310.00");
		verify(restTemplate, never()).getForObject(any(String.class), eq(String.class));
		verify(exchangeRateRepository, never())
				.upsertRate(any(String.class), any(LocalDateTime.class), any(BigDecimal.class));
	}

	@Test
	@DisplayName("DB에 없으면 Yahoo를 14일 범위로 1회 조회하고 찾은 날짜와 targetDate 모두 upsert")
	void resolveAndStoreUsdRelativeRate_fetchesYahooByRangeAndStoresBothDates() {
		LocalDate targetDate = LocalDate.of(2026, 2, 12);

		when(exchangeRateRepository
						.findTopByCurrencyCodeAndRecordedAtGreaterThanEqualAndRecordedAtLessThanOrderByRecordedAtDesc(
								eq(CurrencyCode.GBP),
								any(LocalDateTime.class),
								any(LocalDateTime.class)))
				.thenReturn(Optional.empty());
		when(restTemplate.getForObject(any(String.class), eq(String.class)))
				.thenReturn(yahooSingleDayResponse(LocalDate.of(2026, 2, 11), "0.79"));
		when(exchangeRateRepository.upsertRate(
						eq(CurrencyCode.GBP.name()),
						any(LocalDateTime.class),
						any(BigDecimal.class)))
				.thenReturn(1);

		BigDecimal rate =
				exchangeRateCommandService.resolveAndStoreUsdRelativeRate(
						CurrencyCode.GBP, targetDate);

		assertThat(rate).isEqualByComparingTo("0.79");
		ArgumentCaptor<LocalDateTime> captor = ArgumentCaptor.forClass(LocalDateTime.class);
		verify(exchangeRateRepository, times(2))
				.upsertRate(
						eq(CurrencyCode.GBP.name()), captor.capture(), eq(new BigDecimal("0.79")));
		assertThat(captor.getAllValues())
				.extracting(LocalDateTime::toLocalDate)
				.containsExactlyInAnyOrder(LocalDate.of(2026, 2, 11), LocalDate.of(2026, 2, 12));
		verify(restTemplate, times(1)).getForObject(any(String.class), eq(String.class));
	}

	@Test
	@DisplayName("Yahoo가 미지원 심볼을 반환하면 EXCHANGE_RATE_NOT_FOUND")
	void resolveAndStoreUsdRelativeRate_unsupportedSymbol_throwsNotFound() {
		LocalDate targetDate = LocalDate.of(2026, 2, 12);

		when(exchangeRateRepository
						.findTopByCurrencyCodeAndRecordedAtGreaterThanEqualAndRecordedAtLessThanOrderByRecordedAtDesc(
								eq(CurrencyCode.KRW),
								any(LocalDateTime.class),
								any(LocalDateTime.class)))
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

		when(exchangeRateRepository
						.findTopByCurrencyCodeAndRecordedAtGreaterThanEqualAndRecordedAtLessThanOrderByRecordedAtDesc(
								eq(CurrencyCode.KRW),
								any(LocalDateTime.class),
								any(LocalDateTime.class)))
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

	@Test
	@DisplayName("DB 환율이 비정상이면 무시하고 Yahoo fallback으로 처리")
	void resolveAndStoreUsdRelativeRate_invalidDbRate_fallsBackToYahoo() {
		LocalDate targetDate = LocalDate.of(2026, 2, 12);

		when(exchangeRateRepository
						.findTopByCurrencyCodeAndRecordedAtGreaterThanEqualAndRecordedAtLessThanOrderByRecordedAtDesc(
								eq(CurrencyCode.KRW),
								any(LocalDateTime.class),
								any(LocalDateTime.class)))
				.thenAnswer(
						invocation -> {
							LocalDateTime start = invocation.getArgument(1);
							LocalDateTime end = invocation.getArgument(2);
							if (start.plusDays(1).equals(end)) {
								return Optional.empty();
							}
							return Optional.of(
									ExchangeRate.builder()
											.currencyCode(CurrencyCode.KRW)
											.recordedAt(LocalDateTime.of(2026, 2, 10, 0, 0))
											.rate(new BigDecimal("-1300.00"))
											.build());
						});
		when(restTemplate.getForObject(any(String.class), eq(String.class)))
				.thenReturn(yahooSingleDayResponse(LocalDate.of(2026, 2, 11), "1305.00"));
		when(exchangeRateRepository.upsertRate(
						eq(CurrencyCode.KRW.name()),
						any(LocalDateTime.class),
						any(BigDecimal.class)))
				.thenReturn(1);

		BigDecimal rate =
				exchangeRateCommandService.resolveAndStoreUsdRelativeRate(
						CurrencyCode.KRW, targetDate);

		assertThat(rate).isEqualByComparingTo("1305.00");
		verify(restTemplate, times(1)).getForObject(any(String.class), eq(String.class));
		verify(exchangeRateRepository, times(2))
				.upsertRate(
						eq(CurrencyCode.KRW.name()),
						any(LocalDateTime.class),
						eq(new BigDecimal("1305.00")));
	}

	@Test
	@DisplayName("Upsert 중 deadlock이 발생하면 재시도 후 성공")
	void resolveAndStoreUsdRelativeRate_upsertDeadlock_retriesAndSucceeds() {
		LocalDate targetDate = LocalDate.of(2026, 2, 12);
		ExchangeRate backtrackedRate =
				ExchangeRate.builder()
						.currencyCode(CurrencyCode.KRW)
						.recordedAt(LocalDateTime.of(2026, 2, 10, 0, 0))
						.rate(new BigDecimal("1300.00"))
						.build();

		when(exchangeRateRepository
						.findTopByCurrencyCodeAndRecordedAtGreaterThanEqualAndRecordedAtLessThanOrderByRecordedAtDesc(
								eq(CurrencyCode.KRW),
								any(LocalDateTime.class),
								any(LocalDateTime.class)))
				.thenReturn(Optional.of(backtrackedRate));
		when(exchangeRateRepository.upsertRate(
						eq(CurrencyCode.KRW.name()),
						any(LocalDateTime.class),
						any(BigDecimal.class)))
				.thenThrow(new DeadlockLoserDataAccessException("deadlock", new RuntimeException()))
				.thenReturn(1);

		BigDecimal rate =
				exchangeRateCommandService.resolveAndStoreUsdRelativeRate(
						CurrencyCode.KRW, targetDate);

		assertThat(rate).isEqualByComparingTo("1300.00");
		verify(exchangeRateRepository, times(2))
				.upsertRate(
						eq(CurrencyCode.KRW.name()),
						eq(targetDate.atStartOfDay()),
						eq(new BigDecimal("1300.00")));
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
