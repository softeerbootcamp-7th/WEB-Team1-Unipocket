package com.genesis.unipocket.accountbook.command.application.result;

import com.genesis.unipocket.global.common.enums.CountryCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AccountBookBudgetUpdateResult(
		Long accountBookId,
		CountryCode baseCountryCode,
		CountryCode localCountryCode,
		BigDecimal budget,
		LocalDateTime budgetCreatedAt,
		BigDecimal exchangeRate) {}
