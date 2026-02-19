package com.genesis.unipocket.exchange.command.application.impl;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.genesis.unipocket.TestcontainersConfiguration;
import com.genesis.unipocket.exchange.command.application.ExchangeRateCommandService;
import com.genesis.unipocket.exchange.command.persistence.repository.ExchangeRateRepository;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;

/**
 * Yahoo 엔드포인트를 WireMock으로 대체해 통신/파싱 실패 처리를 검증하는 통합 테스트.
 */
@SpringBootTest
@ActiveProfiles("test-it")
@Import(TestcontainersConfiguration.class)
@Transactional
@Tag("integration")
class ExchangeRateCommandServiceWireMockIntegrationTest {

	@RegisterExtension
	static WireMockExtension wireMock =
			WireMockExtension.newInstance()
					.options(
							com.github.tomakehurst.wiremock.core.WireMockConfiguration
									.wireMockConfig()
									.dynamicPort())
					.build();

	@DynamicPropertySource
	static void registerProperties(DynamicPropertyRegistry registry) {
		registry.add(
				"exchange.yahoo.chart-url",
				() -> wireMock.getRuntimeInfo().getHttpBaseUrl() + "/v8/finance/chart/{symbol}");
		registry.add("external.http.default-connect-timeout-seconds", () -> 1);
		registry.add("external.http.default-read-timeout-seconds", () -> 1);
	}

	@Autowired private ExchangeRateCommandService exchangeRateCommandService;
	@Autowired private ExchangeRateRepository exchangeRateRepository;

	@Test
	void resolveAndStoreUsdRelativeRate_validYahooPayload_returnsRateAndPersists() {
		// 준비
		LocalDate targetDate = LocalDate.of(2026, 2, 12);
		long epoch = targetDate.atStartOfDay(ZoneOffset.UTC).toEpochSecond();

		wireMock.stubFor(
				get(urlPathMatching("/v8/finance/chart/USDKRW.*"))
						.willReturn(
								aResponse()
										.withStatus(200)
										.withHeader("Content-Type", "application/json")
										.withBody(
												"""
												{
												"chart": {
													"result": [
													{
														"timestamp": [%d],
														"indicators": {"quote": [{"close": [1300.12]}]}
													}
													],
													"error": null
												}
												}
												"""
														.formatted(epoch))));

		// 실행
		BigDecimal rate =
				exchangeRateCommandService.resolveAndStoreUsdRelativeRate(
						CurrencyCode.KRW, targetDate);

		// 검증
		assertThat(rate).isEqualByComparingTo("1300.12");
		assertThat(exchangeRateRepository.findAll())
				.anyMatch(
						r ->
								r.getCurrencyCode() == CurrencyCode.KRW
										&& r.getRecordedAt().toLocalDate().isEqual(targetDate)
										&& r.getRate().compareTo(new BigDecimal("1300.12")) == 0);
	}

	@Test
	void resolveAndStoreUsdRelativeRate_yahooServerError_throwsExchangeRateApiError() {
		// 준비
		LocalDate targetDate = LocalDate.of(2026, 2, 12);
		wireMock.stubFor(
				get(urlPathMatching("/v8/finance/chart/USDKRW.*"))
						.willReturn(aResponse().withStatus(500).withBody("internal error")));

		// 실행 + 검증
		assertThatThrownBy(
						() ->
								exchangeRateCommandService.resolveAndStoreUsdRelativeRate(
										CurrencyCode.KRW, targetDate))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("code", ErrorCode.EXCHANGE_RATE_API_ERROR);
	}

	@Test
	void resolveAndStoreUsdRelativeRate_invalidYahooPayload_throwsExchangeRateApiError() {
		// 준비
		LocalDate targetDate = LocalDate.of(2026, 2, 12);
		wireMock.stubFor(
				get(urlPathMatching("/v8/finance/chart/USDJPY.*"))
						.willReturn(
								aResponse()
										.withStatus(200)
										.withHeader("Content-Type", "application/json")
										.withBody("{ this-is-invalid-json }")));

		// 실행 + 검증
		assertThatThrownBy(
						() ->
								exchangeRateCommandService.resolveAndStoreUsdRelativeRate(
										CurrencyCode.JPY, targetDate))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("code", ErrorCode.EXCHANGE_RATE_API_ERROR);
	}

	@Test
	void resolveAndStoreUsdRelativeRate_yahooTimeout_throwsExchangeRateApiError() {
		// 준비
		LocalDate targetDate = LocalDate.of(2026, 2, 12);
		long epoch = targetDate.atStartOfDay(ZoneOffset.UTC).toEpochSecond();
		wireMock.stubFor(
				get(urlPathMatching("/v8/finance/chart/USDGBP.*"))
						.willReturn(
								aResponse()
										.withStatus(200)
										.withFixedDelay(1500)
										.withHeader("Content-Type", "application/json")
										.withBody(
												"""
												{
												"chart": {
													"result": [
													{
														"timestamp": [%d],
														"indicators": {"quote": [{"close": [0.79]}]}
													}
													],
													"error": null
												}
												}
												"""
														.formatted(epoch))));

		// 실행 + 검증
		assertThatThrownBy(
						() ->
								exchangeRateCommandService.resolveAndStoreUsdRelativeRate(
										CurrencyCode.GBP, targetDate))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("code", ErrorCode.EXCHANGE_RATE_API_ERROR);
	}
}
