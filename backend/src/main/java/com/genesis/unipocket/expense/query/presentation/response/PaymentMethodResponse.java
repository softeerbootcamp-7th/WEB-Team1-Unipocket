package com.genesis.unipocket.expense.query.presentation.response;

public record PaymentMethodResponse(boolean isCash, CardResponse card) {

	public record CardResponse(Integer company, String label, String lastDigits) {}

	public static PaymentMethodResponse from(
			Long userCardId, Integer company, String label, String lastDigits) {
		if (userCardId == null) {
			return new PaymentMethodResponse(true, null);
		}
		return new PaymentMethodResponse(false, new CardResponse(company, label, lastDigits));
	}
}
