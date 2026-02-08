package com.genesis.unipocket.accountbook.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.genesis.unipocket.accountbook.dto.common.AccountBookDto;
import com.genesis.unipocket.global.common.enums.CountryCode;
import java.time.LocalDate;
import java.util.List;

public record AccountBookDetailResponse(
		Long id,
		String title,
		CountryCode localCountryCode,
		CountryCode baseCountryCode,
		Long budget,
		List<?> tempExpenseBatchIds,
		@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") LocalDate startDate,
		@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") LocalDate endDate) {

	public static AccountBookDetailResponse of(
			AccountBookDto dto, List<?> tempExpenseBatchIds) {
		return new AccountBookDetailResponse(
				dto.id(),
				dto.title(),
				dto.localCountryCode(),
				dto.baseCountryCode(),
				dto.budget(),
				tempExpenseBatchIds,
				dto.startDate(),
				dto.endDate());
	}
}
