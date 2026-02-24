package com.genesis.unipocket.tempexpense.command.persistence.entity.tempexpense;

import com.genesis.unipocket.exchange.common.service.ExchangeRateService;
import com.genesis.unipocket.expense.common.util.ExchangeAmountCalculator;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TempExpenseAmountInfo {

	private static final BigDecimal AMOUNT_MAX = new BigDecimal("99999999.99");

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
		this.localCurrencyAmount =
				exceedsDbPrecision(localCurrencyAmount) ? null : localCurrencyAmount;
		this.baseCurrencyCode = baseCurrencyCode;
		this.baseCurrencyAmount =
				exceedsDbPrecision(baseCurrencyAmount) ? null : baseCurrencyAmount;
		this.exchangeRate = exchangeRate;
	}

	private static boolean exceedsDbPrecision(BigDecimal amount) {
		return amount != null && amount.compareTo(AMOUNT_MAX) > 0;
	}

	public static TempExpenseAmountInfo of(
			CurrencyCode localCurrencyCode,
			BigDecimal localCurrencyAmount,
			CurrencyCode baseCurrencyCode,
			BigDecimal baseCurrencyAmount,
			BigDecimal exchangeRate) {
		return new TempExpenseAmountInfo(
				localCurrencyCode,
				localCurrencyAmount,
				baseCurrencyCode,
				baseCurrencyAmount,
				exchangeRate);
	}

	public static TempExpenseAmountInfo empty() {
		return new TempExpenseAmountInfo(null, null, null, null, null);
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
		if (localCurrencyAmount == null
				|| localCurrencyAmount.signum() <= 0
				|| exchangeRate == null) {
			return this;
		}
		BigDecimal calculated =
				localCurrencyAmount.multiply(exchangeRate).setScale(2, RoundingMode.HALF_UP);
		return new TempExpenseAmountInfo(
				localCurrencyCode, localCurrencyAmount, baseCurrencyCode, calculated, exchangeRate);
	}

	public boolean isAbnormal(BigDecimal thresholdRatio) {
		if (exchangeRate == null || localCurrencyAmount == null || baseCurrencyAmount == null)
			return false;
		BigDecimal calculated =
				localCurrencyAmount.multiply(exchangeRate).setScale(2, RoundingMode.HALF_UP);
		if (calculated.signum() <= 0) return false;
		BigDecimal ratio =
				calculated
						.subtract(baseCurrencyAmount)
						.abs()
						.divide(calculated, 4, RoundingMode.HALF_UP);
		return ratio.compareTo(thresholdRatio) >= 0;
	}

	public CurrencyCode resolvePatchBaseCurrencyCode(
			CurrencyCode requestedBaseCurrencyCode,
			BigDecimal requestedBaseCurrencyAmount,
			CurrencyCode fallbackBaseCurrencyCode) {
		return requestedBaseCurrencyCode != null
				? requestedBaseCurrencyCode
				: baseCurrencyCode != null
						? baseCurrencyCode
						: requestedBaseCurrencyAmount != null ? fallbackBaseCurrencyCode : null;
	}

	public TempExpenseConversionAmount resolveForConversion(
			CurrencyCode defaultLocalCurrencyCode,
			CurrencyCode defaultBaseCurrencyCode,
			OffsetDateTime occurredAt,
			ExchangeRateService exchangeRateService) {
		CurrencyCode resolvedLocalCurrencyCode =
				localCurrencyCode != null ? localCurrencyCode : defaultLocalCurrencyCode;
		CurrencyCode resolvedBaseCurrencyCode =
				baseCurrencyCode != null ? baseCurrencyCode : defaultBaseCurrencyCode;

		BigDecimal resolvedExchangeRate =
				resolvedLocalCurrencyCode == null || resolvedBaseCurrencyCode == null
						? null
						: resolvedLocalCurrencyCode == resolvedBaseCurrencyCode
								? BigDecimal.ONE
								: exchangeRateService.getExchangeRate(
										resolvedLocalCurrencyCode,
										resolvedBaseCurrencyCode,
										occurredAt);

		BigDecimal calculatedBaseCurrencyAmount =
				localCurrencyAmount == null || resolvedExchangeRate == null
						? null
						: ExchangeAmountCalculator.calculateBaseAmount(
								localCurrencyAmount, resolvedExchangeRate);

		return new TempExpenseConversionAmount(
				resolvedLocalCurrencyCode,
				resolvedBaseCurrencyCode,
				baseCurrencyAmount,
				calculatedBaseCurrencyAmount,
				resolvedExchangeRate);
	}
}
