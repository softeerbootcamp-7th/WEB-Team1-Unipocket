package com.genesis.unipocket.expense.command.facade;

import com.genesis.unipocket.expense.command.application.ExpenseCommandService;
import com.genesis.unipocket.expense.command.application.command.ExpenseCreateCommand;
import com.genesis.unipocket.expense.command.application.command.ExpenseUpdateCommand;
import com.genesis.unipocket.expense.command.application.result.ExpenseResult;
import com.genesis.unipocket.expense.command.facade.port.AccountBookInfoFetchService;
import com.genesis.unipocket.expense.command.facade.port.UserCardFetchService;
import com.genesis.unipocket.expense.command.facade.port.dto.UserCardInfo;
import com.genesis.unipocket.expense.command.presentation.request.ExpenseManualCreateRequest;
import com.genesis.unipocket.expense.command.presentation.request.ExpenseUpdateRequest;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.tempexpense.command.facade.port.AccountBookOwnershipValidator;
import com.genesis.unipocket.tempexpense.command.facade.port.dto.AccountBookInfo;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <b>지출내역 도메인 내부용 Facade 클래스</b>
 * <p>지출내역 도메인에 대한 요청 처리
 *
 * @author codingbaraGo
 * @since 2026-02-03
 */
@Service
@AllArgsConstructor
public class ExpenseCommandFacade {

	private final ExpenseCommandService expenseService;
	private final AccountBookInfoFetchService accountBookInfoFetchService;
	private final AccountBookOwnershipValidator accountBookOwnershipValidator;
	private final UserCardFetchService userCardFetchService;

	@Transactional
	public ExpenseResult createExpenseManual(
			ExpenseManualCreateRequest request, Long accountBookId, UUID userId) {

		accountBookOwnershipValidator.validateOwnership(accountBookId, String.valueOf(userId));

		AccountBookInfo accountBookInfo =
				accountBookInfoFetchService.getAccountBook(accountBookId, userId.toString());

		CurrencyCode baseCurrencyCode = accountBookInfo.baseCountryCode().getCurrencyCode();

		OffsetDateTime occurredAt = request.occurredAt().atOffset(ZoneOffset.UTC);

		ExpenseCreateCommand command =
				new ExpenseCreateCommand(
						accountBookId,
						request.merchantName(),
						request.category(),
						request.userCardId(),
						occurredAt,
						request.localCurrencyAmount(),
						request.localCurrencyCode(),
						baseCurrencyCode,
						request.memo(),
						request.travelId());

		ExpenseResult result = expenseService.createExpenseManual(command);

		return enrichWithCardInfo(result);
	}

	@Transactional
	public ExpenseResult updateExpense(
			Long expenseId, Long accountBookId, UUID userId, ExpenseUpdateRequest request) {

		accountBookOwnershipValidator.validateOwnership(accountBookId, String.valueOf(userId));

		AccountBookInfo accountBookInfo =
				accountBookInfoFetchService.getAccountBook(accountBookId, userId.toString());

		CurrencyCode baseCurrencyCode = accountBookInfo.baseCountryCode().getCurrencyCode();

		OffsetDateTime occurredAt = request.occurredAt().atOffset(ZoneOffset.UTC);

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
						request.localCurrencyCode(),
						request.travelId(),
						baseCurrencyCode);

		ExpenseResult result = expenseService.updateExpense(command);

		return enrichWithCardInfo(result);
	}

	@Transactional
	public void deleteExpense(Long expenseId, Long accountBookId, UUID userId) {
		accountBookOwnershipValidator.validateOwnership(accountBookId, userId.toString());
		expenseService.deleteExpense(expenseId, accountBookId);
	}

	private ExpenseResult enrichWithCardInfo(ExpenseResult result) {
		if (result.userCardId() == null) {
			return result;
		}
		UserCardInfo cardInfo = userCardFetchService.getUserCard(result.userCardId());
		return new ExpenseResult(
				result.expenseId(),
				result.accountBookId(),
				result.travelId(),
				result.category(),
				result.baseCurrencyCode(),
				result.baseCurrencyAmount(),
				result.localCurrencyCode(),
				result.localCurrencyAmount(),
				result.occurredAt(),
				result.merchantName(),
				result.displayMerchantName(),
				result.approvalNumber(),
				result.userCardId(),
				cardInfo.cardCompany(),
				cardInfo.nickName(),
				cardInfo.cardNumber(),
				result.expenseSource(),
				result.fileLink(),
				result.memo(),
				result.cardNumber());
	}
}
