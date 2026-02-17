package com.genesis.unipocket.tempexpense.command.application;

import com.genesis.unipocket.exchange.query.application.ExchangeRateService;
import com.genesis.unipocket.expense.command.persistence.entity.ExpenseEntity;
import com.genesis.unipocket.expense.command.persistence.entity.dto.ExpenseManualCreateArgs;
import com.genesis.unipocket.expense.command.persistence.repository.ExpenseRepository;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.global.common.enums.ExpenseSource;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.tempexpense.command.facade.port.AccountBookRateInfoProvider;
import com.genesis.unipocket.tempexpense.command.persistence.entity.File;
import com.genesis.unipocket.tempexpense.command.persistence.entity.TempExpenseMeta;
import com.genesis.unipocket.tempexpense.command.persistence.entity.TemporaryExpense;
import com.genesis.unipocket.tempexpense.command.persistence.repository.FileRepository;
import com.genesis.unipocket.tempexpense.command.persistence.repository.TempExpenseMetaRepository;
import com.genesis.unipocket.tempexpense.command.persistence.repository.TemporaryExpenseRepository;
import com.genesis.unipocket.tempexpense.common.validation.TemporaryExpenseValidator;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZoneOffset;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TemporaryExpenseSingleConversionTxService {

	private final TemporaryExpenseRepository tempExpenseRepository;
	private final ExpenseRepository expenseRepository;
	private final TempExpenseMetaRepository metaRepository;
	private final FileRepository fileRepository;
	private final ExchangeRateService exchangeRateService;
	private final AccountBookRateInfoProvider accountBookRateInfoProvider;
	private final TemporaryExpenseValidator temporaryExpenseValidator;

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public ExpenseEntity convertToExpense(Long accountBookId, Long tempExpenseId) {
		log.info("Converting temporary expense to permanent: {}", tempExpenseId);

		TemporaryExpense temp =
				tempExpenseRepository
						.findById(tempExpenseId)
						.orElseThrow(() -> new BusinessException(ErrorCode.TEMP_EXPENSE_NOT_FOUND));

		TempExpenseMeta meta =
				metaRepository
						.findById(temp.getTempExpenseMetaId())
						.orElseThrow(
								() -> new BusinessException(ErrorCode.TEMP_EXPENSE_META_NOT_FOUND));
		if (!meta.getAccountBookId().equals(accountBookId)) {
			throw new BusinessException(ErrorCode.TEMP_EXPENSE_SCOPE_MISMATCH);
		}
		CurrencyCode resolvedBaseCurrencyCode = resolveBaseCurrencyCode(temp, meta.getAccountBookId());
		temporaryExpenseValidator.validateConvertible(temp, resolvedBaseCurrencyCode);

		ResolvedAmount resolvedAmount = resolveBaseAmountAndRate(temp, resolvedBaseCurrencyCode);
		File sourceFile =
				fileRepository
						.findById(temp.getFileId())
						.orElseThrow(
								() -> new BusinessException(ErrorCode.TEMP_EXPENSE_FILE_NOT_FOUND));
		if (!temp.getTempExpenseMetaId().equals(sourceFile.getTempExpenseMetaId())) {
			throw new BusinessException(ErrorCode.TEMP_EXPENSE_SCOPE_MISMATCH);
		}
		ExpenseSource source = resolveExpenseSource(sourceFile);
		String fileLink = sourceFile.getS3Key();

		ExpenseManualCreateArgs args =
				new ExpenseManualCreateArgs(
						meta.getAccountBookId(),
						temp.getMerchantName(),
						temp.getCategory(),
						null,
						temp.getOccurredAt().atOffset(ZoneOffset.UTC),
						temp.getLocalCurrencyAmount(),
						temp.getLocalCountryCode() != null ? temp.getLocalCountryCode() : CurrencyCode.KRW,
						resolvedAmount.baseCurrencyAmount(),
						resolvedBaseCurrencyCode,
						resolvedAmount.calculatedBaseCurrencyAmount(),
						resolvedAmount.calculatedBaseCurrencyCode(),
						temp.getMemo(),
						null,
						resolvedAmount.exchangeRate());

		ExpenseEntity expense =
				ExpenseEntity.convertedFromTemporary(
						args,
						source,
						fileLink,
						temp.getApprovalNumber(),
						temp.getCardLastFourDigits());

		ExpenseEntity savedExpense = expenseRepository.save(expense);
		tempExpenseRepository.delete(temp);

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
		BigDecimal exchangeRate = temp.getExchangeRate();

		if (baseAmount != null) {
			if (exchangeRate == null && localAmount.compareTo(BigDecimal.ZERO) > 0) {
				exchangeRate = baseAmount.divide(localAmount, 4, RoundingMode.HALF_UP);
			}
			return new ResolvedAmount(baseAmount, null, null, exchangeRate);
		}

		if (temp.getLocalCountryCode() == resolvedBaseCurrencyCode) {
			return new ResolvedAmount(null, localAmount, resolvedBaseCurrencyCode, BigDecimal.ONE);
		}

		if (exchangeRate == null || exchangeRate.compareTo(BigDecimal.ZERO) <= 0) {
			exchangeRate =
					exchangeRateService.getExchangeRate(
							temp.getLocalCountryCode(),
							resolvedBaseCurrencyCode,
							temp.getOccurredAt().atOffset(ZoneOffset.UTC));
		}

		BigDecimal calculatedBaseAmount =
				localAmount.multiply(exchangeRate).setScale(2, RoundingMode.HALF_UP);
		return new ResolvedAmount(
				null, calculatedBaseAmount, resolvedBaseCurrencyCode, exchangeRate);
	}

	private CurrencyCode resolveBaseCurrencyCode(TemporaryExpense temp, Long accountBookId) {
		if (temp.getBaseCountryCode() != null) {
			return temp.getBaseCountryCode();
		}
		return accountBookRateInfoProvider.getRateInfo(accountBookId).baseCurrencyCode();
	}

	private ExpenseSource resolveExpenseSource(File sourceFile) {
		if (sourceFile == null || sourceFile.getFileType() == null) {
			return ExpenseSource.MANUAL;
		}

		File.FileType fileType = sourceFile.getFileType();
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
