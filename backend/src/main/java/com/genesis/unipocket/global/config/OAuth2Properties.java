package com.genesis.unipocket.global.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * OAuth2 설정을 application.yml에서 바인딩하는 클래스
 */
@Configuration
@ConfigurationProperties(prefix = "oauth2")
@Getter
@Setter
public class OAuth2Properties {

    private Google google;
    private Kakao kakao;

    @Getter
    @Setter
    public static class Google {
        private String clientId;
        private String clientSecret;
        private String redirectUri;
        private String authorizationUri;
        private String tokenUri;
        private String userInfoUri;
        private String jwksUri;
        private String issuer;
        private String scope;
    }

    @Getter
    @Setter
    public static class Kakao {
        private String clientId;
        private String clientSecret;  // 선택사항
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
    }
}