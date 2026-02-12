package com.genesis.unipocket.accountbook.command.presentation.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.genesis.unipocket.accountbook.command.application.result.AccountBookBudgetUpdateResult;
import com.genesis.unipocket.global.common.enums.CountryCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AccountBookBudgetUpdateResponse(
		Long accountBookId,
		CountryCode baseCountryCode,
		CountryCode localCountryCode,
		BigDecimal budget,
		@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
				LocalDateTime budgetCreatedAt,
		BigDecimal exchangeRate) {
	public static AccountBookBudgetUpdateResponse from(AccountBookBudgetUpdateResult result) {
		return new AccountBookBudgetUpdateResponse(
				result.accountBookId(),
				result.baseCountryCode(),
				result.localCountryCode(),
				result.budget(),
				result.budgetCreatedAt(),
				result.exchangeRate());
	}
}
