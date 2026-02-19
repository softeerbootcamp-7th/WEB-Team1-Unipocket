package com.genesis.unipocket.user.command.facade.provide;

import com.genesis.unipocket.auth.command.facade.port.UserLoginProcessor;
import com.genesis.unipocket.auth.command.facade.port.dto.UserLoginRequest;
import com.genesis.unipocket.auth.common.dto.LoginResult;
import com.genesis.unipocket.user.command.application.UserCommandService;
import com.genesis.unipocket.user.command.application.command.RegisterUserCommand;
import com.genesis.unipocket.user.command.application.result.LoginOrRegisterResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * <b>사용자 로그인 Provider</b>
 *
 * @author 김동균
 * @since 2026-02-10
 */
@Component
@RequiredArgsConstructor
public class UserLoginProvider implements UserLoginProcessor {

	private final UserCommandService userService;

	@Override
	public LoginResult loginOrRegister(UserLoginRequest request) {
		RegisterUserCommand command =
				RegisterUserCommand.of(
						request.providerType(),
						request.providerId(),
						request.email(),
						request.name(),
						request.profileImageUrl());

		LoginOrRegisterResult result = userService.loginOrRegister(command);

		return LoginResult.of(result.accessToken(), result.refreshToken(), result.expiresIn());
	}
}
