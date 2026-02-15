package com.genesis.unipocket.expense.command.application.result;

import com.genesis.unipocket.expense.command.facade.port.dto.UserCardInfo;
import com.genesis.unipocket.expense.command.persistence.entity.ExpenseEntity;
import com.genesis.unipocket.global.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.global.common.enums.ExpenseSource;
import com.genesis.unipocket.user.command.persistence.entity.enums.CardCompany;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

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
		OffsetDateTime occurredAt,
		String merchantName,
		String displayMerchantName,
		String approvalNumber,
		Long userCardId,
		CardCompany cardCompany,
		String cardLabel,
		String cardLastDigits,
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
				entity.getDisplayBaseCurrency(),
				entity.getDisplayBaseAmount(),
				entity.getExchangeInfo() != null
						? entity.getExchangeInfo().getLocalCurrencyCode()
						: null,
				entity.getExchangeInfo() != null
						? entity.getExchangeInfo().getLocalCurrencyAmount()
						: null,
				entity.getOccurredAt(),
				entity.getMerchant() != null ? entity.getMerchant().getDisplayMerchantName() : null,
				entity.getMerchant() != null ? entity.getMerchant().getDisplayMerchantName() : null,
				entity.getApprovalNumber(),
				entity.getUserCardId(),
				null,
				null,
				null,
				entity.getExpenseSourceInfo() != null
						? entity.getExpenseSourceInfo().getExpenseSource()
						: null,
				entity.getExpenseSourceInfo() != null
						? entity.getExpenseSourceInfo().getFileLink()
						: null,
				entity.getMemo(),
				entity.getCardNumber());
	}

	public static ExpenseResult from(ExpenseEntity entity, UserCardInfo cardInfo) {
		return new ExpenseResult(
				entity.getExpenseId(),
				entity.getAccountBookId(),
				entity.getTravelId(),
				entity.getCategory(),
				entity.getDisplayBaseCurrency(),
				entity.getDisplayBaseAmount(),
				entity.getExchangeInfo() != null
						? entity.getExchangeInfo().getLocalCurrencyCode()
						: null,
				entity.getExchangeInfo() != null
						? entity.getExchangeInfo().getLocalCurrencyAmount()
						: null,
				entity.getOccurredAt(),
				entity.getMerchant() != null ? entity.getMerchant().getDisplayMerchantName() : null,
				entity.getMerchant() != null ? entity.getMerchant().getDisplayMerchantName() : null,
				entity.getApprovalNumber(),
				entity.getUserCardId(),
				cardInfo != null ? cardInfo.cardCompany() : null,
				cardInfo != null ? cardInfo.nickName() : null,
				cardInfo != null ? cardInfo.cardNumber() : null,
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
