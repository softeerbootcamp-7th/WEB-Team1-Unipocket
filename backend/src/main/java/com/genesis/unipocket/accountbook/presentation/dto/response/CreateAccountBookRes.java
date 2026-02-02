package com.genesis.unipocket.accountbook.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.genesis.unipocket.accountbook.facade.dto.AccountBookDto;
import lombok.Builder;
import lombok.Getter;

/**
 * <b>POST /api/account-books 출력</b>
 * @author bluefishez
 * @since 2026-01-30
 */
@Builder
@Getter
public class CreateAccountBookRes {

	@JsonUnwrapped private AccountBookDto accountBookDto;

	public static CreateAccountBookRes from(AccountBookDto accountBookDto) {
		return CreateAccountBookRes.builder().accountBookDto(accountBookDto).build();
	}
}
