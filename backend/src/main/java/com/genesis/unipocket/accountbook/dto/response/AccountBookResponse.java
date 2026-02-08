package com.genesis.unipocket.accountbook.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.genesis.unipocket.accountbook.dto.common.AccountBookDto;
import com.genesis.unipocket.global.common.enums.CountryCode;
import java.time.LocalDate;

public record AccountBookResponse(
		Long id,
		String title,
		CountryCode localCountryCode,
		CountryCode baseCountryCode,
		@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") LocalDate startDate,
		@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") LocalDate endDate) {

	public static AccountBookResponse from(AccountBookDto dto) {
		return new AccountBookResponse(
				dto.id(),
				dto.title(),
				dto.localCountryCode(),
				dto.baseCountryCode(),
				dto.startDate(),
				dto.endDate());
	}
}
