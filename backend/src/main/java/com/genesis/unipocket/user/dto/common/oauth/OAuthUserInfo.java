package com.genesis.unipocket.user.dto.common.oauth;

/**
 * <b>OAuth 사용자 정보 인터페이스</b>
 * @author 김동균
 * @since 2026-01-30
 */
public interface OAuthUserInfo {

	String getProviderId();

	String getEmail();

	String getName();

	String getProfileImageUrl();
}
