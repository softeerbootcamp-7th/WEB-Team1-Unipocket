package com.genesis.unipocket.auth.command.presentation;

import com.genesis.unipocket.auth.command.application.AuthService;
import com.genesis.unipocket.auth.command.facade.OAuthAuthorizeFacade;
import com.genesis.unipocket.auth.command.facade.UserLoginFacade;
import com.genesis.unipocket.auth.common.config.JwtProperties;
import com.genesis.unipocket.auth.common.dto.AuthorizeResult;
import com.genesis.unipocket.auth.common.dto.LoginResult;
import com.genesis.unipocket.global.config.OAuth2Properties;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.global.util.CookieUtil;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthCommandController {

	private static final String ACCESS_TOKEN_COOKIE_PATH = "/";
	private static final String REFRESH_TOKEN_COOKIE_PATH = "/auth";

	private final AuthService authService;
	private final CookieUtil cookieUtil;
	private final OAuthAuthorizeFacade authorizeFacade;
	private final UserLoginFacade loginFacade;
	private final JwtProperties jwtProperties;

	@Value("${app.frontend.url}")
	private String frontendUrl;

	/**
	 * 토큰 재발급 (Refresh Token Rotation)
	 */
	@PostMapping("/reissue")
	public ResponseEntity<Void> reissue(
			@Parameter(hidden = true) @CookieValue("refresh_token") String refreshToken,
			HttpServletResponse response) {
		log.info("토큰 재발급 요청");

		AuthService.TokenPair tokenPair = authService.reissue(refreshToken);

		// Access Token 쿠키 갱신
		cookieUtil.addCookie(
				response,
				"access_token",
				tokenPair.accessToken(),
				jwtProperties.getAccessTokenExpirationSeconds(),
				ACCESS_TOKEN_COOKIE_PATH);

		// Refresh Token 쿠키 갱신
		cookieUtil.addCookie(
				response,
				"refresh_token",
				tokenPair.refreshToken(),
				jwtProperties.getRefreshTokenExpirationSeconds(),
				REFRESH_TOKEN_COOKIE_PATH);

		return ResponseEntity.ok().build();
	}

	/**
	 * 로그아웃 (토큰 블랙리스트 등록)
	 */
	@PostMapping("/logout")
	public void logout(
			@Parameter(hidden = true) @CookieValue("access_token") String accessToken,
			@Parameter(hidden = true) @CookieValue("refresh_token") String refreshToken,
			HttpServletResponse response) {
		log.info("로그아웃 요청");

		authService.logout(accessToken, refreshToken);

		// 쿠키 삭제
		cookieUtil.deleteCookie(response, "access_token", ACCESS_TOKEN_COOKIE_PATH);
		cookieUtil.deleteCookie(response, "refresh_token", REFRESH_TOKEN_COOKIE_PATH);
	}

	/**
	 * OAuth 인증 시작: 소셜 로그인 페이지로 리다이렉트
	 */
	@GetMapping("/oauth2/authorize/{provider}")
	public void authorize(@PathVariable String provider, HttpServletResponse response)
			throws IOException {

		log.info("OAuth authorize request for provider: {}", provider);
		OAuth2Properties.ProviderType providerType = getProviderType(provider);

		AuthorizeResult authResponse = authorizeFacade.authorize(providerType);
		response.sendRedirect(authResponse.authorizationUrl());
	}

	/**
	 * OAuth 콜백 처리: 로그인 완료 후 JSON 응답 반환 (SPA 방식)
	 */
	@GetMapping("/oauth2/callback/{provider}")
	public void callback(
			@PathVariable String provider,
			@RequestParam("code") String code,
			@RequestParam(value = "state", required = false) String state,
			HttpServletResponse response)
			throws IOException {

		log.info("OAuth callback received for provider: {}", provider);
		OAuth2Properties.ProviderType providerType = getProviderType(provider);

		LoginResult loginResponse = loginFacade.login(providerType, code, state);

		// Access Token 쿠키 저장
		cookieUtil.addCookie(
				response,
				"access_token",
				loginResponse.getAccessToken(),
				loginResponse.getExpiresIn().intValue(),
				ACCESS_TOKEN_COOKIE_PATH);

		// Refresh Token 쿠키 저장
		cookieUtil.addCookie(
				response,
				"refresh_token",
				loginResponse.getRefreshToken(),
				jwtProperties.getRefreshTokenExpirationSeconds(),
				REFRESH_TOKEN_COOKIE_PATH);

		String redirectUrl = createRedirectUrl();
		response.sendRedirect(redirectUrl);
	}

	/**
	 * 프론트엔드 리다이렉트 URL 생성 유틸리티
	 */
	private String createRedirectUrl() {
		return UriComponentsBuilder.fromUriString(frontendUrl).path("/home").build().toUriString();
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
