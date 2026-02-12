package com.genesis.unipocket.exchange.command.application.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.genesis.unipocket.exchange.command.application.ExchangeRateCommandService;
import com.genesis.unipocket.exchange.command.application.impl.dto.YahooChartResponse;
import com.genesis.unipocket.exchange.command.persistence.entity.ExchangeRate;
import com.genesis.unipocket.exchange.command.persistence.repository.ExchangeRateRepository;
import com.genesis.unipocket.exchange.query.application.ExchangeRateQueryService;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ExchangeRateCommandServiceImpl implements ExchangeRateCommandService {

	private static final int MAX_BACKTRACK_DAYS = 3650;
	private static final String YAHOO_CHART_URL =
			"https://query1.finance.yahoo.com/v8/finance/chart/{symbol}";

	private final ExchangeRateRepository exchangeRateRepository;
	private final ExchangeRateQueryService exchangeRateQueryService;
	private final RestTemplate restTemplate;
	private final ObjectMapper objectMapper;

	@Override
	public BigDecimal resolveAndStoreUsdRelativeRate(
			CurrencyCode currencyCode, LocalDate targetDate) {
		LocalDate probeDate = targetDate;
		int attempt = 0;
		while (attempt <= MAX_BACKTRACK_DAYS) {
			Optional<ExchangeRate> dbRate =
					exchangeRateQueryService.findRateOnDate(currencyCode, probeDate);
			if (dbRate.isPresent()) {
				BigDecimal rate = dbRate.get().getRate();
				saveRateIfMissing(currencyCode, targetDate, rate);
				return rate;
			}

			Optional<BigDecimal> yahooRate =
					fetchUsdRelativeRateFromYahooForDate(currencyCode, probeDate);
			if (yahooRate.isPresent()) {
				BigDecimal rate = yahooRate.get();
				if (probeDate.isEqual(targetDate)) {
					saveRateIfMissing(currencyCode, targetDate, rate);
				} else {
					saveRateIfMissing(currencyCode, probeDate, rate);
					saveRateIfMissing(currencyCode, targetDate, rate);
				}
				return rate;
			}

			probeDate = probeDate.minusDays(1);
			attempt++;
		}

		log.warn(
				"Exchange rate not found after backtracking. currency={}, targetDate={},"
						+ " maxDays={}",
				currencyCode,
				targetDate,
				MAX_BACKTRACK_DAYS);
		throw new BusinessException(ErrorCode.EXCHANGE_RATE_NOT_FOUND);
	}

	private void saveRateIfMissing(CurrencyCode currencyCode, LocalDate date, BigDecimal rate) {
		if (exchangeRateQueryService.findRateOnDate(currencyCode, date).isPresent()) {
			return;
		}
		exchangeRateRepository.save(
				ExchangeRate.builder()
						.currencyCode(currencyCode)
						.recordedAt(date.atStartOfDay())
						.rate(rate)
						.build());
	}

	private Optional<BigDecimal> fetchUsdRelativeRateFromYahooForDate(
			CurrencyCode currencyCode, LocalDate date) {
		long period1 = date.atStartOfDay(ZoneOffset.UTC).toEpochSecond();
		long period2 = date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toEpochSecond();
		String symbol = "USD" + currencyCode.name() + "=X";
		String url =
				UriComponentsBuilder.fromUriString(YAHOO_CHART_URL)
						.queryParam("interval", "1d")
						.queryParam("period1", period1)
						.queryParam("period2", period2)
						.buildAndExpand(symbol)
						.toUriString();

		String responseBody;
		try {
			responseBody = restTemplate.getForObject(url, String.class);
		} catch (Exception e) {
			log.error("Yahoo rate request failed. currency={}, date={}", currencyCode, date, e);
			throw new BusinessException(ErrorCode.EXCHANGE_RATE_API_ERROR);
		}
		if (responseBody == null || responseBody.isBlank()) {
			throw new BusinessException(ErrorCode.EXCHANGE_RATE_API_ERROR);
		}

		YahooChartResponse response;
		try {
			response = objectMapper.readValue(responseBody, YahooChartResponse.class);
		} catch (Exception e) {
			log.error(
					"Yahoo rate response parse failed. currency={}, date={}",
					currencyCode,
					date,
					e);
			throw new BusinessException(ErrorCode.EXCHANGE_RATE_API_ERROR);
		}

		YahooChartResponse.Chart chart = response.chart();
		if (chart == null) {
			return Optional.empty();
		}

		if (chart.error() != null) {
			log.warn(
					"Yahoo does not provide symbol data. currency={}, date={}, error={}",
					currencyCode,
					date,
					chart.error());
			throw new BusinessException(ErrorCode.EXCHANGE_RATE_NOT_FOUND);
		}

		if (chart.result() == null || chart.result().isEmpty()) {
			return Optional.empty();
		}

		YahooChartResponse.Result firstResult = chart.result().get(0);
		if (firstResult == null
				|| firstResult.timestamp() == null
				|| firstResult.indicators() == null
				|| firstResult.indicators().quote() == null
				|| firstResult.indicators().quote().isEmpty()) {
			return Optional.empty();
		}

		YahooChartResponse.Quote firstQuote = firstResult.indicators().quote().get(0);
		if (firstQuote == null || firstQuote.close() == null) {
			return Optional.empty();
		}

		int size = Math.min(firstResult.timestamp().size(), firstQuote.close().size());
		for (int i = 0; i < size; i++) {
			Long epochSecond = firstResult.timestamp().get(i);
			BigDecimal close = firstQuote.close().get(i);
			if (epochSecond == null || close == null) {
				continue;
			}

			LocalDate candidateDate =
					Instant.ofEpochSecond(epochSecond).atZone(ZoneOffset.UTC).toLocalDate();
			if (!candidateDate.isEqual(date)) {
				continue;
			}

			if (close.compareTo(BigDecimal.ZERO) > 0) {
				return Optional.of(close);
			}
		}

		return Optional.empty();
	}
}
