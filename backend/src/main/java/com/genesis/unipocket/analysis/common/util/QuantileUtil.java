package com.genesis.unipocket.analysis.common.util;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;

public final class QuantileUtil {

	private static final MathContext DEFAULT_MC = MathContext.DECIMAL64;

	private QuantileUtil() {}

	public static BigDecimal linearInterpolatedQuantile(List<BigDecimal> sorted, double p) {
		return linearInterpolatedQuantile(sorted, p, DEFAULT_MC);
	}

	public static BigDecimal linearInterpolatedQuantile(
			List<BigDecimal> sorted, double p, MathContext mathContext) {
		if (sorted == null || sorted.isEmpty()) {
			return BigDecimal.ZERO;
		}
		if (sorted.size() == 1) {
			return sorted.get(0);
		}
		double clampedP = Math.max(0d, Math.min(1d, p));
		double idx = clampedP * (sorted.size() - 1);
		int lowerIdx = (int) Math.floor(idx);
		int upperIdx = (int) Math.ceil(idx);
		BigDecimal lower = sorted.get(lowerIdx);
		BigDecimal upper = sorted.get(upperIdx);
		if (lowerIdx == upperIdx) {
			return lower;
		}
		BigDecimal ratio = BigDecimal.valueOf(idx - lowerIdx);
		return lower.add(
				upper.subtract(lower, mathContext).multiply(ratio, mathContext), mathContext);
	}
}
