package com.genesis.unipocket.expense.query.presentation.response;

import com.genesis.unipocket.expense.common.util.AmountFormatters;
import com.genesis.unipocket.expense.query.persistence.response.ExpenseOneShotRow;
import com.genesis.unipocket.global.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.global.common.enums.ExpenseSource;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

public record ExpenseResponse(
		Long expenseId,
		Long accountBookId,
		Travel travel,
		String merchantName,
		BigDecimal exchangeRate,
		Category category,
		PaymentMethodResponse paymentMethod,
		OffsetDateTime occurredAt,
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

	public record CardResponse(Long userCardId, Integer company, String label, String lastDigits) {}

	public record PaymentMethodResponse(boolean isCash, CardResponse card) {}

	public record Travel(Long travelId, String name, String imageKey) {}

	public static ExpenseResponse from(ExpenseOneShotRow row, ZoneId localZoneId) {
		return new ExpenseResponse(
				row.expenseId(),
				row.accountBookId(),
				toTravel(row),
				row.merchantName(),
				row.exchangeRate(),
				row.category(),
				row.userCardId() != null
						? new PaymentMethodResponse(
								false,
								new CardResponse(
										row.userCardId(),
										row.cardCompany() != null
												? row.cardCompany().ordinal()
												: null,
										row.cardLabel(),
										row.cardLastDigits()))
						: new PaymentMethodResponse(true, null),
				row.occurredAt().atZoneSameInstant(localZoneId).toOffsetDateTime(),
				row.updatedAt() != null ? row.updatedAt().atOffset(ZoneOffset.UTC) : null,
				AmountFormatters.toAmountString(row.localCurrencyAmount()),
				row.localCurrencyCode(),
				AmountFormatters.toAmountString(row.baseCurrencyAmount()),
				row.baseCurrencyCode(),
				row.memo(),
				row.source(),
				row.approvalNumber(),
				row.expenseCardNumber(),
				row.fileLink());
	}

	private static Travel toTravel(ExpenseOneShotRow row) {
		if (row.travelId() == null) {
			return null;
		}
		if (row.travelName() == null && row.travelImageKey() == null) {
			return null;
		}
		return new Travel(row.travelId(), row.travelName(), row.travelImageKey());
	}
}
