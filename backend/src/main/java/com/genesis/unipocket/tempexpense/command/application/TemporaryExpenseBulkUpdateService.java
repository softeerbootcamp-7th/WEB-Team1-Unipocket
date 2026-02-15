package com.genesis.unipocket.tempexpense.command.application;

import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.tempexpense.command.application.command.TemporaryExpenseUpdateCommand;
import com.genesis.unipocket.tempexpense.command.application.result.TemporaryExpenseMetaBulkUpdateResult;
import com.genesis.unipocket.tempexpense.command.application.result.TemporaryExpenseResult;
import com.genesis.unipocket.tempexpense.command.persistence.entity.TempExpenseMeta;
import com.genesis.unipocket.tempexpense.command.persistence.entity.TemporaryExpense;
import com.genesis.unipocket.tempexpense.command.persistence.repository.TempExpenseMetaRepository;
import com.genesis.unipocket.tempexpense.command.persistence.repository.TemporaryExpenseRepository;
import com.genesis.unipocket.tempexpense.command.presentation.request.TemporaryExpenseMetaBulkUpdateRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TemporaryExpenseBulkUpdateService {

	private final TemporaryExpenseCommandService temporaryExpenseCommandService;
	private final TemporaryExpenseRepository temporaryExpenseRepository;
	private final TempExpenseMetaRepository tempExpenseMetaRepository;

	@Transactional
	public TemporaryExpenseMetaBulkUpdateResult updateByMeta(
			Long accountBookId,
			Long tempExpenseMetaId,
			TemporaryExpenseMetaBulkUpdateRequest request) {
		List<TemporaryExpenseMetaBulkUpdateResult.ItemResult> results = new ArrayList<>();
		int successCount = 0;
		List<Long> requestedIds =
				request.items().stream().map(item -> item.tempExpenseId()).toList();

		// findAll 을 이용해서 조회하여 N+1 문제 개선
		Map<Long, TemporaryExpense> expenseById =
				temporaryExpenseRepository.findAllById(requestedIds).stream()
						.collect(
								Collectors.toMap(
										TemporaryExpense::getTempExpenseId, Function.identity()));

		Set<Long> metaIds =
				expenseById.values().stream()
						.map(TemporaryExpense::getTempExpenseMetaId)
						.collect(Collectors.toSet());

		// findAll 을 이용해서 N+1 문제 개선
		Map<Long, TempExpenseMeta> metaById =
				tempExpenseMetaRepository.findAllById(metaIds).stream()
						.collect(
								Collectors.toMap(
										TempExpenseMeta::getTempExpenseMetaId,
										Function.identity()));

		for (var item : request.items()) {
			try {
				TemporaryExpense target = expenseById.get(item.tempExpenseId());
				if (target == null) {
					throw new BusinessException(ErrorCode.TEMP_EXPENSE_NOT_FOUND);
				}

				TempExpenseMeta targetMeta = metaById.get(target.getTempExpenseMetaId());
				if (targetMeta == null) {
					throw new BusinessException(ErrorCode.TEMP_EXPENSE_META_NOT_FOUND);
				}

				if (!accountBookId.equals(targetMeta.getAccountBookId())) {
					throw new BusinessException(ErrorCode.TEMP_EXPENSE_SCOPE_MISMATCH);
				}

				if (!tempExpenseMetaId.equals(target.getTempExpenseMetaId())) {
					throw new BusinessException(ErrorCode.TEMP_EXPENSE_SCOPE_MISMATCH);
				}

				TemporaryExpenseResult updated =
						temporaryExpenseCommandService.updateTemporaryExpense(
								item.tempExpenseId(), TemporaryExpenseUpdateCommand.from(item));

				successCount++;
				results.add(
						new TemporaryExpenseMetaBulkUpdateResult.ItemResult(
								item.tempExpenseId(), "SUCCESS", null, updated));
			} catch (BusinessException e) {
				results.add(
						new TemporaryExpenseMetaBulkUpdateResult.ItemResult(
								item.tempExpenseId(), "FAILED", e.getMessage(), null));
			} catch (Exception e) {
				results.add(
						new TemporaryExpenseMetaBulkUpdateResult.ItemResult(
								item.tempExpenseId(),
								"FAILED",
								ErrorCode.INTERNAL_SERVER_ERROR.getMessage(),
								null));
			}
		}

		return new TemporaryExpenseMetaBulkUpdateResult(
				request.items().size(),
				successCount,
				request.items().size() - successCount,
				results);
	}
}
