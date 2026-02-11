package com.genesis.unipocket.user.query.persistence.response;

import com.genesis.unipocket.user.command.persistence.entity.UserCardEntity;
import com.genesis.unipocket.user.command.persistence.entity.enums.CardCompany;

public record UserCardQueryResponse(
		Long userCardId, String nickName, String cardNumber,
		CardCompany cardCompany) {
	public static UserCardQueryResponse from(UserCardEntity entity) {
		return new UserCardQueryResponse(
				entity.getUserCardId(),
				entity.getNickName(),
				entity.getCardNumber(),
				entity.getCardCompany());
	}
}
