package com.genesis.unipocket.auth.service;

import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.global.exception.TokenException;
import com.genesis.unipocket.user.command.persistence.repository.UserCommandRepository;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <b>인증 서비스</b>
 *
 * <p>
 * JWT 기반 인증을 처리합니다. 로그인, 토큰 재발급(RTR), 로그아웃 기능을 제공합니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

	private final JwtProvider jwtProvider;
	private final TokenBlacklistService blacklistService;
	private final UserCommandRepository userRepository;

	/**
	 * 로그인 - Access Token과 Refresh Token 발급
	 *
	 * @param userId 사용자 ID
	 * @return TokenPair (accessToken, refreshToken)
	 */
	@Transactional
	public TokenPair login(UUID userId) {
		// 사용자 존재 확인
		userRepository
				.findById(userId)
				.orElseThrow(() -> new TokenException(ErrorCode.USER_NOT_FOUND));

		// Access Token과 Refresh Token 발급
		String accessToken = jwtProvider.createAccessToken(userId);
		String refreshToken = jwtProvider.createRefreshToken(userId);

		log.info("로그인 성공: userId={}", userId);

		return new TokenPair(accessToken, refreshToken);
	}

	/**
	 * 토큰 재발급 (Refresh Token Rotation)
	 *
	 * <p>
	 * Refresh Token을 검증하고 새로운 Access Token과 Refresh Token을 발급합니다. 기존 Refresh Token은
	 * 블랙리스트에 등록됩니다.
	 *
	 * @param refreshToken 기존 Refresh Token
	 * @return TokenPair (새 accessToken, 새 refreshToken)
	 */
	@Transactional
	public TokenPair reissue(String refreshToken) {
		try {
			// 1. Refresh Token 파싱 및 검증
			if (!jwtProvider.validateToken(refreshToken)) {
				throw new TokenException(ErrorCode.TOKEN_INVALID);
			}

			// 2. jti 추출 및 블랙리스트 확인
			String jti = jwtProvider.getJti(refreshToken);
			if (blacklistService.isBlacklisted(jti)) {
				log.warn("블랙리스트에 등록된 토큰으로 재발급 시도: jti={}", jti);
				throw new TokenException(ErrorCode.TOKEN_BLACKLISTED);
			}

			// 3. 사용자 ID 추출
			UUID userId = jwtProvider.getUserId(refreshToken);

			// 4. 새로운 Access Token과 Refresh Token 발급
			String newAccessToken = jwtProvider.createAccessToken(userId);
			String newRefreshToken = jwtProvider.createRefreshToken(userId);

			// 5. 기존 Refresh Token을 블랙리스트에 등록 (RTR 정책)
			long remainingExpiration = jwtProvider.getRemainingExpiration(refreshToken);
			blacklistService.addToBlacklist(jti, remainingExpiration);

			log.info("토큰 재발급 성공 (RTR 적용): userId={}, old_jti={}", userId, jti);

			return new TokenPair(newAccessToken, newRefreshToken);

		} catch (ExpiredJwtException e) {
			log.warn("만료된 Refresh Token으로 재발급 시도");
			throw new TokenException(ErrorCode.TOKEN_EXPIRED);
		} catch (JwtException | IllegalArgumentException e) {
			log.warn("유효하지 않은 Refresh Token: {}", e.getMessage());
			throw new TokenException(ErrorCode.TOKEN_INVALID);
		}
	}

	/**
	 * 로그아웃 - Access Token과 Refresh Token을 블랙리스트에 등록
	 *
	 * @param accessToken  Access Token
	 * @param refreshToken Refresh Token
	 */
	@Transactional
	public void logout(String accessToken, String refreshToken) {
		try {
			// Access Token 블랙리스트 등록
			String atJti = jwtProvider.getJti(accessToken);
			long atRemaining = jwtProvider.getRemainingExpiration(accessToken);
			blacklistService.addToBlacklist(atJti, atRemaining);

			// Refresh Token 블랙리스트 등록
			String rtJti = jwtProvider.getJti(refreshToken);
			long rtRemaining = jwtProvider.getRemainingExpiration(refreshToken);
			blacklistService.addToBlacklist(rtJti, rtRemaining);

			UUID userId = jwtProvider.getUserId(accessToken);
			log.info("로그아웃 성공: userId={}", userId);

		} catch (Exception e) {
			log.warn("로그아웃 중 토큰 파싱 실패 (무시): {}", e.getMessage());
			// 로그아웃은 실패해도 큰 문제가 없으므로 예외를 던지지 않음
		}
	}

	/**
	 * 토큰 쌍 (Access Token + Refresh Token)
	 */
	public record TokenPair(String accessToken, String refreshToken) {}
}
