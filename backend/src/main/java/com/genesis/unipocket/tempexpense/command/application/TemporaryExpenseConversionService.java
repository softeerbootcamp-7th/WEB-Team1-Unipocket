package com.genesis.unipocket.tempexpense.command.application;

import com.genesis.unipocket.exchange.query.application.ExchangeRateService;
import com.genesis.unipocket.expense.command.persistence.entity.ExpenseEntity;
import com.genesis.unipocket.expense.command.persistence.entity.dto.ExpenseManualCreateArgs;
import com.genesis.unipocket.expense.command.persistence.repository.ExpenseRepository;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.global.common.enums.ExpenseSource;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.tempexpense.command.application.result.BatchConversionResult;
import com.genesis.unipocket.tempexpense.command.application.result.ConversionResult;
import com.genesis.unipocket.tempexpense.command.persistence.entity.File;
import com.genesis.unipocket.tempexpense.command.persistence.entity.TempExpenseMeta;
import com.genesis.unipocket.tempexpense.command.persistence.entity.TemporaryExpense;
import com.genesis.unipocket.tempexpense.command.persistence.repository.FileRepository;
import com.genesis.unipocket.tempexpense.command.persistence.repository.TempExpenseMetaRepository;
import com.genesis.unipocket.tempexpense.command.persistence.repository.TemporaryExpenseRepository;
import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <b>임시지출내역 변환 서비스</b>
 *
 * @author 김동균
 * @since 2026-02-08
 */
@Slf4j
@Service
@AllArgsConstructor
public class TemporaryExpenseConversionService {

	private final TemporaryExpenseRepository tempExpenseRepository;
	private final ExpenseRepository expenseRepository;
	private final TempExpenseMetaRepository metaRepository;
	private final FileRepository fileRepository;
	private final ExchangeRateService exchangeRateService;

