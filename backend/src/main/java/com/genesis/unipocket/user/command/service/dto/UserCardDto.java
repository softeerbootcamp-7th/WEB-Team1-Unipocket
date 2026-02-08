package com.genesis.unipocket.user.command.service.dto;

import com.genesis.unipocket.user.command.persistence.entity.UserCardEntity;
import com.genesis.unipocket.user.command.persistence.entity.enums.CardCompany;

/**
 * Service 계층용 UserCard DTO
 */
public record UserCardDto(
		Long userCardId, String nickName, String cardNumber, CardCompany cardCompany) {

	public static UserCardDto from(UserCardEntity entity) {
		return new UserCardDto(
				entity.getUserCardId(),
				entity.getNickName(),
				entity.getCardNumber(),
				entity.getCardCompany());
	}
}
