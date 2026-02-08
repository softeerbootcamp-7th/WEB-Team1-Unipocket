package com.genesis.unipocket.expense.persistence.entity.dto;

import com.genesis.unipocket.expense.common.enums.Category;
import com.genesis.unipocket.expense.dto.request.ExpenseManualCreateRequest;
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
		String paymentMethod,
		LocalDateTime occurredAt,
		BigDecimal localCurrencyAmount,
		CurrencyCode localCurrencyCode,
		BigDecimal baseCurrencyAmount,
		CurrencyCode baseCurrencyCode,
		String memo) {

	public static ExpenseManualCreateArgs of(
			ExpenseManualCreateRequest req,
			Long accountBookId,
			CurrencyCode baseCurrencyCode,
			BigDecimal baseCurrencyAmount) {
		return new ExpenseManualCreateArgs(
				accountBookId,
				req.merchantName(),
				req.category(),
				req.paymentMethod(),
				req.occurredAt(),
				req.localCurrencyAmount(),
				req.localCurrencyCode(),
				baseCurrencyAmount,
				baseCurrencyCode,
				req.memo());
	}
}
