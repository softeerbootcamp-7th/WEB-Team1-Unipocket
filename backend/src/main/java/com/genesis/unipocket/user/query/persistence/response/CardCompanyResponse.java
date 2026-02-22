package com.genesis.unipocket.user.query.persistence.response;

import com.genesis.unipocket.user.common.enums.CardCompany;

/**
 * <b>카드사 응답 DTO</b>
 */
public record CardCompanyResponse(Integer code, String koreanName, String imageUrl) {

	public static CardCompanyResponse from(CardCompany cardCompany) {
		return new CardCompanyResponse(
				cardCompany.ordinal(), cardCompany.getKoreanName(), cardCompany.getImageUrl());
	}
}
