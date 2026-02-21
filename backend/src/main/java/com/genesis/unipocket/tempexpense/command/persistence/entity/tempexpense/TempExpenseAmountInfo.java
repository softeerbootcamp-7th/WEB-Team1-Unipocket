package com.genesis.unipocket.tempexpense.command.persistence.entity.tempexpense;

import com.genesis.unipocket.global.common.enums.CurrencyCode;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TempExpenseAmountInfo {

	@Enumerated(EnumType.STRING)
	private CurrencyCode localCurrencyCode;
	private BigDecimal localCurrencyAmount;

	@Enumerated(EnumType.STRING)
	private CurrencyCode baseCurrencyCode;
	private BigDecimal baseCurrencyAmount;

	private BigDecimal exchangeRate;

	private TempExpenseAmountInfo(
			CurrencyCode localCurrencyCode,
			BigDecimal localCurrencyAmount,
			CurrencyCode baseCurrencyCode,
			BigDecimal baseCurrencyAmount,
			BigDecimal exchangeRate) {
		this.localCurrencyCode = localCurrencyCode;
		this.localCurrencyAmount = localCurrencyAmount;
		this.baseCurrencyCode = baseCurrencyCode;
		this.baseCurrencyAmount = baseCurrencyAmount;
		this.exchangeRate = exchangeRate;
	}

	public static TempExpenseAmountInfo of(
			CurrencyCode localCurrencyCode,
			BigDecimal localCurrencyAmount,
			CurrencyCode baseCurrencyCode,
			BigDecimal baseCurrencyAmount,
			BigDecimal exchangeRate) {
		return new TempExpenseAmountInfo(
				localCurrencyCode, localCurrencyAmount, baseCurrencyCode, baseCurrencyAmount, exchangeRate);
	}

	public TempExpenseAmountInfo merge(
			CurrencyCode localCurrencyCode,
			BigDecimal localCurrencyAmount,
			CurrencyCode baseCurrencyCode,
			BigDecimal baseCurrencyAmount,
			BigDecimal exchangeRate) {
		return new TempExpenseAmountInfo(
				localCurrencyCode != null ? localCurrencyCode : this.localCurrencyCode,
				localCurrencyAmount != null ? localCurrencyAmount : this.localCurrencyAmount,
				baseCurrencyCode != null ? baseCurrencyCode : this.baseCurrencyCode,
				baseCurrencyAmount != null ? baseCurrencyAmount : this.baseCurrencyAmount,
				exchangeRate != null ? exchangeRate : this.exchangeRate);
	}

	public TempExpenseAmountInfo recalculateBaseIfPossible() {
		if (localCurrencyAmount == null || localCurrencyAmount.signum() <= 0 || exchangeRate == null) {
			return this;
		}
		BigDecimal calculated = localCurrencyAmount.multiply(exchangeRate).setScale(2, RoundingMode.HALF_UP);
		return new TempExpenseAmountInfo(localCurrencyCode, localCurrencyAmount, baseCurrencyCode, calculated, exchangeRate);
	}

	public boolean hasRequired() {
		return localCurrencyCode != null
				&& localCurrencyAmount != null
				&& localCurrencyAmount.signum() > 0
				&& baseCurrencyCode != null
				&& baseCurrencyAmount != null
				&& baseCurrencyAmount.signum() > 0;
	}

	public boolean isAbnormal(BigDecimal thresholdRatio) {
		if (exchangeRate == null || localCurrencyAmount == null || baseCurrencyAmount == null) return false;
		BigDecimal calculated = localCurrencyAmount.multiply(exchangeRate).setScale(2, RoundingMode.HALF_UP);
		if (calculated.signum() <= 0) return false;
		BigDecimal ratio = calculated.subtract(baseCurrencyAmount).abs()
				.divide(calculated, 4, RoundingMode.HALF_UP);
		return ratio.compareTo(thresholdRatio) >= 0;
	}
}

