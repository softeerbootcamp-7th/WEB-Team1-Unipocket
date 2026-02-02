package com.genesis.unipocket.accountbook.presentation.dto.response;

import com.genesis.unipocket.accountbook.facade.dto.AccountBookDto;
import com.genesis.unipocket.global.common.enums.CountryCode;
import java.time.LocalDate;

/**
 * <b>POST /api/account-books 출력</b>
 *
 * @author bluefishez
 * @since 2026-01-30
 */
public record CreateAccountBookRes(
		Long id,
		Long userId,
		String title,
		CountryCode localCountryCode,
		CountryCode baseCountryCode,
		Integer budget,
		LocalDate startDate,
		LocalDate endDate) {
	public static CreateAccountBookRes from(AccountBookDto accountBookDto) {
		return new CreateAccountBookRes(
				accountBookDto.id(),
				accountBookDto.userId(),
				accountBookDto.title(),
				accountBookDto.localCountryCode(),
				accountBookDto.baseCountryCode(),
				accountBookDto.budget(),
				accountBookDto.startDate(),
				accountBookDto.endDate());
	}
}
