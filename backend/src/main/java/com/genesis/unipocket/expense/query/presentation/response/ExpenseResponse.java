package com.genesis.unipocket.expense.query.presentation.response;

import com.genesis.unipocket.expense.application.result.ExpenseResult;
import com.genesis.unipocket.expense.application.result.ExpenseTravelResult;
import com.genesis.unipocket.expense.command.presentation.response.PaymentMethodResponse;
import com.genesis.unipocket.expense.presentation.support.AmountFormatters;
import com.genesis.unipocket.global.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.global.common.enums.ExpenseSource;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;

/**
 * <b>지출내역 상세 조회 응답 DTO</b>
 *
 * @author bluefishez
 * @since 2026-02-07
 */
public record ExpenseResponse(
		Long expenseId,
		Long accountBookId,
		Travel travel,
		String merchantName,
		BigDecimal exchangeRate,
		Category category,
		PaymentMethodResponse paymentMethod,
		Instant occurredAt,
		OffsetDateTime updatedAt,
		String localCurrencyAmount,
		CurrencyCode localCurrencyCode,
		String baseCurrencyAmount,
		CurrencyCode baseCurrencyCode,
		String memo,
		ExpenseSource source,
		String approvalNumber,
		String cardNumber,
		String fileLink) {

	public static ExpenseResponse from(ExpenseResult dto) {
		return from(dto, null);
	}

	public static ExpenseResponse from(ExpenseResult dto, ExpenseTravelResult travel) {
		return new ExpenseResponse(
				dto.expenseId(),
				dto.accountBookId(),
				Travel.from(travel),
				dto.displayMerchantName(),
				dto.exchangeRate(),
				dto.category(),
				PaymentMethodResponse.from(
						dto.userCardId(), dto.cardCompany(), dto.cardLabel(), dto.cardLastDigits()),
				dto.occurredAt().toInstant(),
				dto.updatedAt(),
				AmountFormatters.toAmountString(dto.localCurrencyAmount()),
				dto.localCurrencyCode(),
				AmountFormatters.toAmountString(dto.baseCurrencyAmount()),
				dto.baseCurrencyCode(),
				dto.memo(),
				dto.expenseSource(),
				dto.approvalNumber(),
				dto.cardNumber(),
				dto.fileLink());
	}

	public record Travel(Long travelId, String name, String imageKey) {
		public static Travel from(ExpenseTravelResult travel) {
			if (travel == null) {
				return null;
			}
			return new Travel(travel.travelId(), travel.name(), travel.imageKey());
		}
	}
}
