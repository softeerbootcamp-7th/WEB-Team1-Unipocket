package com.genesis.unipocket.global.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * <b>BigDecimal 타입을 컨벤션에 맞춰 변환해주는 유틸 클래스</b>
 * <p>값이 정수면 정수 반환
 * <p>값이 소수이면 둘째자리에서 내림 반환
 */
public final class AmountFormatUtil {

	private AmountFormatUtil() {}

	public static String format(BigDecimal amount) {
		if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) {
			return "0";
		}
		BigDecimal truncated = amount.setScale(2, RoundingMode.DOWN);
		return truncated.stripTrailingZeros().toPlainString();
	}
}
