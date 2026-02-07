package com.genesis.unipocket.accountbook.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.genesis.unipocket.global.common.enums.CountryCode;
import java.time.LocalDate;

public record AccountBookResponse(
		Long id,
		String title,
		CountryCode localCountryCode,
		CountryCode baseCountryCode,
		@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") LocalDate startDate,
		@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") LocalDate endDate) {}
