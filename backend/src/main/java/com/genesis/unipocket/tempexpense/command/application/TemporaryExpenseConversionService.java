package com.genesis.unipocket.tempexpense.command.application;

import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.tempexpense.command.application.result.ConfirmStartResult;
import com.genesis.unipocket.tempexpense.command.facade.port.AccountBookRateInfoProvider;
import com.genesis.unipocket.tempexpense.command.facade.provide.TemporaryExpenseScopeValidationProvider;
import com.genesis.unipocket.tempexpense.command.persistence.entity.TemporaryExpense;
import com.genesis.unipocket.tempexpense.command.persistence.repository.TemporaryExpenseRepository;
import com.genesis.unipocket.tempexpense.common.exception.TempExpenseConvertValidationException;
import com.genesis.unipocket.tempexpense.common.infrastructure.sse.ParsingProgressPublisher;
import com.genesis.unipocket.tempexpense.common.util.TemporaryExpenseBatchTaskRunner;
import com.genesis.unipocket.tempexpense.common.validation.TemporaryExpenseValidator;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class TemporaryExpenseConversionService {

	private final TemporaryExpenseRepository tempExpenseRepository;
	private final AccountBookRateInfoProvider accountBookRateInfoProvider;
	private final ParsingProgressPublisher progressPublisher;
	private final TemporaryExpenseSingleConversionTxService singleConversionTxService;
	private final TemporaryExpenseScopeValidationProvider temporaryExpenseScopeValidator;
	private final TemporaryExpenseValidator temporaryExpenseValidator;

	@Qualifier("parsingExecutor") private final Executor parsingExecutor;

	public ConfirmStartResult startConfirmAsync(Long accountBookId, Long tempExpenseMetaId) {
		temporaryExpenseScopeValidator.validateMetaScope(accountBookId, tempExpenseMetaId);

		List<Long> targetIds =
				tempExpenseRepository.findByTempExpenseMetaId(tempExpenseMetaId).stream()
						.map(TemporaryExpense::getTempExpenseId)
						.toList();

		if (targetIds.isEmpty()) {
			throw new BusinessException(ErrorCode.TEMP_EXPENSE_NOT_FOUND);
		}

		validateRequiredFieldsForBatch(accountBookId, targetIds);

		String taskId = UUID.randomUUID().toString();
		progressPublisher.registerTask(taskId, accountBookId);

		parsingExecutor.execute(() -> convertBatch(accountBookId, targetIds, taskId));
		return new ConfirmStartResult(taskId, targetIds.size());
	}

	public void convertBatch(Long accountBookId, List<Long> tempExpenseIds, String taskId) {
		log.info(
				"Starting async batch conversion for task: {}, expenses: {}",
				taskId,
				tempExpenseIds.size());
		TemporaryExpenseBatchTaskRunner.run(
				taskId,
				tempExpenseIds,
				progressPublisher,
				ErrorCode.TEMP_EXPENSE_PARSE_FAILED,
				tempExpenseId -> singleConversionTxService.convertToExpense(accountBookId, tempExpenseId),
				(tempExpenseId, e) -> {
					log.error("Failed to convert temporary expense: {}", tempExpenseId, e);
					return false;
				});
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
