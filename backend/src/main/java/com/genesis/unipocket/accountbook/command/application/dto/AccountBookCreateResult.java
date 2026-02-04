package com.genesis.unipocket.accountbook.command.application.dto;

import com.genesis.unipocket.global.common.enums.CountryCode;
import java.time.LocalDateTime;

public record AccountBookCreateResult(
		Long accountBookId,
		String userId,
		String title,
		CountryCode localCountryCode,
		CountryCode baseCountryCode,
		LocalDateTime startDate,
		LocalDateTime endDate) {}
