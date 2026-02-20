package com.genesis.unipocket.expense.command.persistence.entity;

import com.genesis.unipocket.global.common.enums.CurrencyCode;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExchangeInfo {

	@Column(nullable = false)
	private CurrencyCode localCurrencyCode;

	@Column(nullable = false)
	private CurrencyCode baseCurrencyCode;

	@Column(nullable = false)
	private BigDecimal localCurrencyAmount;

	@Column(nullable = true)
	private BigDecimal baseCurrencyAmount;

	@Column(nullable = true)
	private BigDecimal calculatedBaseCurrencyAmount;

	@Column(nullable = true)
	private CurrencyCode calculatedBaseCurrencyCode;

	@Column(precision = 10, scale = 4)
	private BigDecimal exchangeRate;

	public static ExchangeInfo of(
			CurrencyCode localCurrency,
			CurrencyCode billingCurrency,
			BigDecimal localAmount,
			BigDecimal billingAmount,
			BigDecimal calculatedBillingAmount,
			CurrencyCode calculatedBillingCurrency,
			BigDecimal exchangeRate) {
		if (localCurrency == null || billingCurrency == null) {
			throw new IllegalArgumentException("currency must not be null");
		}
		if (localAmount == null || localAmount.signum() <= 0) {
			throw new IllegalArgumentException("localCurrencyAmount must be greater than 0");
		}
		if (billingAmount == null && calculatedBillingAmount == null) {
			throw new IllegalArgumentException(
					"baseCurrencyAmount or calculatedBaseCurrencyAmount is required");
		}
		if (billingAmount != null && billingAmount.signum() <= 0) {
			throw new IllegalArgumentException("baseCurrencyAmount must be greater than 0");
		}
		if (calculatedBillingAmount != null && calculatedBillingAmount.signum() <= 0) {
			throw new IllegalArgumentException(
					"calculatedBaseCurrencyAmount must be greater than 0");
		}
		if (calculatedBillingAmount != null && calculatedBillingCurrency == null) {
			throw new IllegalArgumentException(
					"calculatedBaseCurrencyCode is required when calculated amount exists");
		}
		if (localCurrency == billingCurrency
				&& billingAmount != null
				&& localAmount.compareTo(billingAmount) != 0) {
			throw new IllegalArgumentException("same currency must have same amount");
		}

		ExchangeInfo info = new ExchangeInfo();
		info.localCurrencyCode = localCurrency;
		info.baseCurrencyCode = billingCurrency;
		info.localCurrencyAmount = localAmount;
		info.baseCurrencyAmount = billingAmount;
		info.calculatedBaseCurrencyAmount = calculatedBillingAmount;
		info.calculatedBaseCurrencyCode = calculatedBillingCurrency;
		info.exchangeRate = exchangeRate;
		return info;
	}
}
