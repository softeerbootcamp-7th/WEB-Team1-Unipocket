package com.genesis.unipocket.accountbook.command.facade;

import com.genesis.unipocket.accountbook.command.application.AccountBookCommandService;
import com.genesis.unipocket.accountbook.command.application.command.CreateAccountBookCommand;
import com.genesis.unipocket.accountbook.command.application.command.DeleteAccountBookCommand;
import com.genesis.unipocket.accountbook.command.application.command.UpdateAccountBookCommand;
import com.genesis.unipocket.accountbook.command.application.result.AccountBookBudgetUpdateResult;
import com.genesis.unipocket.accountbook.command.facade.port.AccountBookDefaultWidgetPort;
import com.genesis.unipocket.accountbook.command.facade.port.ExpenseCurrencySyncService;
import com.genesis.unipocket.accountbook.command.facade.port.UserInfoReader;
import com.genesis.unipocket.accountbook.command.facade.port.UserMainAccountBookService;
import com.genesis.unipocket.accountbook.command.presentation.request.AccountBookBudgetUpdateRequest;
import com.genesis.unipocket.accountbook.command.presentation.request.AccountBookCreateRequest;
import com.genesis.unipocket.accountbook.command.presentation.request.AccountBookUpdateRequest;
import com.genesis.unipocket.accountbook.command.presentation.response.AccountBookResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountBookCommandFacade {

	private final AccountBookCommandService accountBookCommandService;
	private final UserInfoReader userInfoReader;
	private final AccountBookDefaultWidgetPort accountBookDefaultWidgetPort;
	private final UserMainAccountBookService userMainAccountBookService;
	private final ExpenseCurrencySyncService expenseCurrencySyncService;

	@Transactional
	public AccountBookResponse createAccountBook(UUID userId, AccountBookCreateRequest req) {
		String userName = userInfoReader.getUserName(userId);

		CreateAccountBookCommand command =
				new CreateAccountBookCommand(
						userId, userName, req.localCountryCode(), req.startDate(), req.endDate());

		var result = accountBookCommandService.create(command);
		accountBookDefaultWidgetPort.setDefaultWidget(result.accountBookId());
		return AccountBookResponse.of(result);
	}

	@Transactional
	public AccountBookResponse updateAccountBook(
			UUID userId, Long accountBookId, AccountBookUpdateRequest req) {

		UpdateAccountBookCommand command =
				new UpdateAccountBookCommand(
						accountBookId,
						userId,
						req.title(),
						req.titlePresent(),
						req.localCountryCode(),
						req.localCountryCodePresent(),
						req.baseCountryCode(),
						req.baseCountryCodePresent(),
						req.budget(),
						req.budgetPresent(),
						req.startDate(),
						req.startDatePresent(),
						req.endDate(),
						req.endDatePresent(),
						req.isMain(),
						req.isMainPresent());
		var result = accountBookCommandService.update(command);
		if (command.isMainPresent() && Boolean.TRUE.equals(command.isMain())) {
			userMainAccountBookService.updateMainAccountBook(userId, accountBookId);
		}

		if (result.countryChanged()) {
			expenseCurrencySyncService.updateBaseCurrency(
					accountBookId, result.baseCurrencyCode().getCurrencyCode());
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
