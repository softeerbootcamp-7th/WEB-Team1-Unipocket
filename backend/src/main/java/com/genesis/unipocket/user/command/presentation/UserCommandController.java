package com.genesis.unipocket.user.command.presentation;

import com.genesis.unipocket.global.config.OAuth2Properties.ProviderType;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.user.command.facade.OAuthAuthorizeFacade;
import com.genesis.unipocket.user.command.facade.UserLoginFacade;
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

	@Value("${app.frontend.url:http://localhost:3000}")
	private String frontendUrl;

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

		addCookie(
				response,
				"access_token",
				loginResponse.getAccessToken(),
				loginResponse.getExpiresIn().intValue());
		addCookie(
				response,
				"refresh_token",
				loginResponse.getRefreshToken(),
				7 * 24 * 60 * 60); // 7 days

		String redirectUrl = buildRedirectUrl();
		response.sendRedirect(redirectUrl);
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
	private void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
		Cookie cookie = new Cookie(name, value);
		cookie.setPath("/");
		cookie.setMaxAge(maxAge);

		// Refresh Token은 HttpOnly 설정 (JS 접근 불가, 보안 강화)
		if ("refresh_token".equals(name)) {
			cookie.setHttpOnly(true);
		}
		// HTTPS 환경에서는 Secure 설정 권장 (개발 환경 고려하여 일단 false 또는 조건부)
		// cookie.setSecure(true);

		response.addCookie(cookie);
	}

	/**
	 * 프론트엔드 리다이렉트 URL 생성 유틸리티
	 */
	private String buildRedirectUrl() {
		return UriComponentsBuilder.fromUriString(frontendUrl)
				.path("/auth/callback")
				.build()
				.toUriString();
	}
}
