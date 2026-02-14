package com.genesis.unipocket.accountbook.command.facade;

import com.genesis.unipocket.accountbook.command.application.AccountBookCommandService;
import com.genesis.unipocket.accountbook.command.application.command.CreateAccountBookCommand;
import com.genesis.unipocket.accountbook.command.application.command.DeleteAccountBookCommand;
import com.genesis.unipocket.accountbook.command.application.command.UpdateAccountBookCommand;
import com.genesis.unipocket.accountbook.command.application.result.AccountBookBudgetUpdateResult;
import com.genesis.unipocket.accountbook.command.presentation.request.AccountBookBudgetUpdateRequest;
import com.genesis.unipocket.accountbook.command.presentation.request.AccountBookCreateRequest;
import com.genesis.unipocket.accountbook.command.presentation.request.AccountBookUpdateRequest;
import com.genesis.unipocket.accountbook.query.persistence.response.AccountBookDetailResponse;
import com.genesis.unipocket.accountbook.query.service.AccountBookQueryService;
import com.genesis.unipocket.expense.command.application.ExpenseCommandService;
import com.genesis.unipocket.global.common.enums.CountryCode;
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
	private final AccountBookQueryService accountBookQueryService;
	private final UserQueryService userQueryService;
	private final ExpenseCommandService expenseCommandService;

	@Transactional
	public Long createAccountBook(UUID userId, AccountBookCreateRequest req) {
		UserQueryResponse userResponse = userQueryService.getUserInfo(userId);

		CreateAccountBookCommand command = CreateAccountBookCommand.of(userId, userResponse.name(), req);

		return accountBookCommandService.create(command);
	}

	@Transactional
	public Long updateAccountBook(UUID userId, Long accountBookId, AccountBookUpdateRequest req) {
		// 변경 전 상태 조회 (베이스 국가 변경 확인용)
		AccountBookDetailResponse currentInfo = accountBookQueryService.getAccountBookDetail(userId.toString(),
				accountBookId);
		CountryCode oldBaseCountryCode = currentInfo.baseCountryCode();

		UpdateAccountBookCommand command = UpdateAccountBookCommand.of(accountBookId, userId, req);
		Long updatedId = accountBookCommandService.update(command);

		// 베이스 국가가 변경되었으면 지출 내역 일괄 업데이트
		if (oldBaseCountryCode != req.baseCountryCode()) {
			expenseCommandService.updateBaseCurrency(
					accountBookId, req.baseCountryCode().getCurrencyCode());
		}

		return updatedId;
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
