package com.genesis.unipocket.user.command.application.command;

import com.genesis.unipocket.user.command.presentation.request.UserCardRequest;
import com.genesis.unipocket.user.common.enums.CardCompany;
import java.util.UUID;

public record CreateCardCommand(
		UUID userId, String nickName, String cardNumber, CardCompany cardCompany) {

	public static CreateCardCommand of(UUID userId, UserCardRequest request) {
		return new CreateCardCommand(
				userId, request.nickName(), request.cardNumber(), request.cardCompany());
	}
}
