package com.genesis.unipocket.accountbook.command.application.validator;

import com.genesis.unipocket.accountbook.command.persistence.entity.AccountBookEntity;
import com.genesis.unipocket.global.common.enums.CountryCode;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import java.time.LocalDate;
import org.springframework.stereotype.Component;

/**
 * <b>가계부 도메인 규칙 검증 컴포넌트</b>
 * <p>
 * 가계부의 비즈니스 규칙 검증을 담당합니다.
 * 유저 입력 검증은 DTO 레벨(@Valid)에서 처리되므로,
 * 여기서는 도메인 규칙만 검증합니다.
 * </p>
 *
 * @author bluefishez
 * @since 2026-02-02
 */
@Component
public class AccountBookValidator {

	private void validateCountryCodes(CountryCode localCountryCode, CountryCode baseCountryCode) {

		if (localCountryCode == baseCountryCode) {
			throw new BusinessException(ErrorCode.ACCOUNT_BOOK_INVALID_COUNTRY_CODE);
		}
	}

	private void validateDateRange(LocalDate startDate, LocalDate endDate) {
		if (startDate.isAfter(endDate)) {
			throw new BusinessException(ErrorCode.ACCOUNT_BOOK_INVALID_DATE_RANGE);
		}
	}

	private void validateBudget(Long budget) {
		if (budget != null && budget < 0) {
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
