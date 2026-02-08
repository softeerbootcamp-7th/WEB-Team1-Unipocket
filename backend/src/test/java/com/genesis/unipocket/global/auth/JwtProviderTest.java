package com.genesis.unipocket.global.auth;

import static org.assertj.core.api.Assertions.*;

import com.genesis.unipocket.auth.service.JwtProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import java.util.Date;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * JwtProvider 단위 테스트
 */
@DisplayName("JwtProvider 단위 테스트")
class JwtProviderTest {

	private JwtProvider jwtProvider;
	private final String testSecret =
			"test-secret-key-must-be-at-least-32-characters-long-for-hs256";
	private final long accessTokenExpiration = 1800000L; // 30분
	private final long refreshTokenExpiration = 864000000L; // 10일

	@BeforeEach
	void setUp() {
		jwtProvider = new JwtProvider(testSecret, accessTokenExpiration, refreshTokenExpiration);
	}

	@Test
	@DisplayName("Access Token 생성 - 성공")
	void createAccessToken_Success() {
		// given
		UUID userId = UUID.randomUUID();

		// when
		String token = jwtProvider.createAccessToken(userId);

		// then
		assertThat(token).isNotNull();
		assertThat(token).isNotEmpty();
		assertThat(token.split("\\.")).hasSize(3); // JWT는 3부분으로 구성
	}

	@Test
	@DisplayName("Refresh Token 생성 - 성공")
	void createRefreshToken_Success() {
		// given
		UUID userId = UUID.randomUUID();

		// when
		String token = jwtProvider.createRefreshToken(userId);

		// then
		assertThat(token).isNotNull();
		assertThat(token).isNotEmpty();
		assertThat(token.split("\\.")).hasSize(3);
	}

	@Test
	@DisplayName("토큰에서 사용자 ID 추출 - 성공")
	void getUserId_Success() {
		// given
		UUID userId = UUID.randomUUID();
		String token = jwtProvider.createAccessToken(userId);

		// when
		UUID extractedUserId = jwtProvider.getUserId(token);

		// then
		assertThat(extractedUserId).isEqualTo(userId);
	}

	@Test
	@DisplayName("토큰에서 JWT ID (jti) 추출 - 성공")
	void getJti_Success() {
		// given
		UUID userId = UUID.randomUUID();
		String token = jwtProvider.createAccessToken(userId);

		// when
		String jti = jwtProvider.getJti(token);

		// then
		assertThat(jti).isNotNull();
		assertThat(jti).isNotEmpty();
		// UUID 형식인지 확인
		assertThatCode(() -> UUID.fromString(jti)).doesNotThrowAnyException();
	}

	@Test
	@DisplayName("토큰에 고유한 jti가 포함됨")
	void tokenContainsUniqueJti() {
		// given
		UUID userId = UUID.randomUUID();

		// when
		String token1 = jwtProvider.createAccessToken(userId);
		String token2 = jwtProvider.createAccessToken(userId);

		String jti1 = jwtProvider.getJti(token1);
		String jti2 = jwtProvider.getJti(token2);

		// then
		assertThat(jti1).isNotEqualTo(jti2); // 매번 다른 jti 생성
	}

	@Test
	@DisplayName("토큰 파싱 - 성공")
	void parseToken_Success() {
		// given
		UUID userId = UUID.randomUUID();
		String token = jwtProvider.createAccessToken(userId);

		// when
		Claims claims = jwtProvider.parseToken(token);

		// then
		assertThat(claims).isNotNull();
		assertThat(claims.getSubject()).isEqualTo(userId.toString());
		assertThat(claims.get("type")).isEqualTo("access");
		assertThat(claims.getId()).isNotNull();
	}

	@Test
	@DisplayName("Refresh Token 파싱 - type 확인")
	void parseRefreshToken_TypeCheck() {
		// given
		UUID userId = UUID.randomUUID();
		String token = jwtProvider.createRefreshToken(userId);

		// when
		Claims claims = jwtProvider.parseToken(token);

		// then
		assertThat(claims.get("type")).isEqualTo("refresh");
	}

	@Test
	@DisplayName("토큰 검증 - 유효한 토큰")
	void validateToken_ValidToken() {
		// given
		UUID userId = UUID.randomUUID();
		String token = jwtProvider.createAccessToken(userId);

		// when
		boolean isValid = jwtProvider.validateToken(token);

		// then
		assertThat(isValid).isTrue();
	}

	@Test
	@DisplayName("토큰 검증 - 잘못된 서명")
	void validateToken_InvalidSignature() {
		// given
		UUID userId = UUID.randomUUID();
		String token = jwtProvider.createAccessToken(userId);
		String tamperedToken = token.substring(0, token.length() - 5) + "XXXXX";

		// when
		boolean isValid = jwtProvider.validateToken(tamperedToken);

		// then
		assertThat(isValid).isFalse();
	}

	@Test
	@DisplayName("토큰 검증 - 만료된 토큰")
	void validateToken_ExpiredToken() throws InterruptedException {
		// given - 1ms 만료 시간으로 토큰 생성
		JwtProvider shortLivedProvider = new JwtProvider(testSecret, 1L, 1L);
		UUID userId = UUID.randomUUID();
		String token = shortLivedProvider.createAccessToken(userId);

		// when - 토큰이 만료될 때까지 대기
		Thread.sleep(10);
		boolean isValid = jwtProvider.validateToken(token);

		// then
		assertThat(isValid).isFalse();
	}

	@Test
	@DisplayName("토큰 만료 확인 - 만료되지 않은 토큰")
	void isTokenExpired_NotExpired() {
		// given
		UUID userId = UUID.randomUUID();
		String token = jwtProvider.createAccessToken(userId);

		// when
		boolean isExpired = jwtProvider.isTokenExpired(token);

		// then
		assertThat(isExpired).isFalse();
	}

	@Test
	@DisplayName("토큰 만료 시간 조회 - 성공")
	void getExpiration_Success() {
		// given
		UUID userId = UUID.randomUUID();
		String token = jwtProvider.createAccessToken(userId);

		// when
		Date expiration = jwtProvider.getExpiration(token);

		// then
		assertThat(expiration).isNotNull();
		assertThat(expiration).isAfter(new Date());
	}

	@Test
	@DisplayName("남은 만료 시간 계산 - 성공")
	void getRemainingExpiration_Success() {
		// given
		UUID userId = UUID.randomUUID();
		String token = jwtProvider.createAccessToken(userId);

		// when
		long remaining = jwtProvider.getRemainingExpiration(token);

		// then
		assertThat(remaining).isGreaterThan(0);
		assertThat(remaining).isLessThanOrEqualTo(accessTokenExpiration);
	}

	@Test
	@DisplayName("잘못된 형식의 토큰 파싱 시 예외 발생")
	void parseToken_InvalidFormat() {
		// given
		String invalidToken = "invalid.token.format";

		// when & then
		assertThatThrownBy(() -> jwtProvider.parseToken(invalidToken))
				.isInstanceOf(JwtException.class);
	}

	@Test
	@DisplayName("Access Token과 Refresh Token의 만료 시간이 다름")
	void differentExpirationForAccessAndRefreshToken() {
		// given
		UUID userId = UUID.randomUUID();

		// when
		String accessToken = jwtProvider.createAccessToken(userId);
		String refreshToken = jwtProvider.createRefreshToken(userId);

		long atRemaining = jwtProvider.getRemainingExpiration(accessToken);
		long rtRemaining = jwtProvider.getRemainingExpiration(refreshToken);

		// then
		assertThat(rtRemaining).isGreaterThan(atRemaining);
	}
}