	/**
	 * 단일 변환
	 */
	@Transactional
	public ExpenseEntity convertToExpense(Long accountBookId, Long tempExpenseId) {
		log.info("Converting temporary expense to permanent: {}", tempExpenseId);

		// 1. TemporaryExpense 조회
		TemporaryExpense temp =
				tempExpenseRepository
						.findById(tempExpenseId)
						.orElseThrow(
								() -> new BusinessException(ErrorCode.TEMP_EXPENSE_NOT_FOUND));

		// 2. 필수 필드 검증
		validateRequiredFields(temp);

		// 3. accountBookId 조회 (meta)
		TempExpenseMeta meta =
				metaRepository
						.findById(temp.getTempExpenseMetaId())
						.orElseThrow(() -> new BusinessException(ErrorCode.TEMP_EXPENSE_META_NOT_FOUND));
		if (!meta.getAccountBookId().equals(accountBookId)) {
			throw new BusinessException(ErrorCode.TEMP_EXPENSE_SCOPE_MISMATCH);
		}

		// 4. ExpenseManualCreateArgs 생성 (manual 팩토리 메서드 사용)
		ResolvedAmount resolvedAmount = resolveBaseAmountAndRate(temp);
		List<File> sourceFiles =
				fileRepository.findByTempExpenseMetaId(temp.getTempExpenseMetaId());
		ExpenseSource source = resolveExpenseSource(sourceFiles);
		String fileLink = resolveFileLink(sourceFiles);

		ExpenseManualCreateArgs args =
				new ExpenseManualCreateArgs(
						meta.getAccountBookId(),
						temp.getMerchantName(),
						temp.getCategory(),
						null, // userCardId - 임시지출에서는 카드 정보 없음
						temp.getOccurredAt().atOffset(ZoneOffset.UTC),
						temp.getLocalCurrencyAmount(),
						temp.getLocalCountryCode() != null
								? temp.getLocalCountryCode()
								: com.genesis.unipocket.global.common.enums.CurrencyCode.KRW,
						resolvedAmount.baseCurrencyAmount(),
						temp.getBaseCountryCode() != null
								? temp.getBaseCountryCode()
								: com.genesis.unipocket.global.common.enums.CurrencyCode.KRW,
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

		// 6. Expense 저장
		ExpenseEntity savedExpense = expenseRepository.save(expense);

		// 7. TemporaryExpense 삭제
		tempExpenseRepository.delete(temp);

		log.info(
				"Converted temporary expense {} to expense {}",
				tempExpenseId,
				savedExpense.getExpenseId());

		return savedExpense;
	}

	/**
	 * Batch 변환
	 */
	@Transactional
	public BatchConversionResult convertBatch(Long accountBookId, List<Long> tempExpenseIds) {
		if (tempExpenseIds == null || tempExpenseIds.isEmpty()) {
			return new BatchConversionResult(0, 0, 0, List.of());
		}
		log.info("Starting batch conversion for {} temporary expenses", tempExpenseIds.size());

		List<ConversionResult> results = new ArrayList<>();
		int successCount = 0;
		int failedCount = 0;

		for (Long tempExpenseId : tempExpenseIds) {
			try {
				ExpenseEntity expense = convertToExpense(accountBookId, tempExpenseId);
				results.add(
						new ConversionResult(
								tempExpenseId, expense.getExpenseId(), "SUCCESS", null));
				successCount++;
			} catch (Exception e) {
				log.error("Failed to convert temporary expense: {}", tempExpenseId, e);
				results.add(new ConversionResult(tempExpenseId, null, "FAILED", e.getMessage()));
				failedCount++;
			}
		}

		log.info(
				"Batch conversion completed: {} total, {} success, {} failed",
				tempExpenseIds.size(),
				successCount,
				failedCount);

		return new BatchConversionResult(tempExpenseIds.size(), successCount, failedCount, results);
	}

	@Transactional
	public BatchConversionResult convertMeta(
			Long accountBookId, Long tempExpenseMetaId, List<Long> tempExpenseIds) {
		TempExpenseMeta meta =
				metaRepository
						.findById(tempExpenseMetaId)
						.orElseThrow(() -> new BusinessException(ErrorCode.TEMP_EXPENSE_META_NOT_FOUND));
		if (!meta.getAccountBookId().equals(accountBookId)) {
			throw new BusinessException(ErrorCode.TEMP_EXPENSE_SCOPE_MISMATCH);
		}

		List<Long> targetIds =
				(tempExpenseIds == null || tempExpenseIds.isEmpty())
						? tempExpenseRepository.findByTempExpenseMetaId(tempExpenseMetaId).stream()
								.map(TemporaryExpense::getTempExpenseId)
								.toList()
						: tempExpenseIds;

		return convertBatch(accountBookId, targetIds);
	}

	/**
	 * 필수 필드 검증
	 */
	private void validateRequiredFields(TemporaryExpense temp) {
		if (temp.getMerchantName() == null || temp.getMerchantName().isBlank()) {
			throw new BusinessException(ErrorCode.TEMP_EXPENSE_CONVERT_REQUIRED_FIELDS_MISSING);
		}
		if (temp.getCategory() == null) {
			throw new BusinessException(ErrorCode.TEMP_EXPENSE_CONVERT_REQUIRED_FIELDS_MISSING);
		}
		if (temp.getLocalCountryCode() == null) {
			throw new BusinessException(ErrorCode.TEMP_EXPENSE_CONVERT_REQUIRED_FIELDS_MISSING);
		}
		if (temp.getLocalCurrencyAmount() == null) {
			throw new BusinessException(ErrorCode.TEMP_EXPENSE_CONVERT_REQUIRED_FIELDS_MISSING);
		}
		if (temp.getBaseCountryCode() == null) {
			throw new BusinessException(ErrorCode.TEMP_EXPENSE_CONVERT_REQUIRED_FIELDS_MISSING);
		}
		if (temp.getOccurredAt() == null) {
			throw new BusinessException(ErrorCode.TEMP_EXPENSE_CONVERT_REQUIRED_FIELDS_MISSING);
		}
	}

	private ResolvedAmount resolveBaseAmountAndRate(TemporaryExpense temp) {
		BigDecimal localAmount = temp.getLocalCurrencyAmount();
		BigDecimal baseAmount = temp.getBaseCurrencyAmount();
		BigDecimal exchangeRate = temp.getExchangeRate();

		if (baseAmount != null) {
			if (exchangeRate == null && localAmount.compareTo(BigDecimal.ZERO) > 0) {
				exchangeRate = baseAmount.divide(localAmount, 4, java.math.RoundingMode.HALF_UP);
			}
			return new ResolvedAmount(baseAmount, null, null, exchangeRate);
		}

		if (temp.getLocalCountryCode() == temp.getBaseCountryCode()) {
			return new ResolvedAmount(null, localAmount, temp.getBaseCountryCode(), BigDecimal.ONE);
		}

		if (exchangeRate == null || exchangeRate.compareTo(BigDecimal.ZERO) <= 0) {
			exchangeRate =
					exchangeRateService.getExchangeRate(
							temp.getLocalCountryCode(),
							temp.getBaseCountryCode(),
							temp.getOccurredAt().atOffset(ZoneOffset.UTC));
		}

		BigDecimal calculatedBaseAmount =
				localAmount.multiply(exchangeRate).setScale(2, java.math.RoundingMode.HALF_UP);
		return new ResolvedAmount(
				null, calculatedBaseAmount, temp.getBaseCountryCode(), exchangeRate);
	}

	private ExpenseSource resolveExpenseSource(List<File> files) {
		if (files == null || files.isEmpty()) {
			return ExpenseSource.MANUAL;
		}

		File.FileType fileType = files.get(0).getFileType();
		return switch (fileType) {
			case IMAGE -> ExpenseSource.IMAGE_RECEIPT;
			case CSV -> ExpenseSource.CSV;
			case EXCEL -> ExpenseSource.EXCEL;
		};
	}

	private String resolveFileLink(List<File> files) {
		if (files == null || files.isEmpty()) {
			return null;
		}
		return files.get(0).getS3Key();
	}

	private record ResolvedAmount(
			BigDecimal baseCurrencyAmount,
			BigDecimal calculatedBaseCurrencyAmount,
			CurrencyCode calculatedBaseCurrencyCode,
			BigDecimal exchangeRate) {}
}
