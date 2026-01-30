package com.genesis.unipocket.global.config;

import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.global.exception.oauth.OAuthException;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * <b>OAuth2 Provider 설정</b>
 * <p>
 * application.yml의 oauth2 설정을 바인딩하는 클래스입니다.
 * ProviderType은 별도 파일로 분리하여 의존성을 낮추고,
 * 상세 설정 클래스(OidcProviderConfig)는 내부에 두어 응집도를 높였습니다.
 * </p>
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "oauth2")
public class OAuth2Properties {

    private List<OidcProviderConfig> providers = new ArrayList<>();

    /**
     * Provider 타입으로 설정 조회
     */
    public OidcProviderConfig getProvider(ProviderType type) {
        return providers.stream()
                .filter(p -> p.getType() == type)
                .findFirst()
                .orElseThrow(() -> new OAuthException(ErrorCode.INVALID_OAUTH_PROVIDER));
    }

    /**
     * OIDC Provider 설정 DTO
     * - 외부에서 단독으로 생성해서 쓸 일이 없으므로 static inner class로 유지
     */
    @Getter
    @Setter
    public static class OidcProviderConfig {
        private ProviderType type; // 분리된 Enum 사용
        private String clientId;
        private String clientSecret;
        private String redirectUri;
        private String authorizationUri;
        private String tokenUri;
        private String userInfoUri;
        private String jwksUri;
        private String issuer;
        private String scope;

        /**
         * Client Secret이 설정되어 있는지 확인
         */
        public boolean hasClientSecret() {
            return clientSecret != null && !clientSecret.isBlank();
        }

        /**
         * 설정 유효성 검증
         * (스프링 컨텍스트 로딩 시점이나 로직 수행 전에 호출)
         */
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
}