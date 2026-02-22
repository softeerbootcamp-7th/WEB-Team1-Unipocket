package com.genesis.unipocket.exchange.command.application.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.genesis.unipocket.exchange.command.application.ExchangeRateCommandService;
import com.genesis.unipocket.exchange.command.application.impl.dto.YahooChartResponse;
import com.genesis.unipocket.exchange.common.persistence.entity.ExchangeRate;
import com.genesis.unipocket.exchange.common.persistence.repository.ExchangeRateRepository;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DeadlockLoserDataAccessException;
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
	private static final int MAX_UPSERT_RETRY = 3;
	private static final int MIN_RETRY_BACKOFF_MS = 20;
	private static final int MAX_RETRY_BACKOFF_MS = 100;

	@Value("${exchange.yahoo.chart-url:https://query1.finance.yahoo.com/v8/finance/chart/{symbol}}")
	private String yahooChartUrl = "https://query1.finance.yahoo.com/v8/finance/chart/{symbol}";

	private final ExchangeRateRepository exchangeRateRepository;
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
				RateOnDate foundDbRate = dbRate.get();
				if (isValidRate(foundDbRate.rate())) {
					BigDecimal rate = foundDbRate.rate();
					if (!foundDbRate.date().isEqual(targetDate)) {
						saveRateIfMissing(currencyCode, targetDate, rate);
					}
					return rate;
				}
				log.warn(
						"Invalid DB rate ignored. currency={}, date={}, rate={}",
						currencyCode,
						foundDbRate.date(),
						foundDbRate.rate());
			}

			Map<LocalDate, BigDecimal> yahooRates =
					fetchUsdRelativeRatesFromYahooForRange(
							currencyCode, probeStartDate, probeEndDate);
			saveMissingRates(currencyCode, yahooRates);

			Optional<RateOnDate> yahooRate =
					findLatestRateInMap(yahooRates, probeStartDate, probeEndDate);
			if (yahooRate.isPresent()) {
				RateOnDate foundYahooRate = yahooRate.get();
				if (isValidRate(foundYahooRate.rate())) {
					if (!foundYahooRate.date().isEqual(targetDate)) {
						saveRateIfMissing(currencyCode, targetDate, foundYahooRate.rate());
					}
					return foundYahooRate.rate();
				}
				log.warn(
						"Invalid Yahoo rate ignored. currency={}, date={}, rate={}",
						currencyCode,
						foundYahooRate.date(),
						foundYahooRate.rate());
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
		if (!isValidRate(rate)) {
			log.warn(
					"Skip saving invalid rate. currency={}, date={}, rate={}",
					currencyCode,
					date,
					rate);
			return;
		}
		LocalDateTime recordedAt = date.atStartOfDay();
		for (int attempt = 1; attempt <= MAX_UPSERT_RETRY; attempt++) {
			try {
				exchangeRateRepository.upsertRate(currencyCode.name(), recordedAt, rate);
				return;
			} catch (DeadlockLoserDataAccessException | CannotAcquireLockException e) {
				if (attempt == MAX_UPSERT_RETRY) {
					throw e;
				}
				long backoffMillis =
						ThreadLocalRandom.current()
								.nextLong(MIN_RETRY_BACKOFF_MS, MAX_RETRY_BACKOFF_MS + 1);
				log.warn(
						"Deadlock/lock timeout during exchange rate upsert. Retrying."
								+ " currency={}, date={}, attempt={}, backoffMs={}",
						currencyCode,
						date,
						attempt,
						backoffMillis);
				sleepBackoff(backoffMillis);
			}
		}
	}

	private Optional<RateOnDate> findLatestDbRateInRange(
			CurrencyCode currencyCode, LocalDate startDate, LocalDate endDate) {
		return findLatestEntityInRange(currencyCode, startDate, endDate)
				.map(
						dbRate ->
								new RateOnDate(
										dbRate.getRecordedAt().toLocalDate(), dbRate.getRate()));
	}

	private Optional<ExchangeRate> findLatestEntityInRange(
			CurrencyCode currencyCode, LocalDate startDate, LocalDate endDate) {
		LocalDateTime startOfRange = startDate.atStartOfDay();
		LocalDateTime endExclusive = endDate.plusDays(1).atStartOfDay();
		return exchangeRateRepository
				.findTopByCurrencyCodeAndRecordedAtGreaterThanEqualAndRecordedAtLessThanOrderByRecordedAtDesc(
						currencyCode, startOfRange, endExclusive);
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
		Map<LocalDate, BigDecimal> sortedRatesByDate = new TreeMap<>(ratesByDate);
		for (Map.Entry<LocalDate, BigDecimal> entry : sortedRatesByDate.entrySet()) {
			saveRateIfMissing(currencyCode, entry.getKey(), entry.getValue());
		}
	}

	private void sleepBackoff(long backoffMillis) {
		try {
			Thread.sleep(backoffMillis);
		} catch (InterruptedException interruptedException) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(
					"Exchange rate upsert retry interrupted", interruptedException);
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

	private boolean isValidRate(BigDecimal rate) {
		return rate != null && rate.compareTo(BigDecimal.ZERO) > 0;
	}

	private record RateOnDate(LocalDate date, BigDecimal rate) {}
}
