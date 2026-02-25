package com.genesis.unipocket.accountbook.command.application.validator;

import com.genesis.unipocket.accountbook.command.persistence.entity.AccountBookEntity;
import com.genesis.unipocket.global.common.enums.CountryCode;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.stereotype.Component;

@Component
public class AccountBookValidator {

	private void validateCountryCodes(CountryCode localCountryCode, CountryCode baseCountryCode) {

		if (localCountryCode == baseCountryCode) {
			throw new BusinessException(ErrorCode.ACCOUNT_BOOK_INVALID_COUNTRY_CODE);
		}
	}

	private void validateDateRange(LocalDate startDate, LocalDate endDate) {
		if (startDate == null || endDate == null) {
			return;
		}
		if (startDate.isAfter(endDate)) {
			throw new BusinessException(ErrorCode.ACCOUNT_BOOK_INVALID_DATE_RANGE);
		}
	}

	private void validateBudget(BigDecimal budget) {
		if (budget != null && budget.compareTo(BigDecimal.ZERO) < 0) {
			throw new BusinessException(ErrorCode.ACCOUNT_BOOK_INVALID_BUDGET);
		}
	}

	private void validateForCreate(
			CountryCode localCountryCode,
			CountryCode baseCountryCode,
			LocalDate startDate,
			LocalDate endDate) {

		validateCountryCodes(localCountryCode, baseCountryCode);
		validateDateRange(startDate, endDate);
	}

	public void validate(AccountBookEntity entity) {
		validateForCreate(
				entity.getLocalCountryCode(),
				entity.getBaseCountryCode(),
				entity.getStartDate(),
				entity.getEndDate());

		validateBudget(entity.getBudget());
	}
}
