package com.genesis.unipocket.tempexpense.common.validation;

import com.genesis.unipocket.global.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.tempexpense.command.persistence.entity.TemporaryExpense;
import com.genesis.unipocket.tempexpense.common.enums.TemporaryExpenseStatus;
import com.genesis.unipocket.tempexpense.common.exception.TempExpenseConvertValidationException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class TemporaryExpenseValidator {

	public boolean hasRequiredFieldsForNormal(
			String merchantName,
			Category category,
			CurrencyCode localCountryCode,
			BigDecimal localCurrencyAmount,
			CurrencyCode baseCountryCode,
			LocalDateTime occurredAt) {
		return merchantName != null
				&& !merchantName.isBlank()
				&& category != null
				&& localCountryCode != null
				&& localCurrencyAmount != null
				&& localCurrencyAmount.signum() > 0
				&& baseCountryCode != null
				&& occurredAt != null;
	}

	public TemporaryExpenseStatus resolveStatus(
			TemporaryExpenseStatus originalStatus,
			String merchantName,
			Category category,
			CurrencyCode localCountryCode,
			BigDecimal localCurrencyAmount,
			CurrencyCode baseCountryCode,
			LocalDateTime occurredAt) {
		if (originalStatus == TemporaryExpenseStatus.ABNORMAL) {
			return TemporaryExpenseStatus.ABNORMAL;
		}
		return hasRequiredFieldsForNormal(
						merchantName,
						category,
						localCountryCode,
						localCurrencyAmount,
						baseCountryCode,
						occurredAt)
				? TemporaryExpenseStatus.NORMAL
				: TemporaryExpenseStatus.INCOMPLETE;
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
		if (temp.getOccurredAt() == null) {
			missingOrInvalidFields.add("occurredAt");
		}
		if (temp.getBaseCurrencyAmount() != null && temp.getBaseCurrencyAmount().signum() <= 0) {
			missingOrInvalidFields.add("baseCurrencyAmountMustBeGreaterThanZero");
		}
		if (temp.getLocalCountryCode() != null
				&& resolvedBaseCurrencyCode != null
				&& temp.getLocalCountryCode() == resolvedBaseCurrencyCode
				&& temp.getBaseCurrencyAmount() != null
				&& temp.getLocalCurrencyAmount() != null
				&& temp.getLocalCurrencyAmount().compareTo(temp.getBaseCurrencyAmount()) != 0) {
			missingOrInvalidFields.add("sameCurrencyAmountMismatch");
		}

		return missingOrInvalidFields;
	}
}
