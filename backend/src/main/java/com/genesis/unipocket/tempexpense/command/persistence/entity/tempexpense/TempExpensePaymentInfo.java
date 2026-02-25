package com.genesis.unipocket.tempexpense.command.persistence.entity.tempexpense;

import jakarta.persistence.Embeddable;
import java.util.Optional;
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

	/**
	 * PATCH 시맨틱에 따라 필드를 병합한다.
	 *
	 * @param cardLastFourDigits null = 필드 미전송(기존값 유지), Optional.empty() = 명시적
	 *                           null(초기화),
	 *                           Optional.of(value) = 새 값 설정
	 * @param approvalNumber     null이면 기존값 유지, non-null이면 새 값 설정
	 */
	public TempExpensePaymentInfo merge(
			Optional<String> cardLastFourDigits, String approvalNumber) {
		String mergedCard =
				cardLastFourDigits == null
						? this.cardLastFourDigits // 필드 미전송 → 기존값 유지
						: cardLastFourDigits.orElse(null); // empty() → null, of(v) → v
		return new TempExpensePaymentInfo(
				mergedCard, approvalNumber != null ? approvalNumber : this.approvalNumber);
	}
}
