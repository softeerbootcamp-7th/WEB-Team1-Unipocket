package com.genesis.unipocket.expense.application.dto;

import com.genesis.unipocket.expense.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CurrencyCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <b>지출내역 생성 - 수기 DTO</b>
 * <p>Facade -> Application
 *
 * @author codingbaraGo
 * @since 2026-02-03
 */
public record ExpenseManualCreateCommand(
		Long accountBookId,
		String merchantName,
		Category category,
		String paymentMethod,
		LocalDateTime occurredAt,
		BigDecimal localAmount,
		CurrencyCode localCurrency,
		BigDecimal standardAmount,
		CurrencyCode standardCurrency,
		String memo) {}
