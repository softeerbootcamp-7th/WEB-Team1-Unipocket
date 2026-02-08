package com.genesis.unipocket.auth.facade;

import com.genesis.unipocket.auth.service.OAuthLoginStateService;
import com.genesis.unipocket.auth.service.oauth.OAuthProviderFactory;
import com.genesis.unipocket.auth.service.oauth.OAuthProviderService;
import com.genesis.unipocket.global.config.OAuth2Properties.ProviderType;
import com.genesis.unipocket.user.dto.common.oauth.OAuthTokenResponse;
import com.genesis.unipocket.user.dto.common.oauth.OAuthUserInfo;
import com.genesis.unipocket.user.dto.response.LoginResponse;
import com.genesis.unipocket.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <b>사용자 로그인 Facade</b>
 * @author 김동균
 * @since 2026-01-30
 */
@Service
@RequiredArgsConstructor
public class UserLoginFacade {

	private final OAuthProviderFactory providerFactory;
	private final OAuthLoginStateService loginStateService;
	private final UserService userService;

	@Transactional
	public LoginResponse login(ProviderType providerType, String code, String state) {
		// 1. State 검증
		loginStateService.validateState(state, providerType);

		// 2. Provider Service 조회
		OAuthProviderService provider = providerFactory.getProvider(providerType);

		// 3. Access Token 발급
		OAuthTokenResponse tokenResponse = provider.getAccessToken(code);

		// 4. 사용자 정보 조회
		OAuthUserInfo userInfo = provider.getUserInfo(tokenResponse.getAccessToken());

		// 5. 사용자 생성 또는 조회 및 JWT 발급
		return userService.loginOrRegister(userInfo, providerType);
	}
}
