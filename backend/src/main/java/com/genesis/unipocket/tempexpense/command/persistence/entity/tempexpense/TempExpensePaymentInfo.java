package com.genesis.unipocket.tempexpense.command.persistence.entity.tempexpense;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TempExpensePaymentInfo {

	private String cardLastFourDigits;
	private String approvalNumber;

	private TempExpensePaymentInfo(String cardLastFourDigits, String approvalNumber) {
		this.cardLastFourDigits = cardLastFourDigits;
		this.approvalNumber = approvalNumber;
	}

	public static TempExpensePaymentInfo of(String cardLastFourDigits, String approvalNumber) {
		return new TempExpensePaymentInfo(cardLastFourDigits, approvalNumber);
	}

	public static TempExpensePaymentInfo empty() {
		return new TempExpensePaymentInfo(null, null);
	}

	public TempExpensePaymentInfo merge(String cardLastFourDigits, String approvalNumber) {
		return new TempExpensePaymentInfo(
				cardLastFourDigits != null ? cardLastFourDigits : this.cardLastFourDigits,
				approvalNumber != null ? approvalNumber : this.approvalNumber);
	}
}
