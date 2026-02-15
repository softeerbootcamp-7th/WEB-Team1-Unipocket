package com.genesis.unipocket.expense.command.application.command;

import com.genesis.unipocket.global.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * <b>지출내역 생성 Command</b>
 * <p>Facade -> Application
 *
 * @author bluefishez
 * @since 2026-02-10
 */
public record ExpenseCreateCommand(
		Long accountBookId,
		String merchantName,
		Category category,
		Long userCardId,
		OffsetDateTime occurredAt,
		BigDecimal localCurrencyAmount,
		CurrencyCode localCurrencyCode,
		CurrencyCode baseCurrencyCode,
		String memo,
		Long travelId) {}
