package com.genesis.unipocket.user.command.presentation;

import com.genesis.unipocket.global.config.OAuth2Properties.ProviderType;
import com.genesis.unipocket.global.response.ApiResponse;
import com.genesis.unipocket.user.command.facade.OAuthAuthorizeFacade;
import com.genesis.unipocket.user.command.facade.UserLoginFacade;
import com.genesis.unipocket.user.command.presentation.dto.response.AuthorizeResponse;
import com.genesis.unipocket.user.command.presentation.dto.response.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/oauth2/authorize/{provider}")
    public ApiResponse<AuthorizeResponse> authorize(
            @PathVariable("provider") String provider) {

        ProviderType providerType = ProviderType.valueOf(provider.toUpperCase());
        AuthorizeResponse response = authorizeFacade.authorize(providerType);

        return ApiResponse.success(response);
    }

    @GetMapping("/oauth2/callback/{provider}")
    public ApiResponse<LoginResponse> callback(
            @PathVariable("provider") String provider,
            @RequestParam("code") String code,
            @RequestParam("state") String state) {

        ProviderType providerType = ProviderType.valueOf(provider.toUpperCase());
        LoginResponse response = loginFacade.login(providerType, code, state);

        return ApiResponse.success(response);
    }
}