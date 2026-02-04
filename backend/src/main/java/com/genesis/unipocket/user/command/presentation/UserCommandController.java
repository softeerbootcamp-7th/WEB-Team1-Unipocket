package com.genesis.unipocket.user.command.presentation;

import com.genesis.unipocket.global.config.OAuth2Properties.ProviderType;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.user.command.facade.OAuthAuthorizeFacade;
import com.genesis.unipocket.user.command.facade.UserLoginFacade;
import com.genesis.unipocket.user.command.presentation.dto.request.LogoutRequest;
import com.genesis.unipocket.user.command.presentation.dto.request.ReissueRequest;
import com.genesis.unipocket.user.command.presentation.dto.response.AuthorizeResponse;
import com.genesis.unipocket.user.command.presentation.dto.response.LoginResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * <b>사용자 Command Controller</b>
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserCommandController {

	private final OAuthAuthorizeFacade authorizeFacade;
	private final UserLoginFacade loginFacade;
	private final com.genesis.unipocket.global.auth.AuthService authService;

	@Value("${app.frontend.url:http://localhost:3000}")
	private String frontendUrl;

	@Value("${jwt.access-token-expiration}")
	private long accessTokenExpirationMs;

	/**
	 * OAuth 인증 시작: 소셜 로그인 페이지로 리다이렉트
	 */
	@GetMapping("/oauth2/authorize/{provider}")
	public void authorize(@PathVariable("provider") String provider, HttpServletResponse response)
			throws IOException {

		log.info("OAuth authorize request for provider: {}", provider);
		ProviderType providerType = getProviderType(provider);

		AuthorizeResponse authResponse = authorizeFacade.authorize(providerType);
		response.sendRedirect(authResponse.getAuthorizationUrl());
	}

	/**
	 * OAuth 콜백 처리: 로그인 완료 후 프론트엔드로 리다이렉트
	 */
	@GetMapping("/oauth2/callback/{provider}")
	public void callback(
			@PathVariable("provider") String provider,
			@RequestParam("code") String code,
			@RequestParam(value = "state", required = false) String state,
			HttpServletResponse response)
			throws IOException {

		log.info("OAuth callback received for provider: {}", provider);
		ProviderType providerType = getProviderType(provider);

		LoginResponse loginResponse = loginFacade.login(providerType, code, state);

		// Access Token 쿠키 저장
		addCookie(
				response,
				"access_token",
				loginResponse.getAccessToken(),
				loginResponse.getExpiresIn().intValue());

		// Refresh Token 쿠키 저장 (10일)
		addCookie(response, "refresh_token", loginResponse.getRefreshToken(), 10 * 24 * 60 * 60);

		String redirectUrl = createRedirectUrl();
		response.sendRedirect(redirectUrl);
	}

	/**
	 * 토큰 재발급 (Refresh Token Rotation)
	 */
	@PostMapping("/reissue")
	public LoginResponse reissue(@RequestBody ReissueRequest request) {
		log.info("토큰 재발급 요청");

		com.genesis.unipocket.global.auth.AuthService.TokenPair tokenPair = authService
				.reissue(request.getRefreshToken());

		return LoginResponse.of(
				tokenPair.accessToken(),
				tokenPair.refreshToken(),
				null, // userId는 재발급 시 불필요
				accessTokenExpiresIn());
	}

	/**
	 * 로그아웃 (토큰 블랙리스트 등록)
	 */
	@PostMapping("/logout")
	public void logout(@RequestBody LogoutRequest request, HttpServletResponse response) {
		log.info("로그아웃 요청");

		authService.logout(request.getAccessToken(), request.getRefreshToken());

		// 쿠키 삭제
		addCookie(response, "access_token", "", 0);
		addCookie(response, "refresh_token", "", 0);
	}

	/**
	 * Access Token 만료 시간 (초) 계산
	 */
	private long accessTokenExpiresIn() {
		return accessTokenExpirationMs / 1000;
	}

	/**
	 * Provider 문자열을 Enum으로 변환하고 유효성 검증
	 */
	private ProviderType getProviderType(String provider) {
		try {
			return ProviderType.valueOf(provider.toUpperCase());
		} catch (IllegalArgumentException e) {
			log.error("Invalid OAuth provider: {}", provider);
			throw new BusinessException(ErrorCode.INVALID_OAUTH_PROVIDER);
		}
	}

	/**
	 * 쿠키 추가 유틸리티
	 */
	/**
	 * 쿠키 추가 유틸리티
	 */
	private void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
		Cookie cookie = new Cookie(name, value);
		cookie.setPath("/");
		cookie.setMaxAge(maxAge);
		cookie.setHttpOnly(true); // XSS 방지

		// HTTPS 환경에서는 Secure 설정 권장 (개발 환경 고려하여 일단 false 또는 조건부)
		// cookie.setSecure(true);

		response.addCookie(cookie);
	}

	/**
	 * 프론트엔드 리다이렉트 URL 생성 유틸리티
	 */
	private String createRedirectUrl() {
		return UriComponentsBuilder.fromUriString(frontendUrl).path("/home").build().toUriString();
	}
}
