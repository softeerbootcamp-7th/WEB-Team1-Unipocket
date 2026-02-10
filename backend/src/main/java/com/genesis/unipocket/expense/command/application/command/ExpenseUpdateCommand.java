package com.genesis.unipocket.expense.command.application.command;

import com.genesis.unipocket.expense.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <b>지출내역 수정 Command</b>
 * <p>Facade -> Application
 *
 * @author bluefishez
 * @since 2026-02-10
 */
public record ExpenseUpdateCommand(
		Long expenseId,
		Long accountBookId,
		String merchantName,
		Category category,
		String paymentMethod,
		String memo,
		LocalDateTime occurredAt,
		BigDecimal localCurrencyAmount,
		CurrencyCode localCurrencyCode,
		Long travelId,
		CurrencyCode baseCurrencyCode) {}
