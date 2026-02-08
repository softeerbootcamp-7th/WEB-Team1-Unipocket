package com.genesis.unipocket.auth.service;

import com.genesis.unipocket.user.dto.response.LoginResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {

	private final AuthService authService;

	@Value("${jwt.access-token-expiration}")
	private long accessTokenExpirationMs;

	@Transactional
	public LoginResponse createTokens(UUID userId) {
		// AuthService를 통해 Access Token과 Refresh Token 발급
		AuthService.TokenPair tokenPair = authService.login(userId);

		log.info("JWT 토큰 발행 완료: userId={}", userId);

		return LoginResponse.of(
				tokenPair.accessToken(),
				tokenPair.refreshToken(),
				userId,
				accessTokenExpirationMs / 1000);
	}
}
