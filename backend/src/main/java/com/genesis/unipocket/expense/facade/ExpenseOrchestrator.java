package com.genesis.unipocket.expense.facade;

import com.genesis.unipocket.expense.application.ExpenseService;
import com.genesis.unipocket.expense.facade.converter.ExpenseFacadeConverter;
import com.genesis.unipocket.expense.presentation.dto.ExpenseManualCreateRequest;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * <b>지출내역 도메인 내부용 Facade 클래스</b>
 * <p>지출내역 도메인에 대한 요청 처리
 *
 * @author codingbaraGo
 * @since 2026-02-03
 */
@Service
@AllArgsConstructor
public class ExpenseOrchestrator {

	private final ExpenseService expenseService;
	private final ExpenseFacadeConverter converter;

	public void createExpense(ExpenseManualCreateRequest request, Long accountBookId, Long userId) {
		// TODO: userId - accountBookId 소유권 검증

		CurrencyCode standardCurrency = request.standardCurrency();
		// TODO: accountBookId - standardCurrency 정합성 검증

		CurrencyCode localCurrency = request.localCurrency();
		BigDecimal localAmount = request.localAmount();
		BigDecimal standardAmount = request.standardAmount();
		LocalDateTime occurredAt = request.occurredAt();
		// TODO: 환율 유효성 검증

		expenseService.createExpense(converter.toCommand(request, accountBookId));
	}
}
