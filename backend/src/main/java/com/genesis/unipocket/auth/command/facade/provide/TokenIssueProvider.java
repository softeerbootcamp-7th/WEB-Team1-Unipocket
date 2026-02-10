package com.genesis.unipocket.auth.command.facade.provide;

import com.genesis.unipocket.auth.command.application.TokenService;
import com.genesis.unipocket.auth.common.dto.LoginResult;
import com.genesis.unipocket.user.command.application.result.LoginOrRegisterResult;
import com.genesis.unipocket.user.command.facade.port.TokenIssuePort;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TokenIssueProvider implements TokenIssuePort {

	private final TokenService tokenService;

	@Override
	public LoginOrRegisterResult issueTokens(UUID userId) {
		LoginResult result = tokenService.createTokens(userId);
		return LoginOrRegisterResult.of(
				result.getAccessToken(), result.getRefreshToken(), result.getExpiresIn());
	}
}
