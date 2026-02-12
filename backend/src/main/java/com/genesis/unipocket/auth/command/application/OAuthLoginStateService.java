package com.genesis.unipocket.auth.command.application;

import com.genesis.unipocket.auth.common.config.JwtProperties;
import com.genesis.unipocket.auth.common.exception.oauth.OAuthException;
import com.genesis.unipocket.global.config.OAuth2Properties.ProviderType;
import com.genesis.unipocket.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * <b>OAuth 로그인 State Service</b>
 *
 * <p>
 * Redis를 사용하여 OAuth state를 임시 저장합니다. TTL은 YML 설정에서 관리합니다.
 *
 * @author 김동균
 * @since 2026-01-30
 */
@Service
@RequiredArgsConstructor
public class OAuthLoginStateService {

	private static final String KEY_PREFIX = "oauth:state:";

	private final StringRedisTemplate redisTemplate;
	private final JwtProperties jwtProperties;

	public void saveLoginState(String state, ProviderType providerType) {
		redisTemplate
				.opsForValue()
				.set(
						KEY_PREFIX + state,
						providerType.name(),
						jwtProperties.getOauthStateTtlDuration());
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
