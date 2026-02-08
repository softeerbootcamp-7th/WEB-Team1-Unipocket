package com.genesis.unipocket.accountbook.persistence.entity;

import com.genesis.unipocket.global.common.enums.CountryCode;
import java.time.LocalDate;

public record AccountBookCreateArgs(
		String userId,
		String title,
		CountryCode localCountryCode,
		CountryCode baseCountryCode,
		LocalDate startDate,
		LocalDate endDate) {}
