package com.genesis.unipocket.accountbook.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.genesis.unipocket.accountbook.dto.common.AccountBookDto;
import com.genesis.unipocket.global.common.enums.CountryCode;
import java.time.LocalDate;

public record CreateAccountBookRes(
		Long id,
		String title,
		CountryCode localCountryCode,
		CountryCode baseCountryCode,
		Integer budget,
		@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") LocalDate startDate,
		@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") LocalDate endDate) {

	public static CreateAccountBookRes from(AccountBookDto dto) {
		return new CreateAccountBookRes(
				dto.id(),
				dto.title(),
				dto.localCountryCode(),
				dto.baseCountryCode(),
				dto.budget(),
				dto.startDate(),
				dto.endDate());
	}
}
