package com.genesis.unipocket.expense.command.presentation.response;

import java.time.LocalDateTime;

/**
 * <b>임시지출내역 변환 응답 DTO</b>
 *
 * @author 김동균
 * @since 2026-02-08
 */
public record ConvertTemporaryExpenseResponse(Long expenseId, LocalDateTime convertedAt) {}
