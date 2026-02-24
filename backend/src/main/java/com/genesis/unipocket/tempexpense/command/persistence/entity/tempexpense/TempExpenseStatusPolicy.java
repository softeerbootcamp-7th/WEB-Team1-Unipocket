package com.genesis.unipocket.tempexpense.command.persistence.entity.tempexpense;

import com.genesis.unipocket.tempexpense.common.enums.TemporaryExpenseStatus;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class TempExpenseStatusPolicy {

	private static final BigDecimal ABNORMAL_THRESHOLD = new BigDecimal("0.10");

	public TemporaryExpenseStatus resolve(
			TempExpenseContentInfo content, TempExpenseAmountInfo amount) {
		boolean hasRequiredFields =
				content.hasRequired()
						&& amount.getLocalCurrencyCode() != null
						&& amount.getLocalCurrencyAmount() != null
						&& amount.getLocalCurrencyAmount().signum() > 0;
		if (!hasRequiredFields) {
			return TemporaryExpenseStatus.INCOMPLETE;
		}

		if (amount.isAbnormal(ABNORMAL_THRESHOLD)) {
			return TemporaryExpenseStatus.ABNORMAL;
		}
		return TemporaryExpenseStatus.NORMAL;
	}
}
