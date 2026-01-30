package com.genesis.unipocket.accountbook.command.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.genesis.unipocket.accountbook.command.common.dto.AccountBookDto;
import lombok.Builder;

/**
 * <b>POST /api/account-books 출력</b>
 * @author bluefishez
 * @since 2026-01-30
 */
@Builder
public class CreateAccountBookRes {

	@JsonUnwrapped private final AccountBookDto accountBookDto;
}
