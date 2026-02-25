package com.genesis.unipocket.expense.command.facade;

import com.genesis.unipocket.accountbook.common.validation.AccountBookPeriodValidator;
import com.genesis.unipocket.analysis.command.application.AnalysisMonthlyDirtyMarkerService;
import com.genesis.unipocket.expense.command.application.ExpenseCommandService;
import com.genesis.unipocket.expense.command.application.command.ExpenseCreateCommand;
import com.genesis.unipocket.expense.command.application.command.ExpenseUpdateCommand;
import com.genesis.unipocket.expense.command.application.result.ExpenseResult;
import com.genesis.unipocket.expense.command.facade.port.AccountBookFetchService;
import com.genesis.unipocket.expense.command.facade.port.dto.AccountBookInfo;
import com.genesis.unipocket.expense.command.presentation.request.ExpenseBulkUpdateItemRequest;
import com.genesis.unipocket.expense.command.presentation.request.ExpenseBulkUpdateRequest;
import com.genesis.unipocket.expense.command.presentation.request.ExpenseManualCreateRequest;
import com.genesis.unipocket.expense.command.presentation.request.ExpenseUpdateRequest;
import com.genesis.unipocket.expense.common.facade.port.UserCardFetchService;
import com.genesis.unipocket.expense.common.facade.port.dto.UserCardInfo;
import com.genesis.unipocket.expense.common.validation.ExpenseOwnershipValidator;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class ExpenseCommandFacade {

	private final ExpenseCommandService expenseService;
	private final UserCardFetchService userCardFetchService;
	private final AccountBookFetchService accountBookFetchService;
	private final AnalysisMonthlyDirtyMarkerService analysisMonthlyDirtyMarkerService;
	private final ExpenseOwnershipValidator expenseOwnershipValidator;
	private final AccountBookPeriodValidator accountBookPeriodValidator;

	@Transactional
	public ExpenseResult createExpenseManual(
			ExpenseManualCreateRequest request, Long accountBookId, UUID userId) {

		expenseOwnershipValidator.validateOwnership(accountBookId, String.valueOf(userId));

		AccountBookInfo accountBookInfo =
				accountBookFetchService.getAccountBook(accountBookId, userId.toString());

		CurrencyCode localCurrencyCode =
				request.localCurrencyCode() != null
						? request.localCurrencyCode()
						: accountBookInfo.localCountryCode().getCurrencyCode();
		CurrencyCode baseCurrencyCode = accountBookInfo.baseCountryCode().getCurrencyCode();
		OffsetDateTime occurredAt = request.occurredAt().atOffset(ZoneOffset.UTC);
		validateOccurredAtWithinAccountBookPeriod(accountBookInfo, occurredAt);
		validateUserCardOwnershipIfPresent(request.userCardId(), userId);

		ExpenseCreateCommand command =
				new ExpenseCreateCommand(
						accountBookId,
						request.merchantName(),
						request.category(),
						request.userCardId(),
						occurredAt,
						request.localCurrencyAmount(),
						request.baseCurrencyAmount(),
						localCurrencyCode,
						baseCurrencyCode,
						request.memo(),
						request.travelId());

		ExpenseResult result = expenseService.createExpenseManual(command);

		analysisMonthlyDirtyMarkerService.markDirty(result.accountBookId(), result.occurredAt());
		return enrichWithCardInfoIfPresent(result);
	}

	@Transactional
	public ExpenseResult updateExpense(
			Long expenseId, Long accountBookId, UUID userId, ExpenseUpdateRequest request) {
		expenseOwnershipValidator.validateOwnership(accountBookId, String.valueOf(userId));

		AccountBookInfo accountBookInfo =
				accountBookFetchService.getAccountBook(accountBookId, userId.toString());
		CurrencyCode localCurrencyCode =
				request.localCurrencyCode() != null
						? request.localCurrencyCode()
						: accountBookInfo.localCountryCode().getCurrencyCode();
		CurrencyCode baseCurrencyCode = accountBookInfo.baseCountryCode().getCurrencyCode();
		OffsetDateTime occurredAt = request.occurredAt().atOffset(ZoneOffset.UTC);
		validateOccurredAtWithinAccountBookPeriod(accountBookInfo, occurredAt);
		validateUserCardOwnershipIfPresent(request.userCardId(), userId);

		ExpenseUpdateCommand command =
				new ExpenseUpdateCommand(
						expenseId,
						accountBookId,
						request.merchantName(),
						request.category(),
						request.userCardId(),
						request.memo(),
						occurredAt,
						request.localCurrencyAmount(),
						request.baseCurrencyAmount(),
						localCurrencyCode,
						request.travelId(),
						baseCurrencyCode);

		return checkAndEnsureCard(command);
	}

	@Transactional
	public List<ExpenseResult> updateExpensesBulk(
			Long accountBookId, UUID userId, ExpenseBulkUpdateRequest request) {
		expenseOwnershipValidator.validateOwnership(accountBookId, String.valueOf(userId));

		AccountBookInfo accountBookInfo =
				accountBookFetchService.getAccountBook(accountBookId, userId.toString());
		return request.items().stream()
				.map(item -> updateExpenseItem(accountBookId, userId, item, accountBookInfo))
				.toList();
	}

	private ExpenseResult updateExpenseItem(
			Long accountBookId,
			UUID userId,
			ExpenseBulkUpdateItemRequest item,
			AccountBookInfo accountBookInfo) {
		CurrencyCode accountBookLocalCurrencyCode =
				accountBookInfo.localCountryCode().getCurrencyCode();
		CurrencyCode baseCurrencyCode = accountBookInfo.baseCountryCode().getCurrencyCode();
		CurrencyCode localCurrencyCode =
				item.localCurrencyCode() != null
						? item.localCurrencyCode()
						: accountBookLocalCurrencyCode;
		OffsetDateTime occurredAt = item.occurredAt().atOffset(ZoneOffset.UTC);
		validateOccurredAtWithinAccountBookPeriod(accountBookInfo, occurredAt);
		validateUserCardOwnershipIfPresent(item.userCardId(), userId);

		ExpenseUpdateCommand command =
				new ExpenseUpdateCommand(
						item.expenseId(),
						accountBookId,
						item.merchantName(),
						item.category(),
						item.userCardId(),
						item.memo(),
						occurredAt,
						item.localCurrencyAmount(),
						item.baseCurrencyAmount(),
						localCurrencyCode,
						item.travelId(),
						baseCurrencyCode);

		return checkAndEnsureCard(command);
	}

	private ExpenseResult checkAndEnsureCard(ExpenseUpdateCommand command) {
		ExpenseResult result = expenseService.updateExpense(command);
		return enrichWithCardInfoIfPresent(result);
	}

	private ExpenseResult enrichWithCardInfoIfPresent(ExpenseResult result) {
		if (result.userCardId() == null) {
			return result;
		}

		UserCardInfo cardInfo = userCardFetchService.getUserCard(result.userCardId()).orElse(null);
		if (cardInfo == null) {
			return result;
		}
		return result.withCardInfo(cardInfo);
	}

	private void validateUserCardOwnershipIfPresent(Long userCardId, UUID userId) {
		if (userCardId == null) {
			return;
		}

		boolean owned =
				userCardFetchService.getUserCardOwnedBy(userCardId, userId.toString()).isPresent();
		if (owned) {
			return;
		}

		if (userCardFetchService.getUserCard(userCardId).isEmpty()) {
			throw new BusinessException(ErrorCode.CARD_NOT_FOUND);
		}

		throw new BusinessException(ErrorCode.CARD_NOT_OWNED);
	}

	private void validateOccurredAtWithinAccountBookPeriod(
			AccountBookInfo accountBookInfo, OffsetDateTime occurredAt) {
		accountBookPeriodValidator.validate(
				accountBookInfo.localCountryCode(),
				accountBookInfo.startDate(),
				accountBookInfo.endDate(),
				occurredAt);
	}

	@Transactional
	public void deleteExpense(Long expenseId, Long accountBookId, UUID userId) {
		expenseOwnershipValidator.validateOwnership(accountBookId, userId.toString());
		expenseService.deleteExpense(expenseId, accountBookId);
	}
}
