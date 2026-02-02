package com.genesis.unipocket.user.command.presentation;

import com.genesis.unipocket.global.config.OAuth2Properties.ProviderType;
import com.genesis.unipocket.user.command.facade.OAuthAuthorizeFacade;
import com.genesis.unipocket.user.command.facade.UserLoginFacade;
import com.genesis.unipocket.user.command.presentation.dto.response.AuthorizeResponse;
import com.genesis.unipocket.user.command.presentation.dto.response.LoginResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * <b>사용자 Command Controller</b>
 * @author 김동균
 * @since 2026-01-30
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserCommandController {

    private final OAuthAuthorizeFacade authorizeFacade;
    private final UserLoginFacade loginFacade;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    /**
     * OAuth 인증 시작
     *
     * 사용자가 이 URL에 접속하면:
     * 1. State 생성 및 DB 저장
     * 2. Google/Kakao 로그인 페이지로 자동 리다이렉트
     */
    @GetMapping("/oauth2/authorize/{provider}")
    public void authorize(
            @PathVariable("provider") String provider,
            HttpServletResponse response) throws IOException {

        ProviderType providerType = ProviderType.valueOf(provider.toUpperCase());

        // State 생성 및 인증 URL 받기
        AuthorizeResponse authResponse = authorizeFacade.authorize(providerType);

        // Google/Kakao 로그인 페이지로 리다이렉트
        response.sendRedirect(authResponse.getAuthorizationUrl());
    }

    /**
     * OAuth 콜백 처리
     *
     * Google/Kakao 로그인 완료 후:
     * 1. 토큰 발급
     * 2. 프론트엔드로 리다이렉트 (토큰 포함)
     */
    @GetMapping("/oauth2/callback/{provider}")
    public void callback(
            @PathVariable("provider") String provider,
            @RequestParam("code") String code,
            @RequestParam("state") String state,
            HttpServletResponse response) throws IOException {

        ProviderType providerType = ProviderType.valueOf(provider.toUpperCase());

        // 로그인 처리 및 토큰 발급
        LoginResponse loginResponse = loginFacade.login(providerType, code, state);

        // 프론트엔드로 리다이렉트 (토큰을 쿼리 파라미터로 전달)
        String redirectUrl = String.format(
                "%s/auth/callback?access_token=%s&refresh_token=%s&expires_in=%d",
                frontendUrl,
                URLEncoder.encode(loginResponse.getAccessToken(), StandardCharsets.UTF_8),
                URLEncoder.encode(loginResponse.getRefreshToken(), StandardCharsets.UTF_8),
                loginResponse.getExpiresIn()
        );

        response.sendRedirect(redirectUrl);
    }
}