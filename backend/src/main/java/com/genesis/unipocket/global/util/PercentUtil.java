package com.genesis.unipocket.global.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * <b>퍼센트 계산용 유틸리티 클래스</b>
 * <p>위젯/분석 등에서 퍼센트를 사용할 때 총 합을 100으로 조정
 * <p>각 퍼센트의 값을 양의 정수로 변환
 */
public final class PercentUtil {

	private PercentUtil() {}

	public static int calculatePercent(BigDecimal amount, BigDecimal total) {
		if (total.compareTo(BigDecimal.ZERO) == 0) {
			return 0;
		}
		return amount.multiply(BigDecimal.valueOf(100))
				.divide(total, 0, RoundingMode.HALF_UP)
				.intValue();
	}

	public static List<Integer> distributePercents(List<BigDecimal> amounts, BigDecimal total) {
		if (amounts.isEmpty()) {
			return List.of();
		}
		if (total.compareTo(BigDecimal.ZERO) == 0) {
			return amounts.stream().map(a -> 0).toList();
		}

		List<Integer> percents = new ArrayList<>(amounts.size());
		int sum = 0;

		for (int i = 0; i < amounts.size(); i++) {
			int percent;
			if (i < amounts.size() - 1) {
				percent = calculatePercent(amounts.get(i), total);
				sum += percent;
			} else {
				percent = Math.max(0, 100 - sum);
			}
			percents.add(percent);
		}
		return percents;
	}
}
