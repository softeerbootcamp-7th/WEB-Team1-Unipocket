package com.genesis.unipocket.auth.service;

import com.genesis.unipocket.auth.exception.oauth.OAuthException;
import com.genesis.unipocket.global.config.OAuth2Properties.ProviderType;
import com.genesis.unipocket.global.exception.ErrorCode;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * <b>OAuth 로그인 State Service</b>
 *
 * <p>Redis를 사용하여 OAuth state를 임시 저장합니다. State는 10분 후 자동 만료됩니다.
 *
 * @author 김동균
 * @since 2026-01-30
 */
@Service
@RequiredArgsConstructor
public class OAuthLoginStateService {

	private static final String KEY_PREFIX = "oauth:state:";
	private static final Duration TTL = Duration.ofMinutes(10);

	private final StringRedisTemplate redisTemplate;

	public void saveLoginState(String state, ProviderType providerType) {
		redisTemplate.opsForValue().set(KEY_PREFIX + state, providerType.name(), TTL);
	}

	public void validateState(String state, ProviderType providerType) {
		String value = redisTemplate.opsForValue().getAndDelete(KEY_PREFIX + state);

		if (value == null) {
			throw new OAuthException(ErrorCode.INVALID_OAUTH_STATE);
		}

		if (!value.equals(providerType.name())) {
			throw new OAuthException(ErrorCode.INVALID_OAUTH_STATE);
		}
	}
}
