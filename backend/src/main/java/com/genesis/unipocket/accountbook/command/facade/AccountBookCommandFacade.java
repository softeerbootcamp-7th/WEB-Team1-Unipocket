package com.genesis.unipocket.accountbook.command.facade;

import com.genesis.unipocket.accountbook.command.application.AccountBookCommandService;
import com.genesis.unipocket.accountbook.command.application.command.CreateAccountBookCommand;
import com.genesis.unipocket.accountbook.command.application.command.DeleteAccountBookCommand;
import com.genesis.unipocket.accountbook.command.application.command.UpdateAccountBookCommand;
import com.genesis.unipocket.accountbook.command.application.result.AccountBookBudgetUpdateResult;
import com.genesis.unipocket.accountbook.command.facade.port.AccountBookDefaultWidgetPort;
import com.genesis.unipocket.accountbook.command.presentation.request.AccountBookBudgetUpdateRequest;
import com.genesis.unipocket.accountbook.command.presentation.request.AccountBookCreateRequest;
import com.genesis.unipocket.accountbook.command.presentation.request.AccountBookUpdateRequest;
import com.genesis.unipocket.accountbook.command.presentation.response.AccountBookResponse;
import com.genesis.unipocket.accountbook.query.service.AccountBookQueryService;
import com.genesis.unipocket.expense.command.application.ExpenseCommandService;
import com.genesis.unipocket.user.query.persistence.response.UserQueryResponse;
import com.genesis.unipocket.user.query.service.UserQueryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountBookCommandFacade {

	private final AccountBookCommandService accountBookCommandService;
	private final UserQueryService userQueryService;
	private final AccountBookDefaultWidgetPort accountBookDefaultWidgetPort;
	private final ExpenseCommandService expenseCommandService;
	private final AccountBookQueryService accountBookQueryService;

	@Transactional
	public AccountBookResponse createAccountBook(UUID userId, AccountBookCreateRequest req) {
		UserQueryResponse userResponse = userQueryService.getUserInfo(userId);

		CreateAccountBookCommand command =
				CreateAccountBookCommand.of(userId, userResponse.name(), req);

		var result = accountBookCommandService.create(command);
		accountBookDefaultWidgetPort.setDefaultWidget(result.accountBookId());
		return AccountBookResponse.of(result);
	}

	@Transactional
	public AccountBookResponse updateAccountBook(
			UUID userId, Long accountBookId, AccountBookUpdateRequest req) {

		var currentAccountBook = accountBookQueryService.getAccountBook(accountBookId);
		boolean baseCountryChanged =
				!currentAccountBook.baseCountryCode().equals(req.baseCountryCode());

		UpdateAccountBookCommand command = UpdateAccountBookCommand.of(accountBookId, userId, req);
		var result = accountBookCommandService.update(command);

		if (baseCountryChanged) {
			expenseCommandService.updateBaseCurrency(
					accountBookId, req.baseCountryCode().getCurrencyCode());
		}

		return AccountBookResponse.of(result);
	}

	@Transactional
	public void deleteAccountBook(UUID userId, Long accountBookId) {
		DeleteAccountBookCommand command = DeleteAccountBookCommand.of(accountBookId, userId);

		accountBookCommandService.delete(command);
	}

	@Transactional
	public AccountBookBudgetUpdateResult updateBudget(
			UUID userId, Long accountBookId, AccountBookBudgetUpdateRequest req) {
		return accountBookCommandService.updateBudget(accountBookId, userId, req.budget());
	}
}
