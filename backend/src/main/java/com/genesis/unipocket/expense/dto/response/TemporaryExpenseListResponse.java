package com.genesis.unipocket.expense.dto.response;

import java.util.List;

/**
 * <b>임시지출내역 목록 응답 DTO</b>
 *
 * @author 김동균
 * @since 2026-02-08
 */
public record TemporaryExpenseListResponse(List<TemporaryExpenseResponse> items) {}
