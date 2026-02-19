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
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

/**
 * 백트래킹/야후 fallback/예외 매핑 동작을 검증하는 단위 테스트.
 */
@ExtendWith(MockitoExtension.class)
class ExchangeRateCommandServiceImplTest {

	@Mock private ExchangeRateRepository exchangeRateRepository;
	@Mock private ExchangeRateQueryService exchangeRateQueryService;
	@Mock private RestTemplate restTemplate;
	@Spy private ObjectMapper objectMapper = new ObjectMapper();

	@InjectMocks private ExchangeRateCommandServiceImpl exchangeRateCommandService;

	@Test
	@DisplayName("DB에서 과거 날짜 값을 찾으면 targetDate로 저장 후 반환")
	void resolveAndStoreUsdRelativeRate_usesBacktrackedDbRate() {
		// 준비
		LocalDate targetDate = LocalDate.of(2026, 2, 12);

		when(exchangeRateQueryService.findLatestRateInRange(
						eq(CurrencyCode.KRW), any(LocalDate.class), any(LocalDate.class)))
				.thenReturn(
						Optional.of(
								ExchangeRate.builder()
										.currencyCode(CurrencyCode.KRW)
										.recordedAt(LocalDateTime.of(2026, 2, 10, 0, 0))
										.rate(new BigDecimal("1300.00"))
										.build()));
		when(exchangeRateQueryService.findLatestRateInRange(
						eq(CurrencyCode.KRW), eq(targetDate), eq(targetDate)))
				.thenReturn(Optional.empty());
		when(exchangeRateRepository.save(any(ExchangeRate.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		// 실행
		BigDecimal rate =
				exchangeRateCommandService.resolveAndStoreUsdRelativeRate(
						CurrencyCode.KRW, targetDate);

		// 검증
		assertThat(rate).isEqualByComparingTo("1300.00");
		ArgumentCaptor<ExchangeRate> captor = ArgumentCaptor.forClass(ExchangeRate.class);
		verify(exchangeRateRepository, times(1)).save(captor.capture());
		assertThat(captor.getValue().getRecordedAt().toLocalDate()).isEqualTo(targetDate);
		verify(restTemplate, never()).getForObject(any(String.class), eq(String.class));
	}

	@Test
	@DisplayName("targetDate 환율이 DB에 이미 있으면 외부 호출/재저장 없이 반환")
	void resolveAndStoreUsdRelativeRate_targetExistsInDb_returnsWithoutFetch() {
		// 준비
		LocalDate targetDate = LocalDate.of(2026, 2, 12);
		ExchangeRate targetRate =
				ExchangeRate.builder()
						.currencyCode(CurrencyCode.KRW)
						.recordedAt(targetDate.atStartOfDay())
						.rate(new BigDecimal("1310.00"))
						.build();
		when(exchangeRateQueryService.findLatestRateInRange(
						eq(CurrencyCode.KRW), any(LocalDate.class), any(LocalDate.class)))
				.thenReturn(Optional.of(targetRate));

		// 실행
		BigDecimal rate =
				exchangeRateCommandService.resolveAndStoreUsdRelativeRate(
						CurrencyCode.KRW, targetDate);

		// 검증
		assertThat(rate).isEqualByComparingTo("1310.00");
		verify(restTemplate, never()).getForObject(any(String.class), eq(String.class));
		verify(exchangeRateRepository, never()).save(any(ExchangeRate.class));
	}

	@Test
	@DisplayName("DB에 없으면 Yahoo를 14일 범위로 1회 조회하고 찾은 날짜와 targetDate 모두 저장")
	void resolveAndStoreUsdRelativeRate_fetchesYahooByRangeAndStoresBothDates() {
		// 준비
		LocalDate targetDate = LocalDate.of(2026, 2, 12);

		when(exchangeRateQueryService.findLatestRateInRange(
						eq(CurrencyCode.GBP), any(LocalDate.class), any(LocalDate.class)))
				.thenReturn(Optional.empty());
		when(restTemplate.getForObject(any(String.class), eq(String.class)))
				.thenReturn(yahooSingleDayResponse(LocalDate.of(2026, 2, 11), "0.79"));
		when(exchangeRateRepository.save(any(ExchangeRate.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		// 실행
		BigDecimal rate =
				exchangeRateCommandService.resolveAndStoreUsdRelativeRate(
						CurrencyCode.GBP, targetDate);

		// 검증
		assertThat(rate).isEqualByComparingTo("0.79");
		ArgumentCaptor<ExchangeRate> captor = ArgumentCaptor.forClass(ExchangeRate.class);
		verify(exchangeRateRepository, times(2)).save(captor.capture());
		assertThat(captor.getAllValues())
				.extracting(e -> e.getRecordedAt().toLocalDate())
				.containsExactlyInAnyOrder(LocalDate.of(2026, 2, 11), LocalDate.of(2026, 2, 12));
		verify(restTemplate, times(1)).getForObject(any(String.class), eq(String.class));
	}

	@Test
	@DisplayName("Yahoo가 미지원 심볼을 반환하면 EXCHANGE_RATE_NOT_FOUND")
	void resolveAndStoreUsdRelativeRate_unsupportedSymbol_throwsNotFound() {
		// 준비
		LocalDate targetDate = LocalDate.of(2026, 2, 12);

		when(exchangeRateQueryService.findLatestRateInRange(
						eq(CurrencyCode.KRW), any(LocalDate.class), any(LocalDate.class)))
				.thenReturn(Optional.empty());
		when(restTemplate.getForObject(any(String.class), eq(String.class)))
				.thenReturn(yahooUnsupportedSymbolResponse());

		// 실행 + 검증
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
		// 준비
		LocalDate targetDate = LocalDate.of(2026, 2, 12);

		when(exchangeRateQueryService.findLatestRateInRange(
						eq(CurrencyCode.KRW), any(LocalDate.class), any(LocalDate.class)))
				.thenReturn(Optional.empty());
		when(restTemplate.getForObject(any(String.class), eq(String.class)))
				.thenThrow(new RuntimeException("timeout"));

		// 실행 + 검증
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
		// 준비
		LocalDate targetDate = LocalDate.of(2026, 2, 12);

		when(exchangeRateQueryService.findLatestRateInRange(
						eq(CurrencyCode.KRW), any(LocalDate.class), any(LocalDate.class)))
				.thenAnswer(
						invocation -> {
							LocalDate start = invocation.getArgument(1);
							LocalDate end = invocation.getArgument(2);
							if (start.equals(end)) {
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
		when(exchangeRateRepository.save(any(ExchangeRate.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		// 실행
		BigDecimal rate =
				exchangeRateCommandService.resolveAndStoreUsdRelativeRate(
						CurrencyCode.KRW, targetDate);

		// 검증
		assertThat(rate).isEqualByComparingTo("1305.00");
		verify(restTemplate, times(1)).getForObject(any(String.class), eq(String.class));
		verify(exchangeRateRepository, times(2)).save(any(ExchangeRate.class));
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
