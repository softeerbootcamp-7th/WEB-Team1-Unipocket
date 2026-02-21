package com.genesis.unipocket.accountbook.command.facade.provide;

import com.genesis.unipocket.accountbook.command.persistence.entity.AccountBookEntity;
import com.genesis.unipocket.accountbook.command.persistence.repository.AccountBookCommandRepository;
import com.genesis.unipocket.expense.command.facade.port.AccountBookFetchService;
import com.genesis.unipocket.expense.command.facade.port.dto.AccountBookInfo;
import com.genesis.unipocket.expense.common.validation.ExpenseOwnershipValidator;
import com.genesis.unipocket.expense.query.facade.port.AccountBookOwnershipValidator;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.tempexpense.command.facade.port.AccountBookRateInfoProvider;
import com.genesis.unipocket.tempexpense.command.facade.port.dto.AccountBookRateInfo;
import com.genesis.unipocket.travel.common.validate.UserAccountBookValidator;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountBookValidationProvider
		implements ExpenseOwnershipValidator,
				com.genesis.unipocket.analysis.query.port.AccountBookOwnershipValidator,
				com.genesis.unipocket.tempexpense.common.facade.port.AccountBookOwnershipValidator,
				AccountBookFetchService,
				AccountBookRateInfoProvider,
				UserAccountBookValidator,
				com.genesis.unipocket.widget.common.validate.UserAccountBookValidator {

	private final AccountBookCommandRepository accountBookRepository;

	@Override
	public void validateOwnership(Long accountBookId, String userId) {
		findAndValidate(accountBookId, userId);
	}

	@Override
	public AccountBookInfo getAccountBook(Long accountBookId, String userId) {
		var accountBook = findAndValidate(accountBookId, userId);
		return new AccountBookInfo(
				accountBook.getId(),
				accountBook.getUser().getId().toString(),
				accountBook.getBaseCountryCode(),
				accountBook.getLocalCountryCode());
	}

	@Override
	public void validateUserAccountBook(String userId, Long accountBookId) {
		findAndValidate(accountBookId, userId);
	}

	@Override
	public void validateUserAccountBook(UUID userId, Long accountBookId) {
		findAndValidate(accountBookId, userId.toString());
	}

	@Override
	public AccountBookRateInfo getRateInfo(Long accountBookId) {
		var accountBook =
				accountBookRepository
						.findById(accountBookId)
						.orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_BOOK_NOT_FOUND));
		return new AccountBookRateInfo(
				accountBook.getBaseCountryCode().getCurrencyCode(),
				accountBook.getLocalCountryCode().getCurrencyCode());
	}

	private AccountBookEntity findAndValidate(Long accountBookId, String userId) {
		var accountBook =
				accountBookRepository
						.findById(accountBookId)
						.orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_BOOK_NOT_FOUND));

		if (!accountBook.getUser().getId().toString().equals(userId)) {
			throw new BusinessException(ErrorCode.ACCOUNT_BOOK_UNAUTHORIZED_ACCESS);
		}

		return accountBook;
	}
}
