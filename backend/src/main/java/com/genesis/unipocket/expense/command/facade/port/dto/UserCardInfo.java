package com.genesis.unipocket.expense.command.facade.port.dto;

import com.genesis.unipocket.user.common.enums.CardCompany;

public record UserCardInfo(
		Long userCardId, CardCompany cardCompany, String nickName, String cardNumber) {}
