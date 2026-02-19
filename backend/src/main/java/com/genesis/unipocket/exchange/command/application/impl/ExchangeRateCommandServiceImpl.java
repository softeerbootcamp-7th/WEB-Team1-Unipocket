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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

// 로깅 유틸 필드를 자동 생성한다.
@Slf4j
// 스프링 서비스 빈으로 등록한다.
@Service
// final 필드 생성자를 자동 생성한다.
@RequiredArgsConstructor
// Command 작업 전체를 트랜잭션으로 수행한다.
@Transactional
// 누락 환율을 보정 조회하고 저장하는 Command 서비스 구현체다.
public class ExchangeRateCommandServiceImpl implements ExchangeRateCommandService {

	// 환율이 비는 구간에서 과거로 내려갈 최대 역추적 일수다.
	private static final int MAX_BACKTRACK_DAYS = 3650;
	// Yahoo 1회 요청 시 조회할 일자 폭(윈도우 크기)다.
	private static final int FETCH_LOOKBACK_DAYS = 14;

	// Yahoo Chart API URL을 설정값으로 주입하고, 미설정 시 기본 URL을 사용한다.
	@Value("${exchange.yahoo.chart-url:https://query1.finance.yahoo.com/v8/finance/chart/{symbol}}")
	private String yahooChartUrl = "https://query1.finance.yahoo.com/v8/finance/chart/{symbol}";

	// 환율 저장소다.
	private final ExchangeRateRepository exchangeRateRepository;
	// 저장된 환율 조회 서비스다.
	private final ExchangeRateQueryService exchangeRateQueryService;
	// 외부 HTTP 호출 클라이언트다.
	private final RestTemplate restTemplate;
	// JSON 파싱기다.
	private final ObjectMapper objectMapper;

	// 통화/일자의 USD 상대 환율을 보정하고 누락 시 저장까지 수행한다.
	@Override
	public BigDecimal resolveAndStoreUsdRelativeRate(
			CurrencyCode currencyCode, LocalDate targetDate) {
		// 역추적 하한 일자를 계산한다.
		LocalDate oldestAllowedDate = targetDate.minusDays(MAX_BACKTRACK_DAYS);
		// 첫 탐색 구간의 끝은 targetDate부터 시작한다.
		LocalDate probeEndDate = targetDate;

		// 하한보다 과거로 넘어가기 전까지 윈도우를 반복 탐색한다.
		while (!probeEndDate.isBefore(oldestAllowedDate)) {
			// 현재 윈도우 시작점을 계산한다.
			LocalDate probeStartDate = probeEndDate.minusDays(FETCH_LOOKBACK_DAYS);
			// 시작점이 하한보다 작으면 하한으로 보정한다.
			if (probeStartDate.isBefore(oldestAllowedDate)) {
				probeStartDate = oldestAllowedDate;
			}

			// 먼저 DB에서 윈도우 내 최신 환율을 조회한다.
			Optional<RateOnDate> dbRate =
					findLatestDbRateInRange(currencyCode, probeStartDate, probeEndDate);
			// DB 결과가 있으면 우선 사용을 시도한다.
			if (dbRate.isPresent()) {
				// Optional 값을 꺼낸다.
				RateOnDate foundDbRate = dbRate.get();
				// 값이 유효(양수)하면 즉시 반환 경로로 간다.
				if (isValidRate(foundDbRate.rate())) {
					// 반환할 환율 값을 로컬 변수에 둔다.
					BigDecimal rate = foundDbRate.rate();
					// targetDate 행이 없으면 같은 환율로 채워 넣는다.
					saveRateIfMissing(currencyCode, targetDate, rate);
					// 보정된 환율을 반환한다.
					return rate;
				}
				// DB 값이 비정상이면 로그만 남기고 Yahoo 조회로 진행한다.
				log.warn(
						"Invalid DB rate ignored. currency={}, date={}, rate={}",
						currencyCode,
						foundDbRate.date(),
						foundDbRate.rate());
			}

			// DB에서 못 찾았거나 비정상이면 Yahoo에서 같은 구간을 조회한다.
			Map<LocalDate, BigDecimal> yahooRates =
					fetchUsdRelativeRatesFromYahooForRange(
							currencyCode, probeStartDate, probeEndDate);
			// Yahoo에서 받은 일자별 환율 중 누락된 행을 저장한다.
			saveMissingRates(currencyCode, yahooRates);

			// Yahoo 맵에서도 윈도우 내 최신 환율을 찾는다.
			Optional<RateOnDate> yahooRate =
					findLatestRateInMap(yahooRates, probeStartDate, probeEndDate);
			// Yahoo 결과가 있으면 반환을 시도한다.
			if (yahooRate.isPresent()) {
				// Optional 값을 꺼낸다.
				RateOnDate foundYahooRate = yahooRate.get();
				// Yahoo 값이 유효(양수)인지 확인한다.
				if (isValidRate(foundYahooRate.rate())) {
					// 실제 발견 일자가 targetDate와 다르면 targetDate 행을 채운다.
					if (!foundYahooRate.date().isEqual(targetDate)) {
						saveRateIfMissing(currencyCode, targetDate, foundYahooRate.rate());
					}
					// 유효 환율을 반환한다.
					return foundYahooRate.rate();
				}
				// Yahoo 값이 비정상이면 로그를 남기고 다음 윈도우로 이동한다.
				log.warn(
						"Invalid Yahoo rate ignored. currency={}, date={}, rate={}",
						currencyCode,
						foundYahooRate.date(),
						foundYahooRate.rate());
			}

			// 다음 루프에서는 더 과거 구간을 탐색하도록 끝점을 이동한다.
			probeEndDate = probeStartDate.minusDays(1);
		}

		// 최대 역추적 범위 안에서도 환율을 못 찾았음을 로그로 남긴다.
		log.warn(
				"Exchange rate not found after backtracking. currency={}, targetDate={},"
						+ " maxDays={}",
				currencyCode,
				targetDate,
				MAX_BACKTRACK_DAYS);
		// 환율 미발견 도메인 예외를 던진다.
		throw new BusinessException(ErrorCode.EXCHANGE_RATE_NOT_FOUND);
	}

