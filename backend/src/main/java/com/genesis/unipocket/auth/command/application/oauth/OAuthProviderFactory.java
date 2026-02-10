package com.genesis.unipocket.auth.command.application.oauth;

import com.genesis.unipocket.global.config.OAuth2Properties;
import com.genesis.unipocket.global.config.OAuth2Properties.OidcProviderConfig;
import com.genesis.unipocket.global.config.OAuth2Properties.ProviderType;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * <b>OAuth Provider Factory</b>
 * @author 김동균
 * @since 2026-01-30
 */
@Component
@RequiredArgsConstructor
public class OAuthProviderFactory {

	private final OAuth2Properties oauthProperties;
	private final RestClient restClient;
	private final Map<ProviderType, OAuthProviderService> providers = new HashMap<>();

	public OAuthProviderService getProvider(ProviderType type) {
		return providers.computeIfAbsent(type, this::createProvider);
	}

	private OAuthProviderService createProvider(ProviderType type) {
		OidcProviderConfig config = oauthProperties.getProvider(type);

		return switch (type) {
			case GOOGLE -> new GoogleOAuthService(config, restClient);
			case KAKAO -> new KakaoOAuthService(config, restClient);
		};
	}
}
