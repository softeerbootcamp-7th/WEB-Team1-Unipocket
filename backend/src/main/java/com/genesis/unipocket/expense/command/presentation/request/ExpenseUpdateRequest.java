package com.genesis.unipocket.expense.command.presentation.request;

import com.genesis.unipocket.global.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * <b>지출내역 수정 요청 DTO</b>
 * <p>Full replacement (PUT) - 모든 필드 필수
 *
 * @author bluefishez
 * @since 2026-02-07
 */
public record ExpenseUpdateRequest(
		@NotNull String merchantName,
		Category category,
		Long userCardId,
		@NotNull Instant occurredAt,
		@NotNull BigDecimal localCurrencyAmount,
		@NotNull CurrencyCode localCurrencyCode,
		String memo,
		Long travelId) {}