	// 단일 일자 환율 행을 "없을 때만" 저장한다.
	private void saveRateIfMissing(CurrencyCode currencyCode, LocalDate date, BigDecimal rate) {
		// 입력 환율이 null/0이하면 저장하지 않는다.
		if (!isValidRate(rate)) {
			// 무효 환율 스킵 로그를 남긴다.
			log.warn(
					"Skip saving invalid rate. currency={}, date={}, rate={}",
					currencyCode,
					date,
					rate);
			return;
		}
		// 이미 해당 일자 환율이 있으면 중복 저장을 생략한다.
		if (exchangeRateQueryService.findLatestRateInRange(currencyCode, date, date).isPresent()) {
			return;
		}
		// 실제 저장을 시도한다.
		try {
			// 일자 시작 시각으로 환율 엔티티를 생성해 저장한다.
			exchangeRateRepository.save(
					ExchangeRate.builder()
							.currencyCode(currencyCode)
							.recordedAt(date.atStartOfDay())
							.rate(rate)
							.build());
		} catch (DataIntegrityViolationException e) {
			// 동시성 중복 insert가 나면 경고 로그만 남기고 진행한다.
			log.warn(
					"Skip duplicate rate insert due to concurrent write. currency={}, date={}",
					currencyCode,
					date);
		}
	}

	// DB 범위 조회 결과를 record 형태(RateOnDate)로 변환한다.
	private Optional<RateOnDate> findLatestDbRateInRange(
			CurrencyCode currencyCode, LocalDate startDate, LocalDate endDate) {
		// query 서비스에서 범위 내 최신 환율을 찾는다.
		return exchangeRateQueryService
				.findLatestRateInRange(currencyCode, startDate, endDate)
				.map(
						// 엔티티를 LocalDate + rate record로 축약한다.
						dbRate ->
								new RateOnDate(
										dbRate.getRecordedAt().toLocalDate(), dbRate.getRate()));
	}

	// 메모리 맵에서 endDate부터 역순으로 가장 가까운 환율을 찾는다.
	private Optional<RateOnDate> findLatestRateInMap(
			Map<LocalDate, BigDecimal> ratesByDate, LocalDate startDate, LocalDate endDate) {
		// endDate -> startDate 순으로 역순 순회한다.
		for (LocalDate date = endDate; !date.isBefore(startDate); date = date.minusDays(1)) {
			// 해당 일자의 환율을 조회한다.
			BigDecimal rate = ratesByDate.get(date);
			// 값이 있으면 즉시 반환한다.
			if (rate != null) {
				return Optional.of(new RateOnDate(date, rate));
			}
		}
		// 구간 내 값이 없으면 빈 Optional을 반환한다.
		return Optional.empty();
	}

	// Yahoo 맵의 환율들을 누락 행에 대해 저장한다.
	private void saveMissingRates(
			CurrencyCode currencyCode, Map<LocalDate, BigDecimal> ratesByDate) {
		// 맵의 모든 엔트리를 순회한다.
		for (Map.Entry<LocalDate, BigDecimal> entry : ratesByDate.entrySet()) {
			// 각 일자/환율을 저장 시도한다(이미 있으면 내부에서 스킵).
			saveRateIfMissing(currencyCode, entry.getKey(), entry.getValue());
		}
	}

