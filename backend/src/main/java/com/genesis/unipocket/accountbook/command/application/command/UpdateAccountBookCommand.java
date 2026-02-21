package com.genesis.unipocket.accountbook.command.application.command;

import com.genesis.unipocket.global.common.enums.CountryCode;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record UpdateAccountBookCommand(
		Long accountBookId,
		UUID userId,
		String title,
		boolean titlePresent,
		CountryCode localCountryCode,
		boolean localCountryCodePresent,
		CountryCode baseCountryCode,
		boolean baseCountryCodePresent,
		BigDecimal budget,
		boolean budgetPresent,
		LocalDate startDate,
		boolean startDatePresent,
		LocalDate endDate,
		boolean endDatePresent,
		Boolean isMain,
		boolean isMainPresent) {}
