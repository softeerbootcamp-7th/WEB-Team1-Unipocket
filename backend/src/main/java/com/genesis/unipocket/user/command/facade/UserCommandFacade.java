package com.genesis.unipocket.user.command.facade;

import com.genesis.unipocket.user.command.application.UserCommandService;
import com.genesis.unipocket.user.command.application.command.CreateCardCommand;
import com.genesis.unipocket.user.command.application.command.DeleteCardCommand;
import com.genesis.unipocket.user.command.application.command.RegisterUserCommand;
import com.genesis.unipocket.user.command.application.command.WithdrawUserCommand;
import com.genesis.unipocket.user.command.application.result.LoginOrRegisterResult;
import com.genesis.unipocket.user.command.presentation.request.UserCardCreateRequest;
import com.genesis.unipocket.user.command.presentation.request.UserCardUpdateRequest;
import com.genesis.unipocket.user.command.presentation.response.UserCardUpdateResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserCommandFacade {

	private final UserCommandService userCommandService;

	public LoginOrRegisterResult loginOrRegister(RegisterUserCommand command) {
		return userCommandService.loginOrRegister(command);
	}

	public void withdraw(UUID userId) {
		WithdrawUserCommand command = WithdrawUserCommand.from(userId);
		userCommandService.withdrawUser(command);
	}

	public Long createCard(UserCardCreateRequest request, UUID userId) {
		CreateCardCommand command = CreateCardCommand.of(userId, request);
		return userCommandService.createCard(command);
	}

	public void deleteCard(Long cardId, UUID userId) {
		DeleteCardCommand command = DeleteCardCommand.of(cardId, userId);
		userCommandService.deleteCard(command);
	}

	public UserCardUpdateResponse updateCard(
			UUID userId, Long cardId, UserCardUpdateRequest request) {
		return UserCardUpdateResponse.of(
				userCommandService.updateCard(userId, cardId, request.nickname()));
	}
}
