package com.genesis.unipocket.expense.persistence.entity.expense;

import com.genesis.unipocket.global.common.enums.CurrencyCode;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * <b>기준화폐 & 환율 정보 원본</b>
 * <p>기준 화폐 변경과 독립적으로 유지되는 정보
 * <p>이 정보들은 수정되지 않는다.
 *
 * @author codingbaraGo
 * @since 2026-02-03
 */
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

	@Column(nullable = false)
	private BigDecimal baseCurrencyAmount;

	public static ExchangeInfo of(
			CurrencyCode localCurrency,
			CurrencyCode billingCurrency,
			BigDecimal localAmount,
			BigDecimal billingAmount) {
		if (localCurrency == null || billingCurrency == null) {
			throw new IllegalArgumentException("currency must not be null");
		}
		if (localAmount == null
				|| billingAmount == null
				|| localAmount.signum() <= 0
				|| billingAmount.signum() <= 0) {
			throw new IllegalArgumentException("amount must be positive");
		}
		if (localCurrency == billingCurrency && localAmount.compareTo(billingAmount) != 0) {
			throw new IllegalArgumentException("same currency must have same amount");
		}

		ExchangeInfo info = new ExchangeInfo();
		info.localCurrencyCode = localCurrency;
		info.baseCurrencyCode = billingCurrency;
		info.localCurrencyAmount = localAmount;
		info.baseCurrencyAmount = billingAmount;
		return info;
	}
}
