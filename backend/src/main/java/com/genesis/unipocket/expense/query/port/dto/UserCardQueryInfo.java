package com.genesis.unipocket.expense.query.port.dto;

public record UserCardQueryInfo(
		Long userCardId, Integer cardCompany, String nickName, String cardNumber) {}
