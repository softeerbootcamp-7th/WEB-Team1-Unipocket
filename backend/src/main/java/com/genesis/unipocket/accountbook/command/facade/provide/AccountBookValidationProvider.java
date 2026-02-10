package com.genesis.unipocket.accountbook.command.facade.provide;

import com.genesis.unipocket.accountbook.service.AccountBookService;
import com.genesis.unipocket.expense.command.facade.port.AccountBookInfoFetchService;
import com.genesis.unipocket.expense.common.dto.AccountBookInfo;
import com.genesis.unipocket.expense.common.validator.AccountBookOwnershipValidator;
import com.genesis.unipocket.travel.common.validate.UserAccountBookValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * <b>가계부 검증/조회 Provider</b>
 *
 * @author bluefishez
 * @since 2026-02-10
 */
@Component
@RequiredArgsConstructor
public class AccountBookValidationProvider
		implements AccountBookOwnershipValidator,
				AccountBookInfoFetchService,
				UserAccountBookValidator {

	private final AccountBookService accountBookService;

	@Override
	public void validateOwnership(Long accountBookId, String userId) {
		accountBookService.getAccountBook(accountBookId, userId);
	}

	@Override
	public AccountBookInfo getAccountBook(Long accountBookId, String userId) {
		var accountBook = accountBookService.getAccountBook(accountBookId, userId);
		return new AccountBookInfo(accountBook.baseCountryCode());
	}

	@Override
	public void validateUserAccountBook(String userId, Long accountBookId) {
		accountBookService.getAccountBook(accountBookId, userId);
	}
}
