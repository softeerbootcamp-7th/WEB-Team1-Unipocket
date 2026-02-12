package com.genesis.unipocket.auth.command.application;

import com.genesis.unipocket.auth.common.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

/**
 * <b>JWT 토큰 관리자</b>
 *
 * <p>
 * Access Token과 Refresh Token을 생성하고 검증합니다. 각 토큰에는 고유 ID(jti)가 포함되어 블랙리스트 관리에
 * 사용됩니다.
 */
@Component
public class JwtProvider {

	private final SecretKey key;
	private final long accessTokenExpirationMs;
	private final long refreshTokenExpirationMs;

	public JwtProvider(JwtProperties jwtProperties) {
		this.key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
		this.accessTokenExpirationMs = jwtProperties.getAccessTokenExpirationMs();
		this.refreshTokenExpirationMs = jwtProperties.getRefreshTokenExpirationMs();
	}

	/**
	 * Access Token 생성
	 *
	 * @param userId 사용자 ID
	 * @return Access Token 문자열
	 */
	public String createAccessToken(UUID userId) {
		return createToken(userId, accessTokenExpirationMs, "access");
	}

	/**
	 * Refresh Token 생성
	 *
	 * @param userId 사용자 ID
	 * @return Refresh Token 문자열
	 */
	public String createRefreshToken(UUID userId) {
		return createToken(userId, refreshTokenExpirationMs, "refresh");
	}

	/**
	 * 토큰 생성 공통 로직
	 *
	 * @param userId     사용자 ID
	 * @param validityMs 유효 시간 (밀리초)
	 * @param tokenType  토큰 타입 (access/refresh)
	 * @return JWT 토큰 문자열
	 */
	private String createToken(UUID userId, long validityMs, String tokenType) {
		Date now = new Date();
		Date expiryDate = new Date(now.getTime() + validityMs);
		String jti = UUID.randomUUID().toString(); // 토큰 고유 ID

		return Jwts.builder()
				.subject(userId.toString())
				.claim("type", tokenType)
				.id(jti) // JWT ID (jti) 설정
				.issuedAt(now)
				.expiration(expiryDate)
				.signWith(key)
				.compact();
	}

	/**
	 * 토큰에서 Claims 추출
	 *
	 * @param token JWT 토큰
	 * @return Claims
	 * @throws ExpiredJwtException 토큰이 만료된 경우
	 * @throws JwtException        토큰이 유효하지 않은 경우
	 */
	public Claims parseToken(String token) {
		return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
	}

	/**
	 * 토큰에서 사용자 ID 추출
	 *
	 * @param token JWT 토큰
	 * @return 사용자 UUID
	 */
	public UUID getUserId(String token) {
		String subject = parseToken(token).getSubject();
		return UUID.fromString(subject);
	}

	/**
	 * 토큰에서 JWT ID (jti) 추출
	 *
	 * @param token JWT 토큰
	 * @return JWT ID
	 */
	public String getJti(String token) {
		return parseToken(token).getId();
	}

	/**
	 * 토큰 만료 시간 조회
	 *
	 * @param token JWT 토큰
	 * @return 만료 시간 (Date)
	 */
	public Date getExpiration(String token) {
		return parseToken(token).getExpiration();
	}

	/**
	 * 토큰이 만료되었는지 확인
	 *
	 * @param token JWT 토큰
	 * @return 만료 여부
	 */
	public boolean isTokenExpired(String token) {
		try {
			Date expiration = getExpiration(token);
			return expiration.before(new Date());
		} catch (ExpiredJwtException e) {
			return true;
		}
	}

	/**
	 * 토큰 유효성 검증 (서명 및 만료 확인)
	 *
	 * @param token JWT 토큰
	 * @return 유효 여부
	 */
	public boolean validateToken(String token) {
		try {
			parseToken(token);
			return true;
		} catch (ExpiredJwtException e) {
			// 만료된 토큰
			return false;
		} catch (JwtException | IllegalArgumentException e) {
			// 유효하지 않은 토큰
			return false;
		}
	}

	/**
	 * 토큰의 남은 만료 시간 (밀리초)
	 *
	 * @param token JWT 토큰
	 * @return 남은 시간 (밀리초)
	 */
	public long getRemainingExpiration(String token) {
		try {
			Date expiration = getExpiration(token);
			long remaining = expiration.getTime() - System.currentTimeMillis();
			return Math.max(0, remaining);
		} catch (Exception e) {
			return 0;
		}
	}
}
