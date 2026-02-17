package com.genesis.unipocket.tempexpense.command.application;

import com.genesis.unipocket.exchange.query.application.ExchangeRateService;
import com.genesis.unipocket.expense.command.persistence.entity.ExpenseEntity;
import com.genesis.unipocket.expense.command.persistence.entity.dto.ExpenseManualCreateArgs;
import com.genesis.unipocket.expense.command.persistence.repository.ExpenseRepository;
import com.genesis.unipocket.expense.support.ExchangeAmountCalculator;
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
import com.genesis.unipocket.tempexpense.common.exception.TempExpenseConvertValidationException;
import com.genesis.unipocket.tempexpense.common.validation.TemporaryExpenseValidator;
import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.util.List;
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
		CurrencyCode resolvedBaseCurrencyCode =
				resolveBaseCurrencyCode(temp, meta.getAccountBookId());
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
						temp.getLocalCountryCode() != null
								? temp.getLocalCountryCode()
								: accountBookRateInfoProvider
										.getRateInfo(accountBookId)
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
			// 변환 시점에는 임시 데이터에 저장된 과거 환율을 재사용하지 않고,
			// 반드시 occurredAt 기준 환율을 다시 조회해 manual 생성 경로와 정합성을 맞춘다.
			resolvedRate =
					exchangeRateService.getExchangeRate(
							localCurrencyCode,
							resolvedBaseCurrencyCode,
							temp.getOccurredAt().atOffset(ZoneOffset.UTC));
		}

		BigDecimal calculatedBaseAmount =
				ExchangeAmountCalculator.calculateBaseAmount(localAmount, resolvedRate);

		return new ResolvedAmount(
				baseAmount,
				calculatedBaseAmount,
				resolvedBaseCurrencyCode,
				resolvedRate);
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
