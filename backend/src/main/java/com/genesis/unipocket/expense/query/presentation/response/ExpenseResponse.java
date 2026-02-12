package com.genesis.unipocket.expense.query.presentation.response;

import com.genesis.unipocket.expense.query.service.result.ExpenseResult;
import com.genesis.unipocket.global.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.global.common.enums.ExpenseSource;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <b>지출내역 상세 조회 응답 DTO</b>
 *
 * @author bluefishez
 * @since 2026-02-07
 */
public record ExpenseResponse(
		Long expenseId,
		Long accountBookId,
		Long travelId,
		String merchantName,
		String displayMerchantName,
		Category category,
		String paymentMethod,
		LocalDateTime occurredAt,
		BigDecimal localCurrencyAmount,
		CurrencyCode localCurrencyCode,
		BigDecimal baseCurrencyAmount,
		CurrencyCode baseCurrencyCode,
		String memo,
		ExpenseSource source,
		String approvalNumber,
		String cardNumber,
		String fileLink) {

	public static ExpenseResponse from(ExpenseResult dto) {
		return new ExpenseResponse(
				dto.id(),
				dto.accountBookId(),
				dto.travelId(),
				dto.merchantName(),
				dto.displayMerchantName(),
				dto.category(),
				dto.paymentMethod(),
				dto.occurredAt(),
				dto.localCurrencyAmount(),
				dto.localCurrencyCode(),
				dto.baseCurrencyAmount(),
				dto.baseCurrencyCode(),
				dto.memo(),
				dto.expenseSource(),
				dto.approvalNumber(),
				dto.cardNumber(),
				dto.fileLink());
	}
}
