package com.genesis.unipocket.expense.command.presentation.response;

import com.genesis.unipocket.expense.command.application.result.ExpenseResult;
import com.genesis.unipocket.expense.common.enums.Category;
import com.genesis.unipocket.expense.common.enums.ExpenseSource;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <b>지출내역 수정 응답 DTO</b>
 *
 * @author bluefishez
 * @since 2026-02-10
 */
public record ExpenseUpdateResponse(
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

	public static ExpenseUpdateResponse from(ExpenseResult result) {
		return new ExpenseUpdateResponse(
				result.expenseId(),
				result.accountBookId(),
				result.travelId(),
				result.merchantName(),
				result.displayMerchantName(),
				result.category(),
				result.paymentMethod(),
				result.occurredAt(),
				result.localCurrencyAmount(),
				result.localCurrencyCode(),
				result.baseCurrencyAmount(),
				result.baseCurrencyCode(),
				result.memo(),
				result.expenseSource(),
				result.approvalNumber(),
				result.cardNumber(),
				result.fileLink());
	}
}
