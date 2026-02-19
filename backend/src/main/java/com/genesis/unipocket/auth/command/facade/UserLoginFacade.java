package com.genesis.unipocket.auth.command.facade;

import com.genesis.unipocket.auth.command.application.OAuthLoginStateService;
import com.genesis.unipocket.auth.command.application.oauth.OAuthProviderFactory;
import com.genesis.unipocket.auth.command.application.oauth.OAuthProviderService;
import com.genesis.unipocket.auth.command.facade.port.UserLoginProcessor;
import com.genesis.unipocket.auth.command.facade.port.dto.UserLoginRequest;
import com.genesis.unipocket.auth.common.dto.LoginResult;
import com.genesis.unipocket.auth.common.dto.oauth.OAuthTokenResponse;
import com.genesis.unipocket.auth.common.dto.oauth.OAuthUserInfo;
import com.genesis.unipocket.global.config.OAuth2Properties.ProviderType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <b>사용자 로그인 Facade</b>
 *
 * @author 김동균
 * @since 2026-01-30
 */
@Service
@RequiredArgsConstructor
public class UserLoginFacade {

	private final OAuthProviderFactory providerFactory;
	private final OAuthLoginStateService loginStateService;
	private final UserLoginProcessor userLoginProcessor;

	@Transactional
	public LoginResult login(ProviderType providerType, String code, String state) {
		// 1. State 검증
		loginStateService.validateState(state, providerType);

		// 2. Provider Service 조회
		OAuthProviderService provider = providerFactory.getProvider(providerType);

		// 3. Access Token 발급
		OAuthTokenResponse tokenResponse = provider.getAccessToken(code);

		// 4. 사용자 정보 조회
		OAuthUserInfo userInfo = provider.getUserInfo(tokenResponse.getAccessToken());

		// 5. 사용자 생성 또는 조회 및 JWT 발급
		UserLoginRequest request =
				UserLoginRequest.of(
						providerType,
						userInfo.getProviderId(),
						userInfo.getEmail(),
						userInfo.getName(),
						userInfo.getProfileImageUrl());
		return userLoginProcessor.loginOrRegister(request);
	}
}
