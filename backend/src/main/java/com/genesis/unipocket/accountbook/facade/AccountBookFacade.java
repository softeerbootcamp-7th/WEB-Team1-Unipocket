package com.genesis.unipocket.accountbook.facade;

import com.genesis.unipocket.accountbook.facade.dto.AccountBookDto;
import com.genesis.unipocket.accountbook.presentation.dto.request.CreateAccountBookReq;
import com.genesis.unipocket.accountbook.service.AccountBookService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class AccountBookFacade {
	private final AccountBookService accountBookService;

	@Transactional
	public AccountBookDto createAccountBook(long userId, CreateAccountBookReq req) {

		return accountBookService.create(userId, req);
	}
}
