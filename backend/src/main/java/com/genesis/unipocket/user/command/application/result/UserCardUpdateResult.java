package com.genesis.unipocket.user.command.application.result;

import com.genesis.unipocket.user.command.persistence.entity.UserCardEntity;
import com.genesis.unipocket.user.common.enums.CardCompany;

public record UserCardUpdateResult(
		Long userCardId, String nickName, String cardNumber, CardCompany cardCompany) {
	public static UserCardUpdateResult of(UserCardEntity userCard) {
		return new UserCardUpdateResult(
				userCard.getUserCardId(),
				userCard.getNickName(),
				userCard.getCardNumber(),
				userCard.getCardCompany());
	}
}
