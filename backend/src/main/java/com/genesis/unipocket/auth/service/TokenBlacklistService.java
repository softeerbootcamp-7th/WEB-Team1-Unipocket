package com.genesis.unipocket.auth.service;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * <b>토큰 블랙리스트 서비스</b>
 *
 * <p>
 * Redis를 사용하여 토큰을 블랙리스트에 등록하고 조회합니다. RTR 정책과 로그아웃 시 사용된 토큰을 관리합니다.
 *
 * <h3>Redis 구조:</h3>
 *
 * <ul>
 * <li>Key: {@code blacklist:token:{jti}}
 * <li>Value: {@code "true"}
 * <li>TTL: 토큰의 남은 만료 시간
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TokenBlacklistService {

	private static final String BLACKLIST_PREFIX = "blacklist:token:";

	private final RedisTemplate<String, String> redisTemplate;

	/**
	 * 토큰을 블랙리스트에 추가
	 *
	 * @param jti   JWT ID
	 * @param ttlMs 남은 만료 시간 (밀리초)
	 */
	public void addToBlacklist(String jti, long ttlMs) {
		String key = BLACKLIST_PREFIX + jti;
		long ttlSeconds = TimeUnit.MILLISECONDS.toSeconds(ttlMs);

		if (ttlSeconds > 0) {
			redisTemplate.opsForValue().set(key, "true", ttlSeconds, TimeUnit.SECONDS);
			log.debug("토큰 블랙리스트 등록: jti={}, ttl={}초", jti, ttlSeconds);
		} else {
			log.warn("TTL이 0 이하입니다. 블랙리스트 등록 건너뜀: jti={}", jti);
		}
	}

	/**
	 * 토큰이 블랙리스트에 있는지 확인
	 *
	 * @param jti JWT ID
	 * @return 블랙리스트 포함 여부
	 */
	public boolean isBlacklisted(String jti) {
		String key = BLACKLIST_PREFIX + jti;
		Boolean exists = redisTemplate.hasKey(key);
		return Boolean.TRUE.equals(exists);
	}
}
