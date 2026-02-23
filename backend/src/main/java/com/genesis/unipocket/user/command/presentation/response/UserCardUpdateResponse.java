package com.genesis.unipocket.user.command.presentation.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.genesis.unipocket.user.command.application.result.UserCardUpdateResult;
import com.genesis.unipocket.user.common.enums.CardCompany;

public record UserCardUpdateResponse(
		Long userCardId,
		String nickName,
		String cardNumber,
		@JsonFormat(shape = JsonFormat.Shape.NUMBER) CardCompany cardCompany) {

	public static UserCardUpdateResponse of(UserCardUpdateResult result) {
		return new UserCardUpdateResponse(
				result.userCardId(), result.nickName(), result.cardNumber(), result.cardCompany());
	}
}
