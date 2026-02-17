package com.genesis.unipocket.tempexpense.common.validation;

import com.genesis.unipocket.global.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.tempexpense.command.persistence.entity.TemporaryExpense;
import com.genesis.unipocket.tempexpense.common.enums.TemporaryExpenseStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

@Component
public class TemporaryExpenseValidator {

	/** NORMAL 판정에 필요한 필수 필드 충족 여부 */
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
				&& baseCountryCode != null
				&& occurredAt != null;
	}

	/** 상태 재평가 (ABNORMAL 고정) */
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

	/** 확정 변환 필수값 검증 */
	public void validateConvertible(TemporaryExpense temp, CurrencyCode resolvedBaseCurrencyCode) {
		if (!hasRequiredFieldsForNormal(
				temp.getMerchantName(),
				temp.getCategory(),
				temp.getLocalCountryCode(),
				temp.getLocalCurrencyAmount(),
				resolvedBaseCurrencyCode,
				temp.getOccurredAt())) {
			throw new BusinessException(ErrorCode.TEMP_EXPENSE_CONVERT_REQUIRED_FIELDS_MISSING);
		}
	}
}
