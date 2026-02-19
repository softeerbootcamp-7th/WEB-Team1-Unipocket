package com.genesis.unipocket.auth.common.config;

import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * <b>JWT 및 토큰 관련 타입 안전 설정</b>
 *
 * <p>
 * YML에서 Duration 형식(30m, 10d 등)으로 설정하면 Spring Boot가 자동 변환합니다.
 *
 * @author 김동균
 * @since 2026-02-11
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

	private String secret;

	/** Access Token 만료 시간 (예: 30m) */
	private Duration accessTokenExpiration;

	/** Refresh Token 만료 시간 (예: 10d) */
	private Duration refreshTokenExpiration;

	/** OAuth State TTL (예: 10m) */
	private Duration oauthStateTtl;

	/** Access Token 만료 시간 (밀리초) */
	public long getAccessTokenExpirationMs() {
		return accessTokenExpiration.toMillis();
	}

	/** Refresh Token 만료 시간 (밀리초) */
	public long getRefreshTokenExpirationMs() {
		return refreshTokenExpiration.toMillis();
	}

	/** Access Token 만료 시간 (초) */
	public int getAccessTokenExpirationSeconds() {
		return Math.toIntExact(accessTokenExpiration.toSeconds());
	}

	/** Refresh Token 만료 시간 (초) */
	public int getRefreshTokenExpirationSeconds() {
		return Math.toIntExact(refreshTokenExpiration.toSeconds());
	}

	/** OAuth State TTL (Duration) */
	public Duration getOauthStateTtlDuration() {
		return oauthStateTtl;
	}
}
