package com.genesis.unipocket.global.config;

import com.genesis.unipocket.auth.exception.oauth.OAuthException;
import com.genesis.unipocket.global.exception.ErrorCode;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * <b>OAuth2 Provider 설정</b>
 * <p>
 * application.yml의 oauth2 설정을 바인딩하는 클래스입니다.
 * OIDC 표준을 준수하며, 여러 Provider를 동일한 인터페이스로 관리합니다.
 * </p>
 *
 * @author 김동균
 * @since 2026-01-29
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "oauth2")
public class OAuth2Properties {

	private List<OidcProviderConfig> providers = new ArrayList<>();

	public OidcProviderConfig getProvider(ProviderType type) {
		return providers.stream()
				.filter(p -> p.getType() == type)
				.findFirst()
				.orElseThrow(() -> new OAuthException(ErrorCode.INVALID_OAUTH_PROVIDER));
	}

	@Getter
	@Setter
	public static class OidcProviderConfig {
		private ProviderType type;
		private String clientId;
		private String clientSecret;
		private String redirectUri;
		private String authorizationUri;
		private String tokenUri;
		private String userInfoUri;
		private String jwksUri;
		private String issuer;
		private String scope;

		public boolean hasClientSecret() {
			return clientSecret != null && !clientSecret.isBlank();
		}

		public boolean isClientSecretRequired() {
			return type != null && type.isClientSecretRequired();
		}

		public void validate() {
			if (type == null) {
				throw new OAuthException(ErrorCode.OAUTH_PROVIDER_NOT_CONFIGURED);
			}
			if (clientId == null || clientId.isBlank()) {
				throw new OAuthException(ErrorCode.OAUTH_PROVIDER_NOT_CONFIGURED);
			}
			if (type.isClientSecretRequired() && !hasClientSecret()) {
				throw new OAuthException(ErrorCode.OAUTH_PROVIDER_NOT_CONFIGURED);
			}
		}
	}

	public enum ProviderType {
		GOOGLE(true),
		KAKAO(false);

		private final boolean clientSecretRequired;

		ProviderType(boolean clientSecretRequired) {
			this.clientSecretRequired = clientSecretRequired;
		}

		public boolean isClientSecretRequired() {
			return clientSecretRequired;
		}
	}
}
