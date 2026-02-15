package com.genesis.unipocket.auth.command.application.oauth;

import com.genesis.unipocket.auth.common.dto.oauth.KakaoUserInfo;
import com.genesis.unipocket.auth.common.dto.oauth.OAuthTokenResponse;
import com.genesis.unipocket.auth.common.dto.oauth.OAuthUserInfo;
import com.genesis.unipocket.auth.common.exception.oauth.OAuthCommunicationException;
import com.genesis.unipocket.global.config.OAuth2Properties.OidcProviderConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * <b>Kakao OAuth Service</b>
 * @author 김동균
 * @since 2026-01-30
 */
@Slf4j
@RequiredArgsConstructor
public class KakaoOAuthService implements OAuthProviderService {

	private final OidcProviderConfig config;
	private final RestClient restClient;

	@Override
	public String getAuthorizationUrl(String state) {
		return UriComponentsBuilder.fromUriString(config.getAuthorizationUri())
				.queryParam("client_id", config.getClientId())
				.queryParam("redirect_uri", config.getRedirectUri())
				.queryParam("response_type", "code")
				.queryParam("scope", config.getScope())
				.queryParam("state", state)
				.build()
				.toUriString();
	}

	@Override
	public OAuthTokenResponse getAccessToken(String code) {
		try {
			MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
			params.add("grant_type", "authorization_code");
			params.add("client_id", config.getClientId());
			params.add("redirect_uri", config.getRedirectUri());
			params.add("code", code);

			if (config.hasClientSecret()) {
				params.add("client_secret", config.getClientSecret());
			}

			OAuthTokenResponse response =
					restClient
							.post()
							.uri(config.getTokenUri())
							.contentType(MediaType.APPLICATION_FORM_URLENCODED)
							.body(params)
							.retrieve()
							.body(OAuthTokenResponse.class);
			if (response == null
					|| response.getAccessToken() == null
					|| response.getAccessToken().isBlank()) {
				throw new OAuthCommunicationException(
						OAuthCommunicationException.CommunicationType.TOKEN);
			}
			return response;

		} catch (Exception e) {
			log.error("Failed to get access token from Kakao", e);
			throw new OAuthCommunicationException(
					OAuthCommunicationException.CommunicationType.TOKEN);
		}
	}

	@Override
	public OAuthUserInfo getUserInfo(String accessToken) {
		try {
			OAuthUserInfo userInfo =
					restClient
							.get()
							.uri(config.getUserInfoUri())
							.header("Authorization", "Bearer " + accessToken)
							// .header("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE)
							.retrieve()
							.body(KakaoUserInfo.class);
			if (userInfo == null
					|| userInfo.getProviderId() == null
					|| userInfo.getProviderId().isBlank()
					|| "null".equalsIgnoreCase(userInfo.getProviderId())) {
				throw new OAuthCommunicationException(
						OAuthCommunicationException.CommunicationType.USERINFO);
			}
			return userInfo;

		} catch (Exception e) {
			log.error("Failed to get user info from Kakao", e);
			throw new OAuthCommunicationException(
					OAuthCommunicationException.CommunicationType.USERINFO);
		}
	}
}
