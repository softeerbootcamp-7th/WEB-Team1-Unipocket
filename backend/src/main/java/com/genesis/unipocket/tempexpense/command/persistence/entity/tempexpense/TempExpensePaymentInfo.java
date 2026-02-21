package com.genesis.unipocket.tempexpense.command.persistence.entity.tempexpense;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TempExpensePaymentInfo {

	private String paymentsMethod;
	private String cardLastFourDigits;
	private String approvalNumber;

	private TempExpensePaymentInfo(String paymentsMethod, String cardLastFourDigits, String approvalNumber) {
		this.paymentsMethod = paymentsMethod;
		this.cardLastFourDigits = cardLastFourDigits;
		this.approvalNumber = approvalNumber;
	}

	public static TempExpensePaymentInfo of(String paymentsMethod, String cardLastFourDigits, String approvalNumber) {
		return new TempExpensePaymentInfo(paymentsMethod, cardLastFourDigits, approvalNumber);
	}

	public TempExpensePaymentInfo merge(String paymentsMethod, String cardLastFourDigits, String approvalNumber) {
		return new TempExpensePaymentInfo(
				paymentsMethod != null ? paymentsMethod : this.paymentsMethod,
				cardLastFourDigits != null ? cardLastFourDigits : this.cardLastFourDigits,
				approvalNumber != null ? approvalNumber : this.approvalNumber
		);
	}
}

