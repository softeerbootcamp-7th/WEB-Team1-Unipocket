package com.genesis.unipocket.expense.service.impl;

import com.genesis.unipocket.expense.service.ExchangeRateService;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * <b>네이버 환율 API 기반 환율 서비스 구현체</b>
 * <p>Redis 캐싱을 통해 성능 최적화
 *
 * @author bluefishez
 * @since 2026-02-07
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NaverExchangeRateServiceImpl implements ExchangeRateService {

	private static final String CACHE_KEY_PREFIX = "exchange_rate:";
	private static final long CACHE_TTL_SECONDS = 3600; // 1시간
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

	private final RedisTemplate<String, String> redisTemplate;
	private final RestTemplate restTemplate;

	@Override
	public BigDecimal getExchangeRate(CurrencyCode from, CurrencyCode to, LocalDateTime dateTime) {
		// 동일 통화인 경우 1.0 반환
		if (from == to) {
			return BigDecimal.ONE;
		}

		// 캐시 키 생성
		String cacheKey = buildCacheKey(from, to, dateTime);

		// 캐시 조회
		String cachedRate = redisTemplate.opsForValue().get(cacheKey);
		if (cachedRate != null) {
			log.debug("Cache hit for exchange rate: {} -> {}", from, to);
			return new BigDecimal(cachedRate);
		}

		// API 호출하여 환율 조회
		BigDecimal rate = fetchExchangeRateFromApi(from, to, dateTime);

		// 캐시 저장
		redisTemplate
				.opsForValue()
				.set(cacheKey, rate.toString(), java.time.Duration.ofSeconds(CACHE_TTL_SECONDS));

		return rate;
	}

	@Override
	public BigDecimal convertAmount(
			BigDecimal amount, CurrencyCode from, CurrencyCode to, LocalDateTime dateTime) {
		if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new BusinessException(ErrorCode.EXPENSE_INVALID_AMOUNT);
		}

		BigDecimal rate = getExchangeRate(from, to, dateTime);
		return amount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
	}

	private String buildCacheKey(CurrencyCode from, CurrencyCode to, LocalDateTime dateTime) {
		String date = dateTime.format(DATE_FORMATTER);
		return CACHE_KEY_PREFIX + from.name() + ":" + to.name() + ":" + date;
	}

	private BigDecimal fetchExchangeRateFromApi(
			CurrencyCode from, CurrencyCode to, LocalDateTime dateTime) {
		try {
			// TODO: 실제 네이버 환율 API 연동
			// 현재는 임시로 하드코딩된 환율 반환
			log.warn(
					"Using hardcoded exchange rate for {} -> {}. Implement Naver API integration.",
					from,
					to);

			// 임시 환율 데이터 (실제 구현 시 삭제)
			return getHardcodedExchangeRate(from, to);
		} catch (BusinessException e) {
			// BusinessException은 그대로 다시 throw (의도된 예외)
			throw e;
		} catch (Exception e) {
			// 그 외 예외는 API 에러로 변환
			log.error("Failed to fetch exchange rate from API: {} -> {}", from, to, e);
			throw new BusinessException(ErrorCode.EXCHANGE_RATE_API_ERROR);
		}
	}

	/**
	 * 임시 하드코딩 환율 (실제 API 연동 전까지 사용)
	 */
	private BigDecimal getHardcodedExchangeRate(CurrencyCode from, CurrencyCode to) {
		// KRW 기준 주요 통화 환율 (2026-02-07 기준 예상값)
		if (from == CurrencyCode.KRW && to == CurrencyCode.JPY) {
			return BigDecimal.valueOf(0.11); // 1 KRW = 0.11 JPY
		}
		if (from == CurrencyCode.KRW && to == CurrencyCode.USD) {
			return BigDecimal.valueOf(0.00075); // 1 KRW = 0.00075 USD
		}
		if (from == CurrencyCode.JPY && to == CurrencyCode.KRW) {
			return BigDecimal.valueOf(9.09); // 1 JPY = 9.09 KRW
		}
		if (from == CurrencyCode.USD && to == CurrencyCode.KRW) {
			return BigDecimal.valueOf(1333.33); // 1 USD = 1333.33 KRW
		}

		// 기타 환율은 USD를 중간 통화로 사용한 간접 환산
		log.warn("Using indirect conversion through USD for {} -> {}", from, to);
		throw new BusinessException(ErrorCode.EXCHANGE_RATE_NOT_FOUND);
	}
}
