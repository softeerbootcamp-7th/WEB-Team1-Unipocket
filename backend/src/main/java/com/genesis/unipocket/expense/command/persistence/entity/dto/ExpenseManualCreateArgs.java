package com.genesis.unipocket.expense.command.persistence.entity.dto;

import com.genesis.unipocket.expense.command.application.command.ExpenseCreateCommand;
import com.genesis.unipocket.global.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <b>지출내역 생성 - 수기 DTO</b>
 * <p>Application -> Persistence
 *
 * @author codingbaraGo
 * @since 2026-02-03
 */
public record ExpenseManualCreateArgs(
		Long accountBookId,
		String merchantName,
		Category category,
		Long userCardId,
		LocalDateTime occurredAt,
		BigDecimal localCurrencyAmount,
		CurrencyCode localCurrencyCode,
		BigDecimal baseCurrencyAmount,
		CurrencyCode baseCurrencyCode,
		String memo,
		Long travelId) {

	public static ExpenseManualCreateArgs of(
			ExpenseCreateCommand command, BigDecimal baseCurrencyAmount) {
		return new ExpenseManualCreateArgs(
				command.accountBookId(),
				command.merchantName(),
				command.category(),
				command.userCardId(),
				command.occurredAt(),
				command.localCurrencyAmount(),
				command.localCurrencyCode(),
				baseCurrencyAmount,
				command.baseCurrencyCode(),
				command.memo(),
				command.travelId());
	}
}