	// Yahoo API를 호출해 범위 내 USD 상대 환율 맵을 구성한다.
	private Map<LocalDate, BigDecimal> fetchUsdRelativeRatesFromYahooForRange(
			CurrencyCode currencyCode, LocalDate startDate, LocalDate endDate) {
		// period1은 시작일 00:00:00 UTC epoch second다.
		long period1 = startDate.atStartOfDay(ZoneOffset.UTC).toEpochSecond();
		// period2는 배타 구간이라 endDate 포함을 위해 +1일 00:00:00으로 계산한다.
		long period2 = endDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toEpochSecond();
		// Yahoo 심볼 형식(예: USDKRW=X)을 만든다.
		String symbol = "USD" + currencyCode.name() + "=X";
		// URL 템플릿에 쿼리 파라미터와 심볼을 바인딩한다.
		String url =
				UriComponentsBuilder.fromUriString(yahooChartUrl)
						.queryParam("interval", "1d")
						.queryParam("period1", period1)
						.queryParam("period2", period2)
						.buildAndExpand(symbol)
						.toUriString();

		// HTTP 응답 바디를 받을 변수다.
		String responseBody;
		// Yahoo API 호출을 시도한다.
		try {
			responseBody = restTemplate.getForObject(url, String.class);
		} catch (Exception e) {
			// 통신 실패는 API 오류 예외로 매핑한다.
			log.error(
					"Yahoo rate request failed. currency={}, startDate={}, endDate={}",
					currencyCode,
					startDate,
					endDate,
					e);
			throw new BusinessException(ErrorCode.EXCHANGE_RATE_API_ERROR);
		}
		// 응답이 비어 있으면 API 오류로 본다.
		if (responseBody == null || responseBody.isBlank()) {
			throw new BusinessException(ErrorCode.EXCHANGE_RATE_API_ERROR);
		}

		// 파싱된 Yahoo DTO를 담을 변수다.
		YahooChartResponse response;
		// JSON 바디를 DTO로 파싱한다.
		try {
			response = objectMapper.readValue(responseBody, YahooChartResponse.class);
		} catch (Exception e) {
			// 파싱 실패도 API 오류 예외로 매핑한다.
			log.error(
					"Yahoo rate response parse failed. currency={}, startDate={}, endDate={}",
					currencyCode,
					startDate,
					endDate,
					e);
			throw new BusinessException(ErrorCode.EXCHANGE_RATE_API_ERROR);
		}

		// chart 루트 노드를 꺼낸다.
		YahooChartResponse.Chart chart = response.chart();
		// chart 자체가 없으면 빈 맵을 반환한다.
		if (chart == null) {
			return Map.of();
		}

		// error 노드가 있으면 Yahoo 미지원/미발견 상황으로 본다.
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

		// result 배열이 비면 빈 맵을 반환한다.
		if (chart.result() == null || chart.result().isEmpty()) {
			return Map.of();
		}

		// 일자별 환율을 누적할 맵이다.
		Map<LocalDate, BigDecimal> ratesByDate = new HashMap<>();
		// result 배열을 순회한다.
		for (YahooChartResponse.Result result : chart.result()) {
			// 필수 노드가 비정상이면 해당 result는 건너뛴다.
			if (result == null
					|| result.timestamp() == null
					|| result.indicators() == null
					|| result.indicators().quote() == null
					|| result.indicators().quote().isEmpty()) {
				continue;
			}

			// 현재 구현은 첫 번째 quote 배열만 사용한다.
			YahooChartResponse.Quote firstQuote = result.indicators().quote().get(0);
			// close 배열이 없으면 해당 result는 건너뛴다.
			if (firstQuote == null || firstQuote.close() == null) {
				continue;
			}

			// timestamp와 close 길이 차이를 고려해 최소 길이만 순회한다.
			int size = Math.min(result.timestamp().size(), firstQuote.close().size());
			// 인덱스별 (timestamp, close) 쌍을 순회한다.
			for (int i = 0; i < size; i++) {
				// epoch second 값을 가져온다.
				Long epochSecond = result.timestamp().get(i);
				// close 값을 가져온다.
				BigDecimal close = firstQuote.close().get(i);
				// 값이 null이거나 0 이하이면 무효 데이터로 스킵한다.
				if (epochSecond == null || close == null || close.compareTo(BigDecimal.ZERO) <= 0) {
					continue;
				}

				// epoch second를 UTC 기준 LocalDate로 변환한다.
				LocalDate candidateDate =
						Instant.ofEpochSecond(epochSecond).atZone(ZoneOffset.UTC).toLocalDate();
				// 요청 구간 밖 날짜면 무시한다.
				if (candidateDate.isBefore(startDate) || candidateDate.isAfter(endDate)) {
					continue;
				}
				// 유효한 날짜/환율을 맵에 저장한다.
				ratesByDate.put(candidateDate, close);
			}
		}

		// 수집된 일자별 환율 맵을 반환한다.
		return ratesByDate;
	}

	// 환율 유효성(양수 여부)을 검사한다.
	private boolean isValidRate(BigDecimal rate) {
		return rate != null && rate.compareTo(BigDecimal.ZERO) > 0;
	}

	// 일자와 환율을 함께 다루기 위한 내부 record다.
	private record RateOnDate(LocalDate date, BigDecimal rate) {}
}
