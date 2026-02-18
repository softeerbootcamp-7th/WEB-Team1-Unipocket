package com.genesis.unipocket.accountbook.command.application.command;

import com.genesis.unipocket.accountbook.command.presentation.request.AccountBookUpdateRequest;
import com.genesis.unipocket.global.common.enums.CountryCode;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record UpdateAccountBookCommand(
		Long accountBookId,
		UUID userId,
		String title,
		CountryCode localCountryCode,
		CountryCode baseCountryCode,
		BigDecimal budget,
		LocalDate startDate,
		LocalDate endDate,
		Boolean isMain) {

	public static UpdateAccountBookCommand of(
			Long accountBookId, UUID userId, AccountBookUpdateRequest request) {
		return new UpdateAccountBookCommand(
				accountBookId,
				userId,
				request.title(),
				request.localCountryCode(),
				request.baseCountryCode(),
				request.budget(),
				request.startDate(),
				request.endDate(),
				request.isMain());
	}
}
