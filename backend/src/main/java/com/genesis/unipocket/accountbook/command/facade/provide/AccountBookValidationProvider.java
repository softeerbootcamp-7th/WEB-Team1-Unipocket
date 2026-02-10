package com.genesis.unipocket.accountbook.command.facade.provide;

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
		return new AccountBookInfo(accountBook.getBaseCountryCode());
	}

	@Override
	public void validateUserAccountBook(String userId, Long accountBookId) {
		findAndValidate(accountBookId, userId);
	}

	private com.genesis.unipocket.accountbook.command.persistence.entity.AccountBookEntity
			findAndValidate(Long accountBookId, String userId) {
		var accountBook =
				accountBookRepository
						.findById(accountBookId)
						.orElseThrow(
								() ->
										new com.genesis.unipocket.global.exception
												.BusinessException(
												com.genesis.unipocket.global.exception.ErrorCode
														.ACCOUNT_BOOK_NOT_FOUND));

		if (!accountBook.getUserId().equals(userId)) {
			throw new com.genesis.unipocket.global.exception.BusinessException(
					com.genesis.unipocket.global.exception.ErrorCode
							.ACCOUNT_BOOK_UNAUTHORIZED_ACCESS);
		}
		return accountBook;
	}
}
