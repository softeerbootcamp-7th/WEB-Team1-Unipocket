package com.genesis.unipocket.user.command.service.oauth;

import com.genesis.unipocket.global.config.OAuth2Properties.OidcProviderConfig;
import com.genesis.unipocket.global.exception.oauth.OAuthCommunicationException;
import com.genesis.unipocket.user.command.service.oauth.dto.GoogleUserInfo;
import com.genesis.unipocket.user.command.service.oauth.dto.OAuthTokenResponse;
import com.genesis.unipocket.user.command.service.oauth.dto.OAuthUserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * <b>Google OAuth Service</b>
 *
 * @author 김동균
 * @since 2026-01-30
 */
@Slf4j
@RequiredArgsConstructor
public class GoogleOAuthService implements OAuthProviderService {

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
			params.add("code", code);
			params.add("client_id", config.getClientId());
			params.add("client_secret", config.getClientSecret());
			params.add("redirect_uri", config.getRedirectUri());
			params.add("grant_type", "authorization_code");

			return restClient
					.post()
					.uri(config.getTokenUri())
					.contentType(MediaType.APPLICATION_FORM_URLENCODED)
					.body(params)
					.retrieve()
					.body(OAuthTokenResponse.class);

		} catch (Exception e) {
			log.error("Failed to get access token from Google", e);
			throw new OAuthCommunicationException(
					OAuthCommunicationException.CommunicationType.TOKEN);
		}
	}

	@Override
	public OAuthUserInfo getUserInfo(String accessToken) {
		try {
			return restClient
					.get()
					.uri(config.getUserInfoUri())
					.header("Authorization", "Bearer " + accessToken)
					.retrieve()
					.body(GoogleUserInfo.class);

		} catch (Exception e) {
			log.error("Failed to get user info from Google", e);
			throw new OAuthCommunicationException(
					OAuthCommunicationException.CommunicationType.USERINFO);
		}
	}
}
