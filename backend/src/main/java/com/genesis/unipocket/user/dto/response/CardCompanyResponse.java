package com.genesis.unipocket.user.dto.response;

import com.genesis.unipocket.user.persistence.entity.enums.CardCompany;

/**
 * <b>카드사 응답 DTO</b>
 */
public record CardCompanyResponse(String code, String koreanName, String imageUrl) {

	public static CardCompanyResponse from(CardCompany cardCompany) {
		return new CardCompanyResponse(
				cardCompany.name(), cardCompany.getKoreanName(), cardCompany.getImageUrl());
	}
}
