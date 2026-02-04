package com.genesis.unipocket.accountbook.command.facade.dto;

import com.genesis.unipocket.global.common.enums.CountryCode;
import java.time.LocalDateTime;

public record AccountBookCreateInfo(
		Long accountBookId,
		String title,
		CountryCode localCountryCode,
		CountryCode baseCountryCode,
		LocalDateTime startDate,
		LocalDateTime endDate) {}
