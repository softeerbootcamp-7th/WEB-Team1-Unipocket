package com.genesis.unipocket.accountbook.dto.common;

import com.genesis.unipocket.global.common.enums.CountryCode;
import java.time.LocalDate;

public record AccountBookDto(
		Long id,
		String title,
		CountryCode localCountryCode,
		CountryCode baseCountryCode,
		Long budget,
		LocalDate startDate,
		LocalDate endDate) {}
