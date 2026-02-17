package com.genesis.unipocket.expense.command.application;

import com.genesis.unipocket.exchange.query.application.ExchangeRateService;
import com.genesis.unipocket.expense.command.application.command.ExpenseCreateCommand;
import com.genesis.unipocket.expense.command.application.command.ExpenseUpdateCommand;
import com.genesis.unipocket.expense.support.ExchangeAmountCalculator;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public final class ExpenseExchangeResolver {

	private ExpenseExchangeResolver() {}

	public static ResolvedExchange resolve(
			ExchangeRateService exchangeRateService, ExpenseCreateCommand command) {
		return resolve(
				exchangeRateService,
				command.localCurrencyAmount(),
				command.baseCurrencyAmount(),
				command.localCurrencyCode(),
				command.baseCurrencyCode(),
				command.occurredAt());
	}

	public static ResolvedExchange resolve(
			ExchangeRateService exchangeRateService, ExpenseUpdateCommand command) {
		return resolve(
				exchangeRateService,
				command.localCurrencyAmount(),
				command.baseCurrencyAmount(),
				command.localCurrencyCode(),
				command.baseCurrencyCode(),
				command.occurredAt());
	}

	public static ResolvedExchange resolve(
			ExchangeRateService exchangeRateService,
			BigDecimal localCurrencyAmount,
			BigDecimal inputBaseCurrencyAmount,
			CurrencyCode localCurrencyCode,
			CurrencyCode baseCurrencyCode,
			OffsetDateTime occurredAt) {
		if (localCurrencyAmount == null && inputBaseCurrencyAmount == null) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
		}

		if (localCurrencyAmount != null && localCurrencyAmount.signum() <= 0) {
			throw new BusinessException(ErrorCode.EXPENSE_INVALID_AMOUNT);
		}
		if (inputBaseCurrencyAmount != null && inputBaseCurrencyAmount.signum() <= 0) {
			throw new BusinessException(ErrorCode.EXPENSE_INVALID_AMOUNT);
		}
		if (localCurrencyAmount != null
				&& inputBaseCurrencyAmount != null
				&& localCurrencyCode == baseCurrencyCode
				&& localCurrencyAmount.compareTo(inputBaseCurrencyAmount) != 0) {
			throw new BusinessException(ErrorCode.EXPENSE_SAME_CURRENCY_AMOUNT_MISMATCH);
		}

		BigDecimal exchangeRate =
				exchangeRateService.getExchangeRate(
						localCurrencyCode, baseCurrencyCode, occurredAt);

		// local only -> base(null), calculated 채움
		if (localCurrencyAmount != null && inputBaseCurrencyAmount == null) {
			BigDecimal calculated =
					ExchangeAmountCalculator.calculateBaseAmount(localCurrencyAmount, exchangeRate);
			return new ResolvedExchange(
					localCurrencyAmount, null, calculated, baseCurrencyCode, exchangeRate);
		}

		// base only -> 입력 base를 calculated로 이관, local 역산
		if (localCurrencyAmount == null) {
			BigDecimal resolvedLocalAmount;
			if (localCurrencyCode == baseCurrencyCode) {
				resolvedLocalAmount = ExchangeAmountCalculator.scaleAmount(inputBaseCurrencyAmount);
			} else {
				resolvedLocalAmount =
						ExchangeAmountCalculator.calculateLocalAmount(
								inputBaseCurrencyAmount, exchangeRate);
			}
			return new ResolvedExchange(
					resolvedLocalAmount,
					null,
					ExchangeAmountCalculator.scaleAmount(inputBaseCurrencyAmount),
					baseCurrencyCode,
					exchangeRate);
		}

		// local + base -> base는 원본 보존, calculated 재계산 채움
		BigDecimal calculated =
				ExchangeAmountCalculator.calculateBaseAmount(localCurrencyAmount, exchangeRate);
		return new ResolvedExchange(
				localCurrencyAmount,
				ExchangeAmountCalculator.scaleAmount(inputBaseCurrencyAmount),
				calculated,
				baseCurrencyCode,
				exchangeRate);
	}

	public record ResolvedExchange(
			BigDecimal localCurrencyAmount,
			BigDecimal baseCurrencyAmount,
			BigDecimal calculatedBaseCurrencyAmount,
			CurrencyCode calculatedBaseCurrencyCode,
			BigDecimal exchangeRate) {}
}
