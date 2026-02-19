package com.genesis.unipocket.accountbook.command.application.result;

import com.genesis.unipocket.accountbook.command.persistence.entity.AccountBookEntity;
import com.genesis.unipocket.global.common.enums.CountryCode;
import java.time.LocalDate;

public record AccountBookResult(
		Long accountBookId,
		String title,
		CountryCode localCountryCode,
		CountryCode baseCurrencyCode,
		LocalDate startDate,
		LocalDate endDate) {
	public static AccountBookResult of(AccountBookEntity entity) {
		return new AccountBookResult(
				entity.getId(),
				entity.getTitle(),
				entity.getLocalCountryCode(),
				entity.getBaseCountryCode(),
				entity.getStartDate(),
				entity.getEndDate());
	}
}
