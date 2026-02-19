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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
	private static final int FETCH_LOOKBACK_DAYS = 14;

	@Value("${exchange.yahoo.chart-url:https://query1.finance.yahoo.com/v8/finance/chart/{symbol}}")
	private String yahooChartUrl = "https://query1.finance.yahoo.com/v8/finance/chart/{symbol}";

	private final ExchangeRateRepository exchangeRateRepository;
	private final ExchangeRateQueryService exchangeRateQueryService;
	private final RestTemplate restTemplate;
	private final ObjectMapper objectMapper;

	@Override
	public BigDecimal resolveAndStoreUsdRelativeRate(
			CurrencyCode currencyCode, LocalDate targetDate) {
		LocalDate oldestAllowedDate = targetDate.minusDays(MAX_BACKTRACK_DAYS);
		LocalDate probeEndDate = targetDate;

		while (!probeEndDate.isBefore(oldestAllowedDate)) {
			LocalDate probeStartDate = probeEndDate.minusDays(FETCH_LOOKBACK_DAYS);
			if (probeStartDate.isBefore(oldestAllowedDate)) {
				probeStartDate = oldestAllowedDate;
			}

			Optional<RateOnDate> dbRate =
					findLatestDbRateInRange(currencyCode, probeStartDate, probeEndDate);
			if (dbRate.isPresent()) {
				BigDecimal rate = dbRate.get().rate();
				saveRateIfMissing(currencyCode, targetDate, rate);
				return rate;
			}

			Map<LocalDate, BigDecimal> yahooRates =
					fetchUsdRelativeRatesFromYahooForRange(
							currencyCode, probeStartDate, probeEndDate);
			saveMissingRates(currencyCode, yahooRates);

			Optional<RateOnDate> yahooRate =
					findLatestRateInMap(yahooRates, probeStartDate, probeEndDate);
			if (yahooRate.isPresent()) {
				RateOnDate found = yahooRate.get();
				if (!found.date().isEqual(targetDate)) {
					saveRateIfMissing(currencyCode, targetDate, found.rate());
				}
				return found.rate();
			}

			probeEndDate = probeStartDate.minusDays(1);
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

	private Optional<RateOnDate> findLatestDbRateInRange(
			CurrencyCode currencyCode, LocalDate startDate, LocalDate endDate) {
		for (LocalDate date = endDate; !date.isBefore(startDate); date = date.minusDays(1)) {
			Optional<ExchangeRate> dbRate =
					exchangeRateQueryService.findRateOnDate(currencyCode, date);
			if (dbRate.isPresent()) {
				return Optional.of(new RateOnDate(date, dbRate.get().getRate()));
			}
		}
		return Optional.empty();
	}

	private Optional<RateOnDate> findLatestRateInMap(
			Map<LocalDate, BigDecimal> ratesByDate, LocalDate startDate, LocalDate endDate) {
		for (LocalDate date = endDate; !date.isBefore(startDate); date = date.minusDays(1)) {
			BigDecimal rate = ratesByDate.get(date);
			if (rate != null) {
				return Optional.of(new RateOnDate(date, rate));
			}
		}
		return Optional.empty();
	}

	private void saveMissingRates(
			CurrencyCode currencyCode, Map<LocalDate, BigDecimal> ratesByDate) {
		for (Map.Entry<LocalDate, BigDecimal> entry : ratesByDate.entrySet()) {
			saveRateIfMissing(currencyCode, entry.getKey(), entry.getValue());
		}
	}

	private Map<LocalDate, BigDecimal> fetchUsdRelativeRatesFromYahooForRange(
			CurrencyCode currencyCode, LocalDate startDate, LocalDate endDate) {
		long period1 = startDate.atStartOfDay(ZoneOffset.UTC).toEpochSecond();
		long period2 = endDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toEpochSecond();
		String symbol = "USD" + currencyCode.name() + "=X";
		String url =
				UriComponentsBuilder.fromUriString(yahooChartUrl)
						.queryParam("interval", "1d")
						.queryParam("period1", period1)
						.queryParam("period2", period2)
						.buildAndExpand(symbol)
						.toUriString();

		String responseBody;
		try {
			responseBody = restTemplate.getForObject(url, String.class);
		} catch (Exception e) {
			log.error(
					"Yahoo rate request failed. currency={}, startDate={}, endDate={}",
					currencyCode,
					startDate,
					endDate,
					e);
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
					"Yahoo rate response parse failed. currency={}, startDate={}, endDate={}",
					currencyCode,
					startDate,
					endDate,
					e);
			throw new BusinessException(ErrorCode.EXCHANGE_RATE_API_ERROR);
		}

		YahooChartResponse.Chart chart = response.chart();
		if (chart == null) {
			return Map.of();
		}

		if (chart.error() != null) {
			log.warn(
					"Yahoo does not provide symbol data. currency={}, startDate={}, endDate={},"
							+ " error={}",
					currencyCode,
					startDate,
					endDate,
					chart.error());
			throw new BusinessException(ErrorCode.EXCHANGE_RATE_NOT_FOUND);
		}

		if (chart.result() == null || chart.result().isEmpty()) {
			return Map.of();
		}

		Map<LocalDate, BigDecimal> ratesByDate = new HashMap<>();
		for (YahooChartResponse.Result result : chart.result()) {
			if (result == null
					|| result.timestamp() == null
					|| result.indicators() == null
					|| result.indicators().quote() == null
					|| result.indicators().quote().isEmpty()) {
				continue;
			}

			YahooChartResponse.Quote firstQuote = result.indicators().quote().get(0);
			if (firstQuote == null || firstQuote.close() == null) {
				continue;
			}

			int size = Math.min(result.timestamp().size(), firstQuote.close().size());
			for (int i = 0; i < size; i++) {
				Long epochSecond = result.timestamp().get(i);
				BigDecimal close = firstQuote.close().get(i);
				if (epochSecond == null || close == null || close.compareTo(BigDecimal.ZERO) <= 0) {
					continue;
				}

				LocalDate candidateDate =
						Instant.ofEpochSecond(epochSecond).atZone(ZoneOffset.UTC).toLocalDate();
				if (candidateDate.isBefore(startDate) || candidateDate.isAfter(endDate)) {
					continue;
				}
				ratesByDate.put(candidateDate, close);
			}
		}

		return ratesByDate;
	}

	private record RateOnDate(LocalDate date, BigDecimal rate) {}
}
