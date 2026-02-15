package com.genesis.unipocket.accountbook.command.presentation.response;

import com.genesis.unipocket.accountbook.command.application.result.AccountBookResult;
import com.genesis.unipocket.global.common.enums.CountryCode;
import java.time.LocalDate;

public record AccountBookResponse(
		Long id,
		String title,
		CountryCode localCountryCode,
		CountryCode baseCurrencyCode,
		LocalDate startDate,
		LocalDate endDate) {

	public static AccountBookResponse of(AccountBookResult result) {
		return new AccountBookResponse(
				result.accountBookId(),
				result.title(),
				result.localCountryCode(),
				result.baseCurrencyCode(),
				result.startDate(),
				result.endDate());
	}
}
