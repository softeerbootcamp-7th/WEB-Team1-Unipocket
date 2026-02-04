package com.genesis.unipocket.expense.application.converter;

import com.genesis.unipocket.expense.application.dto.ExpenseManualCreateCommand;
import com.genesis.unipocket.expense.persistence.dto.ExpenseManualCreateArgs;
import org.springframework.stereotype.Component;

/**
 * <b>지출내역 관련 DTO Converter</b>
 * <ul>
 *     <li>Application -> Persistence 변환
 * </ul>
 *
 * @author codingbaraGo
 * @since 2026-02-03
 */
@Component
public class ExpenseApplicationConverter {

	public ExpenseManualCreateArgs toArgs(ExpenseManualCreateCommand command) {
		return new ExpenseManualCreateArgs(
				command.accountBookId(),
				command.merchantName(),
				command.category(),
				command.paymentMethod(),
				command.occurredAt(),
				command.localAmount(),
				command.localCurrency(),
				command.standardAmount(),
				command.standardCurrency(),
				command.memo());
	}
}
