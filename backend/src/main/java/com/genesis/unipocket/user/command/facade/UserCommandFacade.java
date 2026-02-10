package com.genesis.unipocket.user.command.facade;

import com.genesis.unipocket.global.config.OAuth2Properties.ProviderType;
import com.genesis.unipocket.user.command.application.UserCommandService;
import com.genesis.unipocket.user.command.application.command.CreateCardCommand;
import com.genesis.unipocket.user.command.application.command.DeleteCardCommand;
import com.genesis.unipocket.user.command.application.command.RegisterUserCommand;
import com.genesis.unipocket.user.command.application.command.WithdrawUserCommand;
import com.genesis.unipocket.user.command.presentation.request.UserCardRequest;
import com.genesis.unipocket.user.common.OAuthUserInfo;
import com.genesis.unipocket.user.query.persistence.response.LoginResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserCommandFacade {

	private final UserCommandService userCommandService;

	public LoginResponse loginOrRegister(OAuthUserInfo userInfo, ProviderType providerType) {
		RegisterUserCommand command = RegisterUserCommand.of(userInfo, providerType);
		return userCommandService.loginOrRegister(command);
	}

	public void withdraw(UUID userId) {
		WithdrawUserCommand command = WithdrawUserCommand.from(userId);
		userCommandService.withdrawUser(command);
	}

	public Long createCard(UserCardRequest request, UUID userId) {
		CreateCardCommand command = CreateCardCommand.of(userId, request);
		return userCommandService.createCard(command);
	}

	public void deleteCard(Long cardId, UUID userId) {
		DeleteCardCommand command = DeleteCardCommand.of(cardId, userId);
		userCommandService.deleteCard(command);
	}
}
