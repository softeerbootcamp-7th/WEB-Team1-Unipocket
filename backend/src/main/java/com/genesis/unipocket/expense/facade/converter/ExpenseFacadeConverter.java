package com.genesis.unipocket.expense.facade.converter;

import com.genesis.unipocket.expense.application.dto.ExpenseManualCreateCommand;
import com.genesis.unipocket.expense.presentation.dto.ExpenseManualCreateRequest;
import org.springframework.stereotype.Component;

/**
 * <b>지출내역 관련 DTO Converter</b>
 * <ul>
 *     <li>Facade -> Application 변환
 * </ul>
 *
 * @author codingbaraGo
 * @since 2026-02-03
 */
@Component
public class ExpenseFacadeConverter {

	public ExpenseManualCreateCommand toCommand(
			ExpenseManualCreateRequest request, Long accountBookId) {
		return new ExpenseManualCreateCommand(
				accountBookId,
				request.merchantName(),
				request.category(),
				request.paymentMethod(),
				request.occurredAt(),
				request.localAmount(),
				request.localCurrency(),
				request.standardAmount(),
				request.standardCurrency(),
				request.memo());
	}
}
