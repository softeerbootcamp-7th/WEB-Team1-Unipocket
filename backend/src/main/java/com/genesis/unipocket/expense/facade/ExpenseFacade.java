package com.genesis.unipocket.expense.facade;

import com.genesis.unipocket.accountbook.dto.common.AccountBookDto;
import com.genesis.unipocket.accountbook.service.AccountBookService;
import com.genesis.unipocket.expense.dto.common.ExpenseDto;
import com.genesis.unipocket.expense.dto.request.ExpenseManualCreateRequest;
import com.genesis.unipocket.expense.dto.request.ExpenseSearchFilter;
import com.genesis.unipocket.expense.dto.request.ExpenseUpdateRequest;
import com.genesis.unipocket.expense.service.ExpenseService;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
public class ExpenseFacade {

	private final ExpenseService expenseService;
	private final AccountBookService accountBookService;

	@Transactional
	public ExpenseDto createExpenseManual(
			ExpenseManualCreateRequest request, Long accountBookId, UUID userId) {
		// userId - accountBookId 소유권 검증
		AccountBookDto accountBookDto =
				accountBookService.getAccountBook(accountBookId, userId.toString());

		CurrencyCode baseCurrencyCode = accountBookDto.baseCountryCode().getCurrencyCode();

		CurrencyCode localCurrencyCode = request.localCurrencyCode();

		BigDecimal localAmount = request.localCurrencyAmount();
		LocalDateTime occurredAt = request.occurredAt();

		return expenseService.createExpenseManual(request, accountBookId, baseCurrencyCode);
	}

	public ExpenseDto getExpense(Long expenseId, Long accountBookId, UUID userId) {
		// 소유권 검증
		accountBookService.getAccountBook(accountBookId, userId.toString());

		return expenseService.getExpense(expenseId, accountBookId);
	}

	public Page<ExpenseDto> getExpenses(
			Long accountBookId, UUID userId, ExpenseSearchFilter filter, Pageable pageable) {
		// 소유권 검증
		accountBookService.getAccountBook(accountBookId, userId.toString());

		return expenseService.getExpenses(accountBookId, filter, pageable);
	}

	@Transactional
	public ExpenseDto updateExpense(
			Long expenseId, Long accountBookId, UUID userId, ExpenseUpdateRequest request) {
		AccountBookDto accountBookDto =
				accountBookService.getAccountBook(accountBookId, userId.toString());

		CurrencyCode baseCurrencyCode = accountBookDto.baseCountryCode().getCurrencyCode();

		return expenseService.updateExpense(expenseId, accountBookId, request, baseCurrencyCode);
	}

	@Transactional
	public void deleteExpense(Long expenseId, Long accountBookId, UUID userId) {
		accountBookService.getAccountBook(accountBookId, userId.toString());
		expenseService.deleteExpense(expenseId, accountBookId);
	}
}
