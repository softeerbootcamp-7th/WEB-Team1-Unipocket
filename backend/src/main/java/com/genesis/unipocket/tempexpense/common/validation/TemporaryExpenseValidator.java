package com.genesis.unipocket.tempexpense.common.validation;

import com.genesis.unipocket.expense.common.util.ExchangeAmountCalculator;
import com.genesis.unipocket.global.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.tempexpense.command.persistence.entity.TemporaryExpense;
import com.genesis.unipocket.tempexpense.common.enums.TemporaryExpenseStatus;
import com.genesis.unipocket.tempexpense.common.exception.TempExpenseConvertValidationException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class TemporaryExpenseValidator {

	private static final BigDecimal ABNORMAL_DIFF_THRESHOLD_RATIO = new BigDecimal("0.10");

	public boolean hasRequiredFieldsForNormal(
			String merchantName,
			Category category,
			CurrencyCode localCountryCode,
			BigDecimal localCurrencyAmount,
			CurrencyCode baseCountryCode,
			BigDecimal baseCurrencyAmount,
			LocalDateTime occurredAt) {
		return merchantName != null
				&& !merchantName.isBlank()
				&& category != null
				&& localCountryCode != null
				&& localCurrencyAmount != null
				&& localCurrencyAmount.signum() > 0
				&& baseCountryCode != null
				&& baseCurrencyAmount != null
				&& baseCurrencyAmount.signum() > 0
				&& occurredAt != null;
	}

	public TemporaryExpenseStatus resolveStatus(
			String merchantName,
			Category category,
			CurrencyCode localCountryCode,
			BigDecimal localCurrencyAmount,
			CurrencyCode baseCountryCode,
			BigDecimal baseCurrencyAmount,
			BigDecimal exchangeRate,
			LocalDateTime occurredAt) {
		if (!hasRequiredFieldsForNormal(
				merchantName,
				category,
				localCountryCode,
				localCurrencyAmount,
				baseCountryCode,
				baseCurrencyAmount,
				occurredAt)) {
			return TemporaryExpenseStatus.INCOMPLETE;
		}

		if (isAbnormalAmount(localCurrencyAmount, baseCurrencyAmount, exchangeRate)) {
			return TemporaryExpenseStatus.ABNORMAL;
		}
		return TemporaryExpenseStatus.NORMAL;
	}

	public void validateConvertible(TemporaryExpense temp, CurrencyCode resolvedBaseCurrencyCode) {
		List<String> missingOrInvalidFields =
				findMissingOrInvalidFields(temp, resolvedBaseCurrencyCode);
		if (!missingOrInvalidFields.isEmpty()) {
			throw TempExpenseConvertValidationException.single(
					temp.getTempExpenseId(), missingOrInvalidFields);
		}
	}

	public List<String> findMissingOrInvalidFields(
			TemporaryExpense temp, CurrencyCode resolvedBaseCurrencyCode) {
		List<String> missingOrInvalidFields = new ArrayList<>();
		if (temp.getMerchantName() == null || temp.getMerchantName().isBlank()) {
			missingOrInvalidFields.add("merchantName");
		}
		if (temp.getCategory() == null) {
			missingOrInvalidFields.add("category");
		}
		if (temp.getLocalCountryCode() == null) {
			missingOrInvalidFields.add("localCurrencyCode");
		}
		if (temp.getLocalCurrencyAmount() == null) {
			missingOrInvalidFields.add("localCurrencyAmount");
		} else if (temp.getLocalCurrencyAmount().signum() <= 0) {
			missingOrInvalidFields.add("localCurrencyAmountMustBeGreaterThanZero");
		}
		if (resolvedBaseCurrencyCode == null) {
			missingOrInvalidFields.add("baseCurrencyCode");
		}
		if (temp.getBaseCurrencyAmount() == null) {
			missingOrInvalidFields.add("baseCurrencyAmount");
		} else if (temp.getBaseCurrencyAmount().signum() <= 0) {
			missingOrInvalidFields.add("baseCurrencyAmountMustBeGreaterThanZero");
		}
		if (temp.getOccurredAt() == null) {
			missingOrInvalidFields.add("occurredAt");
		}

		return missingOrInvalidFields;
	}

	private boolean isAbnormalAmount(
			BigDecimal localCurrencyAmount,
			BigDecimal baseCurrencyAmount,
			BigDecimal exchangeRate) {
		if (localCurrencyAmount == null
				|| baseCurrencyAmount == null
				|| exchangeRate == null
				|| localCurrencyAmount.signum() <= 0) {
			return false;
		}
		BigDecimal calculatedBaseAmount =
				ExchangeAmountCalculator.calculateBaseAmount(localCurrencyAmount, exchangeRate);
		if (calculatedBaseAmount.signum() <= 0) {
			return false;
		}
		BigDecimal ratio =
				calculatedBaseAmount
						.subtract(baseCurrencyAmount)
						.abs()
						.divide(calculatedBaseAmount, 4, RoundingMode.HALF_UP);
		return ratio.compareTo(ABNORMAL_DIFF_THRESHOLD_RATIO) >= 0;
	}
}
