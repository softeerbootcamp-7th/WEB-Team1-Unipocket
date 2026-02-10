package com.genesis.unipocket.auth.command.application;

import com.genesis.unipocket.auth.common.dto.LoginResult;
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
	public LoginResult createTokens(UUID userId) {
		// AuthService를 통해 Access Token과 Refresh Token 발급
		AuthService.TokenPair tokenPair = authService.login(userId);

		log.info("JWT 토큰 발행 완료: userId={}", userId);

		return LoginResult.of(
				tokenPair.accessToken(), tokenPair.refreshToken(), accessTokenExpirationMs / 1000);
	}
}
