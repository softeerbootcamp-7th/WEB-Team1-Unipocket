package com.genesis.unipocket.accountbook.dto.common;

import com.genesis.unipocket.accountbook.entity.AccountBookEntity;
import com.genesis.unipocket.global.common.enums.CountryCode;
import java.time.LocalDate;

public record AccountBookDto(
		Long id,
		Long userId,
		String title,
		CountryCode localCountryCode,
		CountryCode baseCountryCode,
		Integer budget,
		LocalDate startDate,
		LocalDate endDate) {
	public static AccountBookDto from(AccountBookEntity entity) {
		return new AccountBookDto(
				entity.getId(),
				entity.getUserId(),
				entity.getTitle(),
				entity.getLocalCountryCode(),
				entity.getBaseCountryCode(),
				entity.getBudget(),
				entity.getStartDate(),
				entity.getEndDate());
	}
}
