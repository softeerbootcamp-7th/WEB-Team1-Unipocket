package com.genesis.unipocket.tempexpense.command.application;

import com.genesis.unipocket.expense.command.persistence.entity.ExpenseEntity;
import com.genesis.unipocket.expense.command.persistence.entity.dto.ExpenseManualCreateArgs;
import com.genesis.unipocket.expense.command.persistence.repository.ExpenseRepository;
import com.genesis.unipocket.expense.common.util.ExchangeAmountCalculator;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.global.common.enums.ExpenseSource;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.tempexpense.command.application.result.ConfirmStartResult;
import com.genesis.unipocket.tempexpense.command.persistence.entity.File;
import com.genesis.unipocket.tempexpense.command.persistence.entity.TemporaryExpense;
import com.genesis.unipocket.tempexpense.command.persistence.repository.FileRepository;
import com.genesis.unipocket.tempexpense.command.persistence.repository.TemporaryExpenseRepository;
import com.genesis.unipocket.tempexpense.common.enums.TemporaryExpenseStatus;
import com.genesis.unipocket.tempexpense.common.exception.TempExpenseConvertValidationException;
import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class TemporaryExpenseConversionService {

	private final TemporaryExpenseRepository tempExpenseRepository;
	private final FileRepository fileRepository;
	private final ExpenseRepository expenseRepository;

	@Transactional
	public ConfirmStartResult convertMetaToExpenses(Long accountBookId, Long tempExpenseMetaId) {
		List<TemporaryExpense> tempExpenses =
				tempExpenseRepository.findByTempExpenseMetaId(tempExpenseMetaId);
		if (tempExpenses.isEmpty()) {
			throw new BusinessException(ErrorCode.TEMP_EXPENSE_NOT_FOUND);
		}

		validateAllNormal(tempExpenses);
		Map<Long, File> fileById =
				fileRepository.findByTempExpenseMetaId(tempExpenseMetaId).stream()
						.collect(Collectors.toMap(File::getFileId, Function.identity()));

		List<ExpenseEntity> expenses =
				tempExpenses.stream().map(temp -> convert(accountBookId, temp, fileById)).toList();
		expenseRepository.saveAll(expenses);
		tempExpenseRepository.deleteAll(tempExpenses);

		return new ConfirmStartResult(expenses.size());
	}

	private void validateAllNormal(List<TemporaryExpense> tempExpenses) {
		List<TempExpenseConvertValidationException.Violation> violations =
				tempExpenses.stream()
						.filter(temp -> temp.getStatus() != TemporaryExpenseStatus.NORMAL)
						.map(
								temp ->
										new TempExpenseConvertValidationException.Violation(
												temp.getTempExpenseId(),
												List.of("statusMustBeNormal")))
						.toList();
		if (!violations.isEmpty()) {
			throw new TempExpenseConvertValidationException(violations);
		}
	}

	private ExpenseEntity convert(
			Long accountBookId, TemporaryExpense temp, Map<Long, File> fileById) {
		File file = fileById.get(temp.getFileId());
		if (file == null) {
			throw new BusinessException(ErrorCode.TEMP_EXPENSE_FILE_NOT_FOUND);
		}
		if (temp.getOccurredAt() == null
				|| temp.getLocalCountryCode() == null
				|| temp.getLocalCurrencyAmount() == null) {
			throw TempExpenseConvertValidationException.single(
					temp.getTempExpenseId(), List.of("requiredFieldsMissing"));
		}

		BigDecimal baseAmount =
				temp.getBaseCurrencyAmount() != null
						? temp.getBaseCurrencyAmount()
						: temp.getLocalCurrencyAmount();
		CurrencyCode baseCurrency =
				temp.getBaseCountryCode() != null
						? temp.getBaseCountryCode()
						: temp.getLocalCountryCode();
		BigDecimal calculatedAmount =
				baseAmount != null ? ExchangeAmountCalculator.scaleAmount(baseAmount) : null;
		BigDecimal exchangeRate = temp.getExchangeRate();
		if (exchangeRate == null
				&& temp.getLocalCurrencyAmount() != null
				&& baseAmount != null
				&& temp.getLocalCurrencyAmount().signum() > 0) {
			exchangeRate =
					ExchangeAmountCalculator.deriveExchangeRate(
							baseAmount, temp.getLocalCurrencyAmount());
		}

		ExpenseManualCreateArgs args =
				new ExpenseManualCreateArgs(
						accountBookId,
						temp.getMerchantName(),
						temp.getCategory(),
						null,
						temp.getOccurredAt().atOffset(ZoneOffset.UTC),
						temp.getLocalCurrencyAmount(),
						temp.getLocalCountryCode(),
						baseAmount,
						baseCurrency,
						calculatedAmount,
						baseCurrency,
						temp.getMemo(),
						null,
						exchangeRate);

		return ExpenseEntity.convertedFromTemporary(
				args,
				resolveExpenseSource(file.getFileType()),
				file.getS3Key(),
				temp.getApprovalNumber(),
				temp.getCardLastFourDigits());
	}

	private ExpenseSource resolveExpenseSource(File.FileType fileType) {
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
