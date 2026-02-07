package com.genesis.unipocket.user.command.presentation;

import com.genesis.unipocket.global.config.OAuth2Properties.ProviderType;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.global.util.CookieUtil;
import com.genesis.unipocket.user.command.facade.OAuthAuthorizeFacade;
import com.genesis.unipocket.user.command.facade.UserLoginFacade;
import com.genesis.unipocket.user.command.presentation.dto.response.AuthorizeResponse;
import com.genesis.unipocket.user.command.presentation.dto.response.LoginResponse;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
	private final CookieUtil cookieUtil;

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
	 * OAuth 콜백 처리: 로그인 완료 후 JSON 응답 반환 (SPA 방식)
	 */
	@GetMapping("/oauth2/callback/{provider}")
	public ResponseEntity<LoginResponse> callback(
			@PathVariable("provider") String provider,
			@RequestParam("code") String code,
			@RequestParam(value = "state", required = false) String state,
			HttpServletResponse response) {

		log.info("OAuth callback received for provider: {}", provider);
		ProviderType providerType = getProviderType(provider);

		LoginResponse loginResponse = loginFacade.login(providerType, code, state);

		// Access Token 쿠키 저장 (보안 강화 및 프론트 편의성)
		cookieUtil.addCookie(
				response,
				"access_token",
				loginResponse.getAccessToken(),
				loginResponse.getExpiresIn().intValue());

		// Refresh Token 쿠키 저장 (10일)
		cookieUtil.addCookie(
				response, "refresh_token", loginResponse.getRefreshToken(), 10 * 24 * 60 * 60);

		return ResponseEntity.ok(loginResponse);
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
}
