package com.genesis.unipocket.accountbook.query.persistence.response;

import com.fasterxml.jackson.annotation.JsonFormat;

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

}
