package com.genesis.unipocket.accountbook.command.persistence.entity;

import com.genesis.unipocket.global.common.enums.CountryCode;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record AccountBookCreateByUserIdArgs(
		UUID userId,
		String title,
		CountryCode localCountryCode,
		CountryCode baseCountryCode,
		Integer bucketOrder,
		BigDecimal budget,
		LocalDate startDate,
		LocalDate endDate) {}
