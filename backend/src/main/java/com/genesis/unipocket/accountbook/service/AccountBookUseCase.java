package com.genesis.unipocket.accountbook.service;

import com.genesis.unipocket.accountbook.dto.common.AccountBookDto;
import com.genesis.unipocket.accountbook.dto.request.CreateAccountBookReq;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountBookUseCase {
	private final AccountBookService accountBookService;

	public AccountBookDto createAccountBook(long userId, CreateAccountBookReq req) {
		return accountBookService.create(userId, "유저", req);
	}
}
