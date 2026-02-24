package com.genesis.unipocket.analysis.query.application;

import com.genesis.unipocket.analysis.common.enums.CurrencyType;
import com.genesis.unipocket.analysis.common.util.OffsetDateTimeConverter;
import com.genesis.unipocket.analysis.query.persistence.repository.AnalysisQueryRepository;
import com.genesis.unipocket.exchange.common.service.ExchangeRateService;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class AnalysisMonthlyDailySeriesBuilder {

	private final AnalysisQueryRepository analysisQueryRepository;
	private final ExchangeRateService exchangeRateService;

	AnalysisDailySeries build(
			Long accountBookId,
			CurrencyType currencyType,
			LocalDateTime startUtc,
			LocalDateTime endUtcExclusive,
			LocalDate startLocalDate,
			LocalDate endLocalDateInclusive,
			ZoneId zoneId,
			CurrencyCode localCurrency) {
		Map<LocalDate, BigDecimal> dailyMap;
		OffsetDateTime startOffset = toOffsetUtc(startUtc);
		OffsetDateTime endOffset = toOffsetUtc(endUtcExclusive);
		if (currencyType == CurrencyType.LOCAL) {
			OffsetDateTime refDateTime = toOffsetUtc(startUtc);
			try (Stream<Object[]> stream =
					analysisQueryRepository.getMySpendEventsWithCurrency(
							accountBookId, startOffset, endOffset)) {
				dailyMap =
						toDailyAmountMapWithConversion(stream, localCurrency, refDateTime, zoneId);
			}
		} else {
			try (Stream<Object[]> stream =
					analysisQueryRepository.getMySpendEvents(
							accountBookId, startOffset, endOffset, currencyType)) {
				dailyMap = toDailyAmountMap(stream, zoneId);
			}
		}

		BigDecimal cumulative = BigDecimal.ZERO;
		List<AnalysisDailyRow> items = new ArrayList<>();
		for (LocalDate date = startLocalDate;
				!date.isAfter(endLocalDateInclusive);
				date = date.plusDays(1)) {
			BigDecimal dailySpend = dailyMap.getOrDefault(date, BigDecimal.ZERO);
			cumulative = cumulative.add(dailySpend);
			items.add(new AnalysisDailyRow(date.toString(), cumulative));
		}
		return new AnalysisDailySeries(items, cumulative);
	}

	private Map<LocalDate, BigDecimal> toDailyAmountMapWithConversion(
			Stream<Object[]> stream,
			CurrencyCode targetCurrency,
			OffsetDateTime refDateTime,
			ZoneId zoneId) {
		Map<LocalDate, BigDecimal> result = new HashMap<>();
		stream.forEach(
				row -> {
					LocalDate date = toLocalDateInZone(row[0], zoneId);
					CurrencyCode from = (CurrencyCode) row[2];
					BigDecimal amount = toBigDecimal(row[1]);
					if (from != null && from != targetCurrency) {
						amount =
								exchangeRateService.convertAmount(
										amount, from, targetCurrency, refDateTime);
					}
					result.merge(date, amount, BigDecimal::add);
				});
		return result;
	}

	private Map<LocalDate, BigDecimal> toDailyAmountMap(Stream<Object[]> stream, ZoneId zoneId) {
		return stream.collect(
				Collectors.groupingBy(
						row -> toLocalDateInZone(row[0], zoneId),
						Collectors.reducing(
								BigDecimal.ZERO, row -> toBigDecimal(row[1]), BigDecimal::add)));
	}

	private LocalDate toLocalDateInZone(Object occurredAt, ZoneId zoneId) {
		OffsetDateTime offsetDateTime = OffsetDateTimeConverter.from(occurredAt);
		return offsetDateTime.atZoneSameInstant(zoneId).toLocalDate();
	}

	private OffsetDateTime toOffsetUtc(LocalDateTime localDateTime) {
		return localDateTime.atOffset(ZoneOffset.UTC);
	}

	private BigDecimal toBigDecimal(Object value) {
		if (value == null) {
			return BigDecimal.ZERO;
		}
		if (value instanceof BigDecimal bd) {
			return bd;
		}
		return new BigDecimal(value.toString());
	}
}
