package com.genesis.unipocket.user.command.facade;

import com.genesis.unipocket.global.config.OAuth2Properties.ProviderType;
import com.genesis.unipocket.user.command.presentation.dto.response.LoginResponse;
import com.genesis.unipocket.user.command.service.UserCommandService;
import com.genesis.unipocket.user.command.service.OAuthLoginStateService;
import com.genesis.unipocket.user.command.service.oauth.OAuthProviderFactory;
import com.genesis.unipocket.user.command.service.oauth.OAuthProviderService;
import com.genesis.unipocket.user.command.service.oauth.dto.OAuthTokenResponse;
import com.genesis.unipocket.user.command.service.oauth.dto.OAuthUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <b>사용자 로그인 Facade</b>
 * @author bluefishez
 * @since 2026-01-29
 */
@Service
@RequiredArgsConstructor
public class UserLoginFacade {

    private final OAuthProviderFactory providerFactory;
    private final OAuthLoginStateService loginStateService;
    private final UserCommandService userCommandService;

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
        return userCommandService.loginOrRegister(userInfo, providerType);
    }
}