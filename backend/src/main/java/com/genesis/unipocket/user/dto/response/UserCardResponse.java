package com.genesis.unipocket.user.dto.response;

import com.genesis.unipocket.user.persistence.entity.UserCardEntity;
import com.genesis.unipocket.user.persistence.entity.enums.CardCompany;

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
