package com.genesis.unipocket.user.command.presentation.dto.response;

import com.genesis.unipocket.user.command.persistence.entity.UserCardEntity;
import com.genesis.unipocket.user.command.persistence.entity.enums.CardCompany;

public record UserCardResponse(
		Long userCardId, String nickName, String cardNumber, CardCompany cardCompany) {
	public static UserCardResponse from(UserCardEntity entity) {
		return new UserCardResponse(
				entity.getUserCardId(),
				entity.getNickName(),
				entity.getCardNumber(),
				entity.getCardCompany());
	}
}
