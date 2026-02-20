package com.genesis.unipocket.tempexpense.command.application;

import com.genesis.unipocket.exchange.common.service.ExchangeRateService;
import com.genesis.unipocket.expense.command.persistence.entity.ExpenseEntity;
import com.genesis.unipocket.expense.command.persistence.entity.dto.ExpenseManualCreateArgs;
import com.genesis.unipocket.expense.command.persistence.repository.ExpenseRepository;
import com.genesis.unipocket.expense.common.util.ExchangeAmountCalculator;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.global.common.enums.ExpenseSource;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.tempexpense.command.facade.provide.TemporaryExpenseScopeValidationProvider;
import com.genesis.unipocket.tempexpense.command.facade.port.AccountBookRateInfoProvider;
import com.genesis.unipocket.tempexpense.command.persistence.entity.File.FileType;
import com.genesis.unipocket.tempexpense.command.persistence.entity.TemporaryExpense;
import com.genesis.unipocket.tempexpense.command.persistence.repository.TemporaryExpenseRepository;
import com.genesis.unipocket.tempexpense.command.persistence.repository.dto.TempExpenseConversionContextRow;
import com.genesis.unipocket.tempexpense.common.exception.TempExpenseConvertValidationException;
import com.genesis.unipocket.tempexpense.common.validation.TemporaryExpenseValidator;
import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TemporaryExpenseSingleConversionTxService {

	private final TemporaryExpenseRepository tempExpenseRepository;
	private final ExpenseRepository expenseRepository;
	private final ExchangeRateService exchangeRateService;
	private final AccountBookRateInfoProvider accountBookRateInfoProvider;
	private final TemporaryExpenseScopeValidationProvider temporaryExpenseScopeValidator;
	private final TemporaryExpenseValidator temporaryExpenseValidator;

	@Transactional
	public ExpenseEntity convertToExpense(Long accountBookId, Long tempExpenseId) {
		log.info("Converting temporary expense to permanent: {}", tempExpenseId);

		TemporaryExpense temp =
				tempExpenseRepository
						.findById(tempExpenseId)
						.orElseThrow(() -> new BusinessException(ErrorCode.TEMP_EXPENSE_NOT_FOUND));

		TempExpenseConversionContextRow context =
				tempExpenseRepository
						.findConversionContext(accountBookId, tempExpenseId)
						.orElseThrow(
								() ->
										temporaryExpenseScopeValidator.resolveScopeException(
												accountBookId,
												temp.getTempExpenseMetaId(),
												temp.getFileId()));
		CurrencyCode resolvedBaseCurrencyCode =
				resolveBaseCurrencyCode(temp, context.accountBookId());
		temporaryExpenseValidator.validateConvertible(temp, resolvedBaseCurrencyCode);

		ResolvedAmount resolvedAmount = resolveBaseAmountAndRate(temp, resolvedBaseCurrencyCode);
		ExpenseSource source = resolveExpenseSource(context.fileType());
		String fileLink = context.s3Key();

		ExpenseManualCreateArgs args =
				new ExpenseManualCreateArgs(
						context.accountBookId(),
						temp.getMerchantName(),
						temp.getCategory(),
						null,
						temp.getOccurredAt().atOffset(ZoneOffset.UTC),
						temp.getLocalCurrencyAmount(),
						temp.getLocalCountryCode() != null
								? temp.getLocalCountryCode()
								: accountBookRateInfoProvider
										.getRateInfo(context.accountBookId())
										.localCurrencyCode(),
						resolvedAmount.baseCurrencyAmount(),
						resolvedBaseCurrencyCode,
						resolvedAmount.calculatedBaseCurrencyAmount(),
						resolvedAmount.calculatedBaseCurrencyCode(),
						temp.getMemo(),
						null,
						resolvedAmount.exchangeRate());

		ExpenseEntity savedExpense;
		try {
			ExpenseEntity expense =
					ExpenseEntity.convertedFromTemporary(
							args,
							source,
							fileLink,
							temp.getApprovalNumber(),
							temp.getCardLastFourDigits());
			savedExpense = expenseRepository.save(expense);
			tempExpenseRepository.delete(temp);
		} catch (IllegalArgumentException e) {
			throw TempExpenseConvertValidationException.single(
					temp.getTempExpenseId(), List.of(e.getMessage()));
		}

		log.info(
				"Converted temporary expense {} to expense {}",
				tempExpenseId,
				savedExpense.getExpenseId());
		return savedExpense;
	}

	private ResolvedAmount resolveBaseAmountAndRate(
			TemporaryExpense temp, CurrencyCode resolvedBaseCurrencyCode) {
		BigDecimal localAmount = temp.getLocalCurrencyAmount();
		BigDecimal baseAmount = temp.getBaseCurrencyAmount();
		CurrencyCode localCurrencyCode = temp.getLocalCountryCode();

		BigDecimal resolvedRate;
		if (localCurrencyCode == resolvedBaseCurrencyCode) {
			resolvedRate = BigDecimal.ONE;
		} else {
			resolvedRate =
					exchangeRateService.getExchangeRate(
							localCurrencyCode,
							resolvedBaseCurrencyCode,
							temp.getOccurredAt().atOffset(ZoneOffset.UTC));
		}

		BigDecimal calculatedBaseAmount =
				ExchangeAmountCalculator.calculateBaseAmount(localAmount, resolvedRate);

		return new ResolvedAmount(
				baseAmount, calculatedBaseAmount, resolvedBaseCurrencyCode, resolvedRate);
	}

	private CurrencyCode resolveBaseCurrencyCode(TemporaryExpense temp, Long accountBookId) {
		if (temp.getBaseCountryCode() != null) {
			return temp.getBaseCountryCode();
		}
		return accountBookRateInfoProvider.getRateInfo(accountBookId).baseCurrencyCode();
	}

	private ExpenseSource resolveExpenseSource(FileType fileType) {
		if (fileType == null) {
			return ExpenseSource.MANUAL;
		}

		return switch (fileType) {
			case IMAGE -> ExpenseSource.IMAGE_RECEIPT;
			case CSV -> ExpenseSource.CSV;
			case EXCEL -> ExpenseSource.EXCEL;
		};
	}

	private record ResolvedAmount(
			BigDecimal baseCurrencyAmount,
			BigDecimal calculatedBaseCurrencyAmount,
			CurrencyCode calculatedBaseCurrencyCode,
			BigDecimal exchangeRate) {}
}
