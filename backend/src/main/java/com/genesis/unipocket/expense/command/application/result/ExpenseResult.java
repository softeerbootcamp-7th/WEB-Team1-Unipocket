package com.genesis.unipocket.expense.command.application.result;

import com.genesis.unipocket.expense.command.persistence.entity.expense.ExpenseEntity;
import com.genesis.unipocket.expense.common.enums.Category;
import com.genesis.unipocket.expense.common.enums.ExpenseSource;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <b>지출내역 결과 DTO</b>
 * <p>Application -> Facade
 *
 * @author bluefishez
 * @since 2026-02-10
 */
public record ExpenseResult(
		Long expenseId,
		Long accountBookId,
		Long travelId,
		Category category,
		CurrencyCode baseCurrencyCode,
		BigDecimal baseCurrencyAmount,
		CurrencyCode localCurrencyCode,
		BigDecimal localCurrencyAmount,
		LocalDateTime occurredAt,
		String merchantName,
		String displayMerchantName,
		String approvalNumber,
		String paymentMethod,
		ExpenseSource expenseSource,
		String fileLink,
		String memo,
		String cardNumber) {

	public static ExpenseResult from(ExpenseEntity entity) {
		return new ExpenseResult(
				entity.getExpenseId(),
				entity.getAccountBookId(),
				entity.getTravelId(),
				entity.getCategory(),
				entity.getExchangeInfo() != null
						? entity.getExchangeInfo().getBaseCurrencyCode()
						: null,
				entity.getExchangeInfo() != null
						? entity.getExchangeInfo().getBaseCurrencyAmount()
						: null,
				entity.getExchangeInfo() != null
						? entity.getExchangeInfo().getLocalCurrencyCode()
						: null,
				entity.getExchangeInfo() != null
						? entity.getExchangeInfo().getLocalCurrencyAmount()
						: null,
				entity.getOccurredAt(),
				entity.getMerchant() != null ? entity.getMerchant().getMerchantName() : null,
				entity.getMerchant() != null ? entity.getMerchant().getDisplayMerchantName() : null,
				entity.getApprovalNumber(),
				entity.getPaymentMethod(),
				entity.getExpenseSourceInfo() != null
						? entity.getExpenseSourceInfo().getExpenseSource()
						: null,
				entity.getExpenseSourceInfo() != null
						? entity.getExpenseSourceInfo().getFileLink()
						: null,
				entity.getMemo(),
				entity.getCardNumber());
	}
}
