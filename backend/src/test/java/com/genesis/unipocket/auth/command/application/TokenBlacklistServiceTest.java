package com.genesis.unipocket.auth.command.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

/**
 * TokenBlacklistService 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TokenBlacklistService 단위 테스트")
class TokenBlacklistServiceTest {

	@Mock private RedisTemplate<String, String> redisTemplate;

	@Mock private ValueOperations<String, String> valueOperations;

	@InjectMocks private TokenBlacklistService tokenBlacklistService;

	@Test
	@DisplayName("블랙리스트에 토큰 추가 - 성공")
	void addToBlacklist_Success() {
		// given
		String jti = "test-jti-12345";
		long ttlMs = 60000L; // 60초
		when(redisTemplate.opsForValue()).thenReturn(valueOperations);

		// when
		tokenBlacklistService.addToBlacklist(jti, ttlMs);

		// then
		verify(valueOperations)
				.set(eq("blacklist:token:" + jti), eq("true"), eq(60L), eq(TimeUnit.SECONDS));
	}

	@Test
	@DisplayName("블랙리스트에 토큰 추가 - TTL이 0인 경우 등록하지 않음")
	void addToBlacklist_ZeroTTL_NotAdded() {
		// given
		String jti = "test-jti-12345";
		long ttlMs = 0L;

		// when
		tokenBlacklistService.addToBlacklist(jti, ttlMs);

		// then
		verify(redisTemplate, never()).opsForValue();
	}

	@Test
	@DisplayName("블랙리스트에 토큰 추가 - TTL이 음수인 경우 등록하지 않음")
	void addToBlacklist_NegativeTTL_NotAdded() {
		// given
		String jti = "test-jti-12345";
		long ttlMs = -1000L;

		// when
		tokenBlacklistService.addToBlacklist(jti, ttlMs);

		// then
		verify(redisTemplate, never()).opsForValue();
	}

	@Test
	@DisplayName("블랙리스트에 있는지 확인 - 있음")
	void isBlacklisted_Exists() {
		// given
		String jti = "test-jti-12345";
		String key = "blacklist:token:" + jti;
		when(redisTemplate.hasKey(key)).thenReturn(true);

		// when
		boolean result = tokenBlacklistService.isBlacklisted(jti);

		// then
		assertThat(result).isTrue();
		verify(redisTemplate).hasKey(key);
	}

	@Test
	@DisplayName("블랙리스트에 있는지 확인 - 없음")
	void isBlacklisted_NotExists() {
		// given
		String jti = "test-jti-12345";
		String key = "blacklist:token:" + jti;
		when(redisTemplate.hasKey(key)).thenReturn(false);

		// when
		boolean result = tokenBlacklistService.isBlacklisted(jti);

		// then
		assertThat(result).isFalse();
		verify(redisTemplate).hasKey(key);
	}

	@Test
	@DisplayName("블랙리스트에 있는지 확인 - null 반환 시 false")
	void isBlacklisted_NullResponse() {
		// given
		String jti = "test-jti-12345";
		String key = "blacklist:token:" + jti;
		when(redisTemplate.hasKey(key)).thenReturn(null);

		// when
		boolean result = tokenBlacklistService.isBlacklisted(jti);

		// then
		assertThat(result).isFalse();
	}

	@Test
	@DisplayName("올바른 키 형식으로 저장됨")
	void addToBlacklist_CorrectKeyFormat() {
		// given
		String jti = "abc-123-def-456";
		long ttlMs = 10000L;
		when(redisTemplate.opsForValue()).thenReturn(valueOperations);

		// when
		tokenBlacklistService.addToBlacklist(jti, ttlMs);

		// then
		verify(valueOperations)
				.set(eq("blacklist:token:abc-123-def-456"), anyString(), anyLong(), any());
	}

	@Test
	@DisplayName("TTL이 밀리초에서 초로 정확히 변환됨")
	void addToBlacklist_TTLConversion() {
		// given
		String jti = "test-jti";
		long ttlMs = 3600000L; // 1시간
		when(redisTemplate.opsForValue()).thenReturn(valueOperations);

		// when
		tokenBlacklistService.addToBlacklist(jti, ttlMs);

		// then
		verify(valueOperations).set(anyString(), anyString(), eq(3600L), eq(TimeUnit.SECONDS));
	}
}
