package com.genesis.unipocket.expense.command.presentation.response;

import com.genesis.unipocket.user.command.persistence.entity.enums.CardCompany;

/**
 * <b>결제수단 응답 DTO</b>
 *
 * @author bluefishez
 * @since 2026-02-12
 */
public record PaymentMethodResponse(boolean isCash, CardResponse card) {

	public record CardResponse(CardCompany company, String label, String lastDigits) {}

	public static PaymentMethodResponse from(
			Long userCardId, CardCompany company, String label, String lastDigits) {
		if (userCardId == null) {
			return new PaymentMethodResponse(true, null);
		}
		return new PaymentMethodResponse(false, new CardResponse(company, label, lastDigits));
	}
}
