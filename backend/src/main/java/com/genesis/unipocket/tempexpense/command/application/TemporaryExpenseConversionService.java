package com.genesis.unipocket.tempexpense.command.application;

import com.genesis.unipocket.exchange.common.service.ExchangeRateService;
import com.genesis.unipocket.expense.command.persistence.entity.ExpenseEntity;
import com.genesis.unipocket.expense.command.persistence.entity.dto.ExpenseManualCreateArgs;
import com.genesis.unipocket.expense.command.persistence.repository.ExpenseRepository;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.global.common.enums.ExpenseSource;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.tempexpense.command.application.result.ConfirmStartResult;
import com.genesis.unipocket.tempexpense.command.facade.port.AccountBookRateInfoProvider;
import com.genesis.unipocket.tempexpense.command.facade.port.dto.AccountBookRateInfo;
import com.genesis.unipocket.tempexpense.command.facade.provide.TemporaryExpenseScopeValidationProvider;
import com.genesis.unipocket.tempexpense.command.persistence.entity.File;
import com.genesis.unipocket.tempexpense.command.persistence.entity.File.FileType;
import com.genesis.unipocket.tempexpense.command.persistence.entity.TemporaryExpense;
import com.genesis.unipocket.tempexpense.command.persistence.entity.tempexpense.TempExpenseConversionAmount;
import com.genesis.unipocket.tempexpense.command.persistence.repository.FileRepository;
import com.genesis.unipocket.tempexpense.command.persistence.repository.TemporaryExpenseRepository;
import java.time.ZoneOffset;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@AllArgsConstructor
public class TemporaryExpenseConversionService {

	private final TemporaryExpenseRepository tempExpenseRepository;
	private final FileRepository fileRepository;
	private final ExpenseRepository expenseRepository;
	private final ExchangeRateService exchangeRateService;
	private final AccountBookRateInfoProvider accountBookRateInfoProvider;
	private final TemporaryExpenseScopeValidationProvider temporaryExpenseScopeValidator;

	@Transactional
	public ConfirmStartResult startConfirmAsync(Long accountBookId, Long tempExpenseMetaId) {
		temporaryExpenseScopeValidator.validateMetaScope(accountBookId, tempExpenseMetaId);

		List<TemporaryExpense> expenses =
				tempExpenseRepository.findByTempExpenseMetaId(tempExpenseMetaId);
		if (expenses.isEmpty()) {
			throw new BusinessException(ErrorCode.TEMP_EXPENSE_NOT_FOUND);
		}

		AccountBookRateInfo rateInfo = accountBookRateInfoProvider.getRateInfo(accountBookId);
		CurrencyCode defaultBaseCurrencyCode = rateInfo.baseCurrencyCode();
		CurrencyCode defaultLocalCurrencyCode = rateInfo.localCurrencyCode();

		for (TemporaryExpense expense : expenses) {
			convertOne(accountBookId, expense, defaultBaseCurrencyCode, defaultLocalCurrencyCode);
		}
		return new ConfirmStartResult(null, expenses.size());
	}

	private void convertOne(
			Long accountBookId,
			TemporaryExpense temp,
			CurrencyCode defaultBaseCurrencyCode,
			CurrencyCode defaultLocalCurrencyCode) {
		File file =
				temp.getFileId() != null
						? fileRepository.findById(temp.getFileId()).orElse(null)
						: null;

		var amountInfo = temp.getAmountInfoOrEmpty();
		TempExpenseConversionAmount conversionAmount =
				amountInfo.resolveForConversion(
						defaultLocalCurrencyCode,
						defaultBaseCurrencyCode,
						temp.getOccurredAt().atOffset(ZoneOffset.UTC),
						exchangeRateService);

		ExpenseManualCreateArgs args =
				new ExpenseManualCreateArgs(
						accountBookId,
						temp.getMerchantName(),
						temp.getCategory(),
						null,
						temp.getOccurredAt().atOffset(ZoneOffset.UTC),
						amountInfo.getLocalCurrencyAmount(),
						conversionAmount.localCurrencyCode(),
						conversionAmount.baseCurrencyAmount(),
						conversionAmount.baseCurrencyCode(),
						conversionAmount.calculatedBaseCurrencyAmount(),
						conversionAmount.baseCurrencyCode(),
						temp.getMemo(),
						null,
						conversionAmount.exchangeRate());

		ExpenseEntity expense =
				ExpenseEntity.convertedFromTemporary(
						args,
						resolveExpenseSource(file != null ? file.getFileType() : null),
						file != null ? file.getS3Key() : null,
						temp.getApprovalNumber(),
						temp.getCardLastFourDigits());
		expenseRepository.save(expense);
		tempExpenseRepository.delete(temp);
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
}
