package com.genesis.unipocket.exchange.command.application.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.genesis.unipocket.exchange.command.application.ExchangeRateCommandService;
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

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ExchangeRateCommandServiceImpl implements ExchangeRateCommandService {

	private static final int MAX_BACKTRACK_DAYS = 3650;
	private static final String YAHOO_CHART_URL_TEMPLATE =
			"https://query1.finance.yahoo.com/v8/finance/chart/%s?interval=1d&period1=%d&period2=%d";
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private final ExchangeRateRepository exchangeRateRepository;
	private final ExchangeRateQueryService exchangeRateQueryService;
	private final RestTemplate restTemplate;

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
		String url = String.format(YAHOO_CHART_URL_TEMPLATE, symbol, period1, period2);

		try {
			String responseBody = restTemplate.getForObject(url, String.class);
			if (responseBody == null || responseBody.isBlank()) {
				throw new BusinessException(ErrorCode.EXCHANGE_RATE_API_ERROR);
			}

			JsonNode root = OBJECT_MAPPER.readTree(responseBody);
			JsonNode errorNode = root.path("chart").path("error");
			if (!errorNode.isMissingNode() && !errorNode.isNull()) {
				log.warn(
						"Yahoo does not provide symbol data. currency={}, date={}, error={}",
						currencyCode,
						date,
						errorNode.toString());
				throw new BusinessException(ErrorCode.EXCHANGE_RATE_NOT_FOUND);
			}

			JsonNode resultNode = root.path("chart").path("result");
			if (!resultNode.isArray() || resultNode.isEmpty()) {
				return Optional.empty();
			}

			JsonNode firstResult = resultNode.get(0);
			JsonNode timestamps = firstResult.path("timestamp");
			JsonNode quoteArray = firstResult.path("indicators").path("quote");
			if (!timestamps.isArray() || !quoteArray.isArray() || quoteArray.isEmpty()) {
				return Optional.empty();
			}

			JsonNode closes = quoteArray.get(0).path("close");
			if (!closes.isArray()) {
				return Optional.empty();
			}

			int size = Math.min(timestamps.size(), closes.size());
			for (int i = 0; i < size; i++) {
				JsonNode tsNode = timestamps.get(i);
				JsonNode closeNode = closes.get(i);
				if (tsNode == null
						|| !tsNode.isNumber()
						|| closeNode == null
						|| !closeNode.isNumber()) {
					continue;
				}

				LocalDate candidateDate =
						Instant.ofEpochSecond(tsNode.asLong()).atZone(ZoneOffset.UTC).toLocalDate();
				if (!candidateDate.isEqual(date)) {
					continue;
				}

				BigDecimal close = closeNode.decimalValue();
				if (close.compareTo(BigDecimal.ZERO) > 0) {
					return Optional.of(close);
				}
			}

			return Optional.empty();
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			log.error("Yahoo rate fetch failed. currency={}, date={}", currencyCode, date, e);
			throw new BusinessException(ErrorCode.EXCHANGE_RATE_API_ERROR);
		}
	}
}
