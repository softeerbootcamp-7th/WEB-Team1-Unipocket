package com.genesis.unipocket.tempexpense.command.persistence.entity.tempexpense;

import com.genesis.unipocket.tempexpense.common.enums.TemporaryExpenseStatus;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class TempExpenseStatusPolicy {

	private static final BigDecimal ABNORMAL_THRESHOLD = new BigDecimal("0.10");

	public TemporaryExpenseStatus resolve(
			TemporaryExpenseStatus originalStatus,
			TempExpenseContentInfo content,
			TempExpenseAmountInfo amount) {
		if (originalStatus == TemporaryExpenseStatus.ABNORMAL) {
			return TemporaryExpenseStatus.ABNORMAL;
		}

		boolean hasRequiredFields =
				content.hasRequired()
						&& amount.getLocalCurrencyCode() != null
						&& amount.getLocalCurrencyAmount() != null
						&& amount.getLocalCurrencyAmount().signum() > 0
						&& amount.getBaseCurrencyCode() != null;
		if (!hasRequiredFields) {
			return TemporaryExpenseStatus.INCOMPLETE;
		}

		if (amount.isAbnormal(ABNORMAL_THRESHOLD)) {
			return TemporaryExpenseStatus.ABNORMAL;
		}
		return TemporaryExpenseStatus.NORMAL;
	}
}
