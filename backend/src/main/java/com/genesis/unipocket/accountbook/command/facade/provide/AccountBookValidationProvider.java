package com.genesis.unipocket.accountbook.command.facade.provide;

import com.genesis.unipocket.accountbook.command.persistence.entity.AccountBookEntity;
import com.genesis.unipocket.expense.common.port.AccountBookOwnershipValidator;
import com.genesis.unipocket.expense.common.port.dto.AccountBookInfo;
import com.genesis.unipocket.expense.expense.command.facade.port.AccountBookInfoFetchService;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
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

	private final com.genesis.unipocket.accountbook.command.persistence.repository
					.AccountBookCommandRepository
			accountBookRepository;

	@Override
	public void validateOwnership(Long accountBookId, String userId) {
		findAndValidate(accountBookId, userId);
	}

	@Override
	public AccountBookInfo getAccountBook(Long accountBookId, String userId) {
		var accountBook = findAndValidate(accountBookId, userId);
		return new AccountBookInfo(
				accountBook.getId(),
				accountBook.getUserId(),
				accountBook.getBaseCountryCode(),
				accountBook.getLocalCountryCode());
	}

	@Override
	public void validateUserAccountBook(String userId, Long accountBookId) {
		findAndValidate(accountBookId, userId);
	}

	private AccountBookEntity findAndValidate(Long accountBookId, String userId) {
		var accountBook =
				accountBookRepository
						.findById(accountBookId)
						.orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_BOOK_NOT_FOUND));

		if (!accountBook.getUserId().equals(userId)) {
			throw new BusinessException(ErrorCode.ACCOUNT_BOOK_UNAUTHORIZED_ACCESS);
		}

		return accountBook;
	}
}
