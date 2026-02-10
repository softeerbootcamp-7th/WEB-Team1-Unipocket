package com.genesis.unipocket.auth.controller;

import com.genesis.unipocket.auth.facade.OAuthAuthorizeFacade;
import com.genesis.unipocket.auth.facade.UserLoginFacade;
import com.genesis.unipocket.auth.service.AuthService;
import com.genesis.unipocket.global.config.OAuth2Properties;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.global.util.CookieUtil;
import com.genesis.unipocket.user.dto.request.LogoutRequest;
import com.genesis.unipocket.user.dto.request.ReissueRequest;
import com.genesis.unipocket.user.dto.response.AuthorizeResponse;
import com.genesis.unipocket.user.dto.response.LoginResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * <b>인증 컨트롤러</b>
 * <p>
 * 토큰 재발급, 로그아웃 등 인증 세션 관리 기능을 담당합니다.
 * </p>
 */
@Tag(name = "인증 기능")
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;
	private final CookieUtil cookieUtil;
	private final OAuthAuthorizeFacade authorizeFacade;
	private final UserLoginFacade loginFacade;

	@Value("${jwt.access-token-expiration}")
	private long accessTokenExpirationMs;

	@Value("${app.frontend.url}")
	private String frontendUrl;

	@Value("${app.frontend.allowed-origins:}")
	private String allowedOrigins;

	/**
	 * 토큰 재발급 (Refresh Token Rotation)
	 */
	@PostMapping("/reissue")
	public LoginResponse reissue(
			@RequestBody ReissueRequest request, HttpServletResponse response) {
		log.info("토큰 재발급 요청");

		AuthService.TokenPair tokenPair = authService.reissue(request.getRefreshToken());

		// Access Token 쿠키 갱신
		cookieUtil.addCookie(
				response,
				"access_token",
				tokenPair.accessToken(),
				(int) (accessTokenExpirationMs / 1000));

		// Refresh Token 쿠키 갱신
		cookieUtil.addCookie(
				response, "refresh_token", tokenPair.refreshToken(), 10 * 24 * 60 * 60);

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
		cookieUtil.deleteCookie(response, "access_token");
		cookieUtil.deleteCookie(response, "refresh_token");
	}

	/**
	 * OAuth 인증 시작: 소셜 로그인 페이지로 리다이렉트
	 */
	@GetMapping("/oauth2/authorize/{provider}")
	public void authorize(@PathVariable("provider") String provider, HttpServletResponse response)
			throws IOException {

		log.info("OAuth authorize request for provider: {}", provider);
		OAuth2Properties.ProviderType providerType = getProviderType(provider);

		AuthorizeResponse authResponse = authorizeFacade.authorize(providerType);
		response.sendRedirect(authResponse.getAuthorizationUrl());
	}

	/**
	 * OAuth 콜백 처리: 로그인 완료 후 JSON 응답 반환 (SPA 방식)
	 */
	@GetMapping("/oauth2/callback/{provider}")
	public void callback(
			@PathVariable("provider") String provider,
			@RequestParam("code") String code,
			@RequestParam(value = "state", required = false) String state,
			HttpServletRequest request,
			HttpServletResponse response)
			throws IOException {

		log.info("OAuth callback received for provider: {}", provider);
		OAuth2Properties.ProviderType providerType = getProviderType(provider);

		LoginResponse loginResponse = loginFacade.login(providerType, code, state);

		// Access Token 쿠키 저장
		cookieUtil.addCookie(
				response,
				"access_token",
				loginResponse.getAccessToken(),
				loginResponse.getExpiresIn().intValue());

		// Refresh Token 쿠키 저장 (10일)
		cookieUtil.addCookie(
				response, "refresh_token", loginResponse.getRefreshToken(), 10 * 24 * 60 * 60);

		String redirectUrl = createRedirectUrl(request);
		response.sendRedirect(redirectUrl);
	}

	/**
	 * Access Token 만료 시간 (초) 계산
	 */
	private long accessTokenExpiresIn() {
		return accessTokenExpirationMs / 1000;
	}

	/**
	 * 프론트엔드 리다이렉트 URL 생성 유틸리티
	 */
	private String createRedirectUrl(HttpServletRequest request) {
		String baseUrl = resolveRedirectBaseUrl(request);
		return UriComponentsBuilder.fromUriString(baseUrl).path("/home").build().toUriString();
	}

	private String resolveRedirectBaseUrl(HttpServletRequest request) {
		Set<String> allowed = parseAllowedOrigins();
		String origin = request.getHeader("Origin");
		if (origin != null && allowed.contains(origin)) {
			return origin;
		}

		String referer = request.getHeader("Referer");
		if (referer != null) {
			String refererOrigin = extractOrigin(referer);
			if (refererOrigin != null && allowed.contains(refererOrigin)) {
				return refererOrigin;
			}
		}

		return frontendUrl;
	}

	private Set<String> parseAllowedOrigins() {
		if (allowedOrigins == null || allowedOrigins.isBlank()) {
			return Set.of();
		}
		return new HashSet<>(
				Arrays.stream(allowedOrigins.split(","))
						.map(String::trim)
						.filter(s -> !s.isEmpty())
						.toList());
	}

	private String extractOrigin(String url) {
		try {
			URI uri = URI.create(url);
			if (uri.getScheme() == null || uri.getHost() == null) {
				return null;
			}
			int port = uri.getPort();
			if (port == -1 || port == uri.toURL().getDefaultPort()) {
				return uri.getScheme() + "://" + uri.getHost();
			}
			return uri.getScheme() + "://" + uri.getHost() + ":" + port;
		} catch (IllegalArgumentException | MalformedURLException e) {
			return null;
		}
	}

	/**
	 * Provider 문자열을 Enum으로 변환하고 유효성 검증
	 */
	private OAuth2Properties.ProviderType getProviderType(String provider) {
		try {
			return OAuth2Properties.ProviderType.valueOf(provider.toUpperCase());
		} catch (IllegalArgumentException e) {
			log.error("Invalid OAuth provider: {}", provider);
			throw new BusinessException(ErrorCode.INVALID_OAUTH_PROVIDER);
		}
	}
}
