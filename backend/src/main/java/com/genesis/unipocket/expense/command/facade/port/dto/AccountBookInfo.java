package com.genesis.unipocket.expense.command.facade.port.dto;

import com.genesis.unipocket.global.common.enums.CountryCode;
import java.time.LocalDate;

public record AccountBookInfo(
		Long accountBookId,
		String userId,
		CountryCode baseCountryCode,
		CountryCode localCountryCode,
		LocalDate startDate,
		LocalDate endDate) {}
