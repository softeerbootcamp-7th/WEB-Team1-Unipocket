package com.genesis.unipocket.expense.dto.request;

import java.util.List;

/**
 * <b>Batch 변환 요청 DTO</b>
 *
 * @author 김동균
 * @since 2026-02-08
 */
public record BatchConvertRequest(List<Long> tempExpenseIds) {}
