package com.genesis.unipocket.user.command.facade;

import com.genesis.unipocket.global.config.OAuth2Properties.ProviderType;
import com.genesis.unipocket.user.command.presentation.dto.response.AuthorizeResponse;
import com.genesis.unipocket.user.command.service.OAuthLoginStateService;
import com.genesis.unipocket.user.command.service.oauth.OAuthProviderFactory;
import com.genesis.unipocket.user.command.service.oauth.OAuthProviderService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <b>OAuth 인증 Facade</b>
 * @author bluefishez
 * @since 2026-01-29
 */
@Service
@RequiredArgsConstructor
public class OAuthAuthorizeFacade {

	private final OAuthProviderFactory providerFactory;
	private final OAuthLoginStateService loginStateService;

	@Transactional
	public AuthorizeResponse authorize(ProviderType providerType) {
		String state = UUID.randomUUID().toString();
		loginStateService.saveLoginState(state, providerType);

		OAuthProviderService provider = providerFactory.getProvider(providerType);
		String authorizationUrl = provider.getAuthorizationUrl(state);

		return new AuthorizeResponse(authorizationUrl, state);
	}
}
