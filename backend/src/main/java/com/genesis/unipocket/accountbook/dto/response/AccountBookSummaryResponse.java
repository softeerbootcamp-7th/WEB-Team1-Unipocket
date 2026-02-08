package com.genesis.unipocket.accountbook.dto.response;

import com.genesis.unipocket.accountbook.dto.common.AccountBookDto;

public record AccountBookSummaryResponse(Long id, String title, Boolean isMain) {

	public static AccountBookSummaryResponse of(AccountBookDto dto, Long mainAccountBookId) {
		return new AccountBookSummaryResponse(
				dto.id(), dto.title(), dto.id().equals(mainAccountBookId));
	}
}
