package com.genesis.unipocket.auth.controller;

import com.genesis.unipocket.auth.facade.OAuthAuthorizeFacade;
import com.genesis.unipocket.auth.facade.UserLoginFacade;
import com.genesis.unipocket.auth.service.AuthService;
import com.genesis.unipocket.auth.service.JwtProvider;
import com.genesis.unipocket.global.config.OAuth2Properties.ProviderType; // Enum 직접 임포트
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.global.util.CookieUtil;
import com.genesis.unipocket.user.dto.response.AuthorizeResponse;
import com.genesis.unipocket.user.dto.response.LoginResponse;
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
 * 토큰 재발급, 로그아웃 및 OAuth2 인증 흐름을 관리합니다.
 * </p>
 *
 * @author 김동균
 * 수정 이력
 * 2026-02-11 log.info 단계 변경, JwtProvider 사용하여 설정값 제대로 반영되도록 적용
 */
@Tag(name = "인증 기능")
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;
	private final CookieUtil cookieUtil;
	private final JwtProvider jwtProvider;
	private final OAuthAuthorizeFacade authorizeFacade;
	private final UserLoginFacade loginFacade;

	@Value("${app.frontend.url}")
	private String frontendDefaultUrl;

	/**
	 * 토큰 재발급 (Refresh Token Rotation)
	 */
	@PostMapping("/reissue")
	public ResponseEntity<Void> reissue(
			@Parameter(hidden = true) @CookieValue(value = "refresh_token") String refreshToken,
			HttpServletResponse response) {

		log.debug("토큰 재발급 요청");

		AuthService.TokenPair tokenPair = authService.reissue(refreshToken);

		updateAuthCookies(response, tokenPair.accessToken(), tokenPair.refreshToken());

		return ResponseEntity.ok().build();
	}

	/**
	 * 로그아웃 (토큰 블랙리스트 등록 및 쿠키 삭제)
	 */
	@PostMapping("/logout")
	public void logout(
			@Parameter(hidden = true) @CookieValue(value = "access_token") String accessToken,
			@Parameter(hidden = true) @CookieValue(value = "refresh_token") String refreshToken,
			HttpServletResponse response) {

		log.debug("로그아웃 요청");

		authService.logout(accessToken, refreshToken);

		cookieUtil.deleteCookie(response, "access_token");
		cookieUtil.deleteCookie(response, "refresh_token");
	}

	/**
	 * OAuth 인증 시작: 소셜 로그인 페이지로 리다이렉트
	 */
	@GetMapping("/oauth2/authorize/{provider}")
	public void authorize(@PathVariable String provider, HttpServletResponse response)
			throws IOException {

		log.debug("OAuth authorize request for provider: {}", provider);

		ProviderType providerType = validateAndGetProviderType(provider);

		AuthorizeResponse authResponse = authorizeFacade.authorize(providerType);

		response.sendRedirect(authResponse.getAuthorizationUrl());
	}

	/**
	 * OAuth 콜백 처리: 로그인 완료 후 프론트엔드 홈으로 리다이렉트
	 */
	@GetMapping("/oauth2/callback/{provider}")
	public void callback(
			@PathVariable String provider,
			@RequestParam("code") String code,
			@RequestParam(value = "state", required = false) String state,
			HttpServletResponse response)
			throws IOException {

		log.debug("OAuth callback received for provider: {}", provider);
		ProviderType providerType = validateAndGetProviderType(provider);

		LoginResponse loginResponse = loginFacade.login(providerType, code, state);

		updateAuthCookies(
				response, loginResponse.getAccessToken(), loginResponse.getRefreshToken());

		// 보안상 헤더에 의존하지 않고 설정된 프론트엔드 주소로 이동
		response.sendRedirect(getFinalRedirectUrl());
	}

	/**
	 * 공통 쿠키 업데이트 유틸리티
	 */
	private void updateAuthCookies(
			HttpServletResponse response, String accessToken, String refreshToken) {
		cookieUtil.addCookie(
				response,
				"access_token",
				accessToken,
				jwtProvider.getAccessTokenExpirationAsSeconds());
		cookieUtil.addCookie(
				response,
				"refresh_token",
				refreshToken,
				jwtProvider.getRefreshTokenExpirationAsSeconds());
	}

	/**
	 * 최종 리다이렉트 URL 생성 (설정된 frontendDefaultUrl 기반)
	 */
	private String getFinalRedirectUrl() {
		return UriComponentsBuilder.fromUriString(frontendDefaultUrl)
				.path("/home")
				.build()
				.toUriString();
	}

	/**
	 * Provider 유효성 검증 및 Enum 변환 (엄격한 검증)
	 */
	private ProviderType validateAndGetProviderType(String provider) {
		if (provider == null || provider.isBlank()) {
			throw new BusinessException(ErrorCode.INVALID_OAUTH_PROVIDER);
		}

		try {
			return ProviderType.valueOf(provider.toUpperCase());
		} catch (IllegalArgumentException e) {
			log.error("지원하지 않는 OAuth 제공자: {}", provider);
			throw new BusinessException(ErrorCode.INVALID_OAUTH_PROVIDER);
		}
	}
}
