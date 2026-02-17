package com.genesis.unipocket.expense.command.application;

import com.genesis.unipocket.expense.application.result.ExpenseResult;
import com.genesis.unipocket.expense.command.facade.port.UserCardFetchService;
import com.genesis.unipocket.expense.command.facade.port.dto.UserCardInfo;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ExpenseCommandContextService {

	private final UserCardFetchService userCardFetchService;

	public CurrencyCode resolveLocalCurrencyCode(
			CurrencyCode requestedLocalCurrencyCode, CurrencyCode defaultLocalCurrencyCode) {
		if (requestedLocalCurrencyCode != null) {
			return requestedLocalCurrencyCode;
		}
		return defaultLocalCurrencyCode;
	}

	public ExpenseResult enrichWithCardInfo(ExpenseResult result) {
		if (result.userCardId() == null) {
			return result;
		}
		UserCardInfo cardInfo = userCardFetchService.getUserCard(result.userCardId());
		return new ExpenseResult(
				result.expenseId(),
				result.accountBookId(),
				result.travelId(),
				result.category(),
				result.baseCurrencyCode(),
				result.baseCurrencyAmount(),
				result.exchangeRate(),
				result.localCurrencyCode(),
				result.localCurrencyAmount(),
				result.occurredAt(),
				result.displayMerchantName(),
				result.approvalNumber(),
				result.userCardId(),
				cardInfo.cardCompany(),
				cardInfo.nickName(),
				cardInfo.cardNumber(),
				result.expenseSource(),
				result.fileLink(),
				result.memo(),
				result.cardNumber());
	}
}
