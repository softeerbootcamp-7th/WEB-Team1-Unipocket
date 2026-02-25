package com.genesis.unipocket.accountbook.common.validation;

import com.genesis.unipocket.global.common.enums.CountryCode;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.global.util.CountryCodeTimezoneMapper;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import org.springframework.stereotype.Component;

@Component
public class AccountBookPeriodValidator {

	public void validate(
			CountryCode localCountryCode,
			LocalDate startDate,
			LocalDate endDate,
			OffsetDateTime occurredAtUtc) {
		ZoneId accountBookZoneId = CountryCodeTimezoneMapper.getZoneId(localCountryCode);
		LocalDate occurredDate = occurredAtUtc.atZoneSameInstant(accountBookZoneId).toLocalDate();
		LocalDate effectiveEndDate = endDate != null ? endDate : LocalDate.now(accountBookZoneId);

		boolean beforeStart = startDate != null && occurredDate.isBefore(startDate);
		boolean afterEnd = occurredDate.isAfter(effectiveEndDate);
		if (beforeStart || afterEnd) {
			throw new BusinessException(ErrorCode.EXPENSE_OUT_OF_ACCOUNT_BOOK_PERIOD);
		}
	}
}
