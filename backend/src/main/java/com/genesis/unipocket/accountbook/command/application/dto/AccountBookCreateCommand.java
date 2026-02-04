package com.genesis.unipocket.accountbook.command.application.dto;

import com.genesis.unipocket.global.common.enums.CountryCode;
import java.time.LocalDate;

public record AccountBookCreateCommand(
		String userId,
		String username,
		CountryCode localCountryCode,
		LocalDate startDate,
		LocalDate endDate) {}
