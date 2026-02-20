package com.genesis.unipocket.tempexpense.command.application;

import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.tempexpense.command.application.result.BatchConversionResult;
import com.genesis.unipocket.tempexpense.command.application.result.ConfirmStartResult;
import com.genesis.unipocket.tempexpense.command.application.result.ConversionResult;
import com.genesis.unipocket.tempexpense.command.facade.port.AccountBookRateInfoProvider;
import com.genesis.unipocket.tempexpense.command.persistence.entity.TempExpenseMeta;
import com.genesis.unipocket.tempexpense.command.persistence.entity.TemporaryExpense;
import com.genesis.unipocket.tempexpense.command.persistence.repository.TempExpenseMetaRepository;
import com.genesis.unipocket.tempexpense.command.persistence.repository.TemporaryExpenseRepository;
import com.genesis.unipocket.tempexpense.common.exception.TempExpenseConvertValidationException;
import com.genesis.unipocket.tempexpense.common.infrastructure.ParsingProgressPublisher;
import com.genesis.unipocket.tempexpense.common.validation.TemporaryExpenseValidator;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

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
	private final TempExpenseMetaRepository metaRepository;
	private final AccountBookRateInfoProvider accountBookRateInfoProvider;
	private final ParsingProgressPublisher progressPublisher;
	private final TemporaryExpenseSingleConversionTxService singleConversionTxService;
	private final TemporaryExpenseValidator temporaryExpenseValidator;

	@Qualifier("parsingExecutor") private final Executor parsingExecutor;

	/**
	 * 메타 단위 비동기 확정 시작
	 */
	public ConfirmStartResult startConfirmAsync(Long accountBookId, Long tempExpenseMetaId) {
		TempExpenseMeta meta =
				metaRepository
						.findById(tempExpenseMetaId)
						.orElseThrow(
								() -> new BusinessException(ErrorCode.TEMP_EXPENSE_META_NOT_FOUND));
		if (!meta.getAccountBookId().equals(accountBookId)) {
			throw new BusinessException(ErrorCode.TEMP_EXPENSE_SCOPE_MISMATCH);
		}

		List<Long> targetIds =
				tempExpenseRepository.findByTempExpenseMetaId(tempExpenseMetaId).stream()
						.map(TemporaryExpense::getTempExpenseId)
						.toList();

		if (targetIds.isEmpty()) {
			throw new BusinessException(ErrorCode.TEMP_EXPENSE_NOT_FOUND);
		}

		validateRequiredFieldsForBatch(meta.getAccountBookId(), targetIds);

		String taskId = UUID.randomUUID().toString();
		progressPublisher.registerTask(taskId, accountBookId);

		CompletableFuture.runAsync(
				() -> convertBatchAsync(accountBookId, targetIds, taskId), parsingExecutor);
		return new ConfirmStartResult(taskId, targetIds.size());
	}

	public CompletableFuture<BatchConversionResult> convertBatchAsync(
			Long accountBookId, List<Long> tempExpenseIds, String taskId) {
		log.info(
				"Starting async batch conversion for task: {}, expenses: {}",
				taskId,
				tempExpenseIds.size());
		try {
			int totalExpenses = tempExpenseIds.size();
			int completed = 0;
			List<ConversionResult> results = new ArrayList<>();
			int successCount = 0;
			int failedCount = 0;

			for (Long tempExpenseId : tempExpenseIds) {
				progressPublisher.publishProgress(
						taskId,
						new ParsingProgressPublisher.ParsingProgressEvent(
								completed,
								totalExpenses,
								"tempExpenseId=" + tempExpenseId,
								(completed * 100) / totalExpenses));
				try {
					var expense =
							singleConversionTxService.convertToExpense(
									accountBookId, tempExpenseId);
					results.add(
							new ConversionResult(
									tempExpenseId, expense.getExpenseId(), "SUCCESS", null));
					successCount++;
				} catch (Exception e) {
					log.error("Failed to convert temporary expense: {}", tempExpenseId, e);
					String errorMessage = resolveClientErrorMessage(e);
					results.add(new ConversionResult(tempExpenseId, null, "FAILED", errorMessage));
					failedCount++;
					progressPublisher.publishFileError(
							taskId,
							new ParsingProgressPublisher.FileErrorEvent(
									completed + 1,
									totalExpenses,
									"tempExpenseId=" + tempExpenseId,
									((completed + 1) * 100) / totalExpenses,
									errorMessage));
				}
				completed++;
			}

			BatchConversionResult finalResult =
					new BatchConversionResult(
							totalExpenses, successCount, failedCount, List.copyOf(results));
			progressPublisher.complete(taskId, finalResult);
			return CompletableFuture.completedFuture(finalResult);
		} catch (Exception e) {
			log.error("Batch conversion failed before completion. taskId={}", taskId, e);
			String errorMessage = resolveClientErrorMessage(e);
			progressPublisher.publishError(taskId, errorMessage);
			return CompletableFuture.failedFuture(e);
		}
	}

	private String resolveClientErrorMessage(Exception e) {
		if (e instanceof BusinessException businessException) {
			return businessException.getCode().getMessage();
		}
		return ErrorCode.TEMP_EXPENSE_PARSE_FAILED.getMessage();
	}

	private void validateRequiredFieldsForBatch(Long accountBookId, List<Long> tempExpenseIds) {
		List<TemporaryExpense> expenses = tempExpenseRepository.findAllById(tempExpenseIds);
		if (expenses.size() != tempExpenseIds.size()) {
			throw new BusinessException(ErrorCode.TEMP_EXPENSE_NOT_FOUND);
		}
		CurrencyCode defaultBaseCurrencyCode =
				accountBookRateInfoProvider.getRateInfo(accountBookId).baseCurrencyCode();
		List<TempExpenseConvertValidationException.Violation> violations = new ArrayList<>();
		for (TemporaryExpense expense : expenses) {
			CurrencyCode resolvedBaseCurrencyCode =
					resolveBaseCurrencyCode(expense, defaultBaseCurrencyCode);
			List<String> missingOrInvalidFields =
					temporaryExpenseValidator.findMissingOrInvalidFields(
							expense, resolvedBaseCurrencyCode);
			if (!missingOrInvalidFields.isEmpty()) {
				violations.add(
						new TempExpenseConvertValidationException.Violation(
								expense.getTempExpenseId(), missingOrInvalidFields));
			}
		}
		if (!violations.isEmpty()) {
			throw new TempExpenseConvertValidationException(violations);
		}
	}

	private CurrencyCode resolveBaseCurrencyCode(
			TemporaryExpense temp, CurrencyCode defaultBaseCurrencyCode) {
		if (temp.getBaseCountryCode() != null) {
			return temp.getBaseCountryCode();
		}
		return defaultBaseCurrencyCode;
	}
}
