package com.genesis.unipocket.auth.service.oauth;

import com.genesis.unipocket.user.common.dto.oauth.OAuthTokenResponse;
import com.genesis.unipocket.user.common.dto.oauth.OAuthUserInfo;

/**
 * <b>OAuth Provider Service 인터페이스</b>
 * <p>
 * 모든 OAuth Provider(Google, Kakao 등)가 구현해야 하는 인터페이스입니다.
 * </p>
 * @author 김동균
 * @since 2026-01-30
 */
public interface OAuthProviderService {

	String getAuthorizationUrl(String state);

	OAuthTokenResponse getAccessToken(String code);

	OAuthUserInfo getUserInfo(String accessToken);
}
