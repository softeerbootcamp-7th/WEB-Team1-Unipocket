package com.genesis.unipocket.auth.command.presentation;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.genesis.unipocket.auth.command.application.AuthService;
import com.genesis.unipocket.auth.command.application.JwtProvider;
import com.genesis.unipocket.auth.command.application.TokenBlacklistService;
import com.genesis.unipocket.auth.command.facade.OAuthAuthorizeFacade;
import com.genesis.unipocket.auth.command.facade.UserLoginFacade;
import com.genesis.unipocket.auth.common.config.JwtProperties;
import com.genesis.unipocket.auth.common.constant.AuthCookieConstants;
import com.genesis.unipocket.auth.common.dto.AuthorizeResult;
import com.genesis.unipocket.auth.common.dto.LoginResult;
import com.genesis.unipocket.global.config.OAuth2Properties;
import com.genesis.unipocket.global.util.CookieUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthCommandController.class)
class AuthCommandControllerTest {

	@Autowired private MockMvc mockMvc;

	@MockitoBean private AuthService authService;
	@MockitoBean private CookieUtil cookieUtil;
	@MockitoBean private OAuthAuthorizeFacade authorizeFacade;
	@MockitoBean private UserLoginFacade loginFacade;
	@MockitoBean private JpaMetamodelMappingContext jpaMetamodelMappingContext;
	@MockitoBean private JwtProvider jwtProvider;
	@MockitoBean private TokenBlacklistService tokenBlacklistService;
	@MockitoBean private JwtProperties jwtProperties;

	@Test
	@DisplayName("토큰 재발급 성공")
	void reissue_Success() throws Exception {
		// given
		String refreshToken = "valid_refresh_token";
		String newAccessToken = "new_access_token";
		String newRefreshToken = "new_refresh_token";

		AuthService.TokenPair tokenPair =
				new AuthService.TokenPair(newAccessToken, newRefreshToken);

		given(authService.reissue(refreshToken)).willReturn(tokenPair);

		// when & then
		mockMvc.perform(
						post("/auth/reissue")
								.cookie(
										new Cookie(
												AuthCookieConstants.REFRESH_TOKEN, refreshToken)))
				.andExpect(status().isOk());

		verify(authService).reissue(refreshToken);
		verify(cookieUtil)
				.addCookie(
						any(HttpServletResponse.class),
						eq(AuthCookieConstants.ACCESS_TOKEN),
						eq(newAccessToken),
						anyInt(),
						eq("/"));
		verify(cookieUtil)
				.addCookie(
						any(HttpServletResponse.class),
						eq(AuthCookieConstants.REFRESH_TOKEN),
						eq(newRefreshToken),
						anyInt(),
						eq("/"));
	}

	@Test
	@DisplayName("토큰 재발급 실패 - refresh_token 쿠키 누락")
	void reissue_Fail_WhenRefreshTokenMissing() throws Exception {
		mockMvc.perform(post("/auth/reissue"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("400_REFRESH_TOKEN_REQUIRED"));

		verify(authService, never()).reissue(anyString());
	}

	@Test
	@DisplayName("로그아웃 성공")
	void logout_Success() throws Exception {
		// given
		String accessToken = "valid_access_token";
		String refreshToken = "valid_refresh_token";

		// when & then
		mockMvc.perform(
						post("/auth/logout")
								.cookie(new Cookie(AuthCookieConstants.ACCESS_TOKEN, accessToken))
								.cookie(
										new Cookie(
												AuthCookieConstants.REFRESH_TOKEN, refreshToken)))
				.andExpect(status().isOk());

		verify(authService).logout(accessToken, refreshToken);
		verify(cookieUtil)
				.deleteCookie(
						any(HttpServletResponse.class),
						eq(AuthCookieConstants.ACCESS_TOKEN),
						eq("/"));
		verify(cookieUtil)
				.deleteCookie(
						any(HttpServletResponse.class),
						eq(AuthCookieConstants.REFRESH_TOKEN),
						eq("/"));
	}

	@Test
	@DisplayName("로그아웃 성공 - 쿠키 누락 시에도 200")
	void logout_Success_WhenCookiesMissing() throws Exception {
		mockMvc.perform(post("/auth/logout")).andExpect(status().isOk());

		verify(authService, never()).logout(anyString(), anyString());
		verify(cookieUtil)
				.deleteCookie(
						any(HttpServletResponse.class),
						eq(AuthCookieConstants.ACCESS_TOKEN),
						eq("/"));
		verify(cookieUtil)
				.deleteCookie(
						any(HttpServletResponse.class),
						eq(AuthCookieConstants.REFRESH_TOKEN),
						eq("/"));
	}

	@Test
	@DisplayName("OAuth2 인증 요청 리다이렉트 성공")
	void authorize_Success() throws Exception {
		// given
		String provider = "kakao";
		String authorizationUrl = "https://kauth.kakao.com/oauth/authorize";
		String state = "state_code";

		AuthorizeResult authorizeResult = new AuthorizeResult(authorizationUrl, state);

		given(authorizeFacade.authorize(OAuth2Properties.ProviderType.KAKAO))
				.willReturn(authorizeResult);

		// when & then
		mockMvc.perform(get("/auth/oauth2/authorize/{provider}", provider))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl(authorizationUrl));

		verify(authorizeFacade).authorize(OAuth2Properties.ProviderType.KAKAO);
	}

	@Test
	@DisplayName("OAuth2 콜백 처리 및 로그인 성공")
	void callback_Success() throws Exception {
		// given
		String provider = "kakao";
		String code = "auth_code";
		String state = "state_code";
		String accessToken = "new_access_token";
		String refreshToken = "new_refresh_token";
		Long expiresIn = 3600L;

		LoginResult loginResult =
				LoginResult.builder()
						.accessToken(accessToken)
						.refreshToken(refreshToken)
						.expiresIn(expiresIn)
						.build();

		given(loginFacade.login(eq(OAuth2Properties.ProviderType.KAKAO), eq(code), eq(state)))
				.willReturn(loginResult);

		// when & then
		mockMvc.perform(
						get("/auth/oauth2/callback/{provider}", provider)
								.param("code", code)
								.param("state", state))
				.andExpect(status().is3xxRedirection());

		verify(loginFacade).login(eq(OAuth2Properties.ProviderType.KAKAO), eq(code), eq(state));

		verify(cookieUtil)
				.addCookie(
						any(HttpServletResponse.class),
						eq(AuthCookieConstants.ACCESS_TOKEN),
						eq(accessToken),
						eq(expiresIn.intValue()),
						eq("/"));

		verify(cookieUtil)
				.addCookie(
						any(HttpServletResponse.class),
						eq(AuthCookieConstants.REFRESH_TOKEN),
						eq(refreshToken),
						anyInt(),
						eq("/"));
	}
}
