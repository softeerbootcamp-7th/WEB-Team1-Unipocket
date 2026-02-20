package com.genesis.unipocket.user.query.persistence.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.genesis.unipocket.user.common.enums.CardCompany;

public record UserCardQueryResponse(
		Long userCardId,
		String nickName,
		String cardNumber,
		@JsonFormat(shape = JsonFormat.Shape.NUMBER) CardCompany cardCompany) {}
