package com.genesis.unipocket.expense.command.presentation.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.genesis.unipocket.user.common.enums.CardCompany;

public record PaymentMethodResponse(boolean isCash, CardResponse card) {

	public record CardResponse(
			@JsonFormat(shape = JsonFormat.Shape.NUMBER) CardCompany company,
			String label,
			String lastDigits) {}

	public static PaymentMethodResponse from(
			Long userCardId, CardCompany company, String label, String lastDigits) {
		if (userCardId == null) {
			return new PaymentMethodResponse(true, null);
		}
		return new PaymentMethodResponse(false, new CardResponse(company, label, lastDigits));
	}
}
