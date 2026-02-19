package com.genesis.unipocket.expense.command.presentation.request;

import com.genesis.unipocket.global.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * <b>지출내역 생성 - 수기 DTO</b>
 * <p>Client -> Presentation
 *
 * @author codingbaraGo
 * @since 2026-02-03
 */
public record ExpenseManualCreateRequest(
		@NotBlank String merchantName,
		Category category,
		Long userCardId,
		@NotNull Instant occurredAt,
		BigDecimal localCurrencyAmount,
		CurrencyCode localCurrencyCode,
		BigDecimal baseCurrencyAmount,
		String memo,
		Long travelId) {}
