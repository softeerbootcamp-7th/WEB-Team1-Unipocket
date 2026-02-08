package com.genesis.unipocket.expense.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.client.RestTemplate;

/**
 * <b>네이버 환율 서비스 단위 테스트</b>
 *
 * @author codingbaraGo
 * @since 2026-02-07
 */
@ExtendWith(MockitoExtension.class)
class NaverExchangeRateServiceImplTest {

	@Mock private RedisTemplate<String, String> redisTemplate;
	@Mock private RestTemplate restTemplate;
	@Mock private ValueOperations<String, String> valueOperations;

	@InjectMocks private NaverExchangeRateServiceImpl exchangeRateService;

	@Test
	@DisplayName("동일 통화 처리 - rate = 1.0 반환")
	void getExchangeRate_sameCurrency_returnsOne() {
		// given
		CurrencyCode currency = CurrencyCode.KRW;
		LocalDateTime dateTime = LocalDateTime.now();

		// when
		BigDecimal rate = exchangeRateService.getExchangeRate(currency, currency, dateTime);

		// then
		assertThat(rate).isEqualTo(BigDecimal.ONE);
		verifyNoInteractions(redisTemplate, restTemplate); // 캐시/API 호출 없음
	}

	@Test
	@DisplayName("캐시 히트 - Redis에서 환율 조회 성공")
	void getExchangeRate_cacheHit_returnsFromCache() {
		// given
		CurrencyCode from = CurrencyCode.KRW;
		CurrencyCode to = CurrencyCode.JPY;
		LocalDateTime dateTime = LocalDateTime.now();
		String cachedRate = "0.11";

		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		when(valueOperations.get(anyString())).thenReturn(cachedRate);

		// when
		BigDecimal rate = exchangeRateService.getExchangeRate(from, to, dateTime);

		// then
		assertThat(rate).isEqualTo(new BigDecimal(cachedRate));
		verifyNoInteractions(restTemplate); // API 호출 없음
	}

	@Test
	@DisplayName("캐시 미스 - API 호출 후 캐시 저장")
	void getExchangeRate_cacheMiss_fetchesFromApiAndCaches() {
		// given
		CurrencyCode from = CurrencyCode.KRW;
		CurrencyCode to = CurrencyCode.JPY;
		LocalDateTime dateTime = LocalDateTime.now();

		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		when(valueOperations.get(anyString())).thenReturn(null); // 캐시 미스

		// when
		BigDecimal rate = exchangeRateService.getExchangeRate(from, to, dateTime);

		// then
		assertThat(rate).isNotNull();
		verify(valueOperations, times(1))
				.set(anyString(), anyString(), any(Duration.class)); // 캐시 저장 확인
	}

	@Test
	@DisplayName("금액 변환 - 0 이하 금액 시 예외 발생")
	void convertAmount_invalidAmount_throwsException() {
		// given
		BigDecimal invalidAmount = BigDecimal.ZERO;
		CurrencyCode from = CurrencyCode.KRW;
		CurrencyCode to = CurrencyCode.JPY;
		LocalDateTime dateTime = LocalDateTime.now();

		// when & then
		assertThatThrownBy(
						() -> exchangeRateService.convertAmount(invalidAmount, from, to, dateTime))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("code", ErrorCode.EXPENSE_INVALID_AMOUNT);
	}

	@Test
	@DisplayName("금액 변환 - 정상 변환")
	void convertAmount_success() {
		// given
		BigDecimal amount = BigDecimal.valueOf(10000);
		CurrencyCode from = CurrencyCode.KRW;
		CurrencyCode to = CurrencyCode.JPY;
		LocalDateTime dateTime = LocalDateTime.now();

		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		when(valueOperations.get(anyString())).thenReturn("0.11"); // 1 KRW = 0.11 JPY

		// when
		BigDecimal convertedAmount = exchangeRateService.convertAmount(amount, from, to, dateTime);

		// then
		assertThat(convertedAmount).isEqualByComparingTo(BigDecimal.valueOf(1100.00));
	}

	@Test
	@DisplayName("하드코딩된 환율 - KRW to JPY")
	void getExchangeRate_hardcoded_KRWtoJPY() {
		// given
		CurrencyCode from = CurrencyCode.KRW;
		CurrencyCode to = CurrencyCode.JPY;
		LocalDateTime dateTime = LocalDateTime.now();

		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		when(valueOperations.get(anyString())).thenReturn(null);

		// when
		BigDecimal rate = exchangeRateService.getExchangeRate(from, to, dateTime);

		// then
		assertThat(rate).isEqualTo(BigDecimal.valueOf(0.11));
	}

	@Test
	@DisplayName("지원하지 않는 통화 쌍 - 예외 발생")
	void getExchangeRate_unsupportedCurrencyPair_throwsException() {
		// given
		CurrencyCode from = CurrencyCode.EUR;
		CurrencyCode to = CurrencyCode.GBP;
		LocalDateTime dateTime = LocalDateTime.now();

		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		when(valueOperations.get(anyString())).thenReturn(null);

		// when & then
		assertThatThrownBy(() -> exchangeRateService.getExchangeRate(from, to, dateTime))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("code", ErrorCode.EXCHANGE_RATE_NOT_FOUND);
	}
}
