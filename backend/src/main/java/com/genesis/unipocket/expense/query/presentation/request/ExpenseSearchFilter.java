package com.genesis.unipocket.expense.query.presentation.request;

import com.genesis.unipocket.global.common.enums.Category;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * <b>지출내역 검색 필터 DTO</b>
 * <p>모든 필드 nullable
 *
 * @author bluefishez
 * @since 2026-02-07
 */
public record ExpenseSearchFilter(
		OffsetDateTime startDate,
		OffsetDateTime endDate,
		Category category,
		BigDecimal minAmount,
		BigDecimal maxAmount,
		String merchantName,
		Long travelId) {}
