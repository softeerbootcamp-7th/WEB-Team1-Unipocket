package com.genesis.unipocket.tempexpense.command.application;

import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.tempexpense.command.application.command.TemporaryExpenseUpdateCommand;
import com.genesis.unipocket.tempexpense.command.application.result.TemporaryExpenseMetaBulkUpdateResult;
import com.genesis.unipocket.tempexpense.command.application.result.TemporaryExpenseResult;
import com.genesis.unipocket.tempexpense.command.presentation.request.TemporaryExpenseMetaBulkUpdateRequest;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TemporaryExpenseBulkUpdateService {

	private final TemporaryExpenseCommandService temporaryExpenseCommandService;

	@Transactional
	public TemporaryExpenseMetaBulkUpdateResult updateByMeta(
			Long accountBookId,
			Long tempExpenseMetaId,
			TemporaryExpenseMetaBulkUpdateRequest request) {
		List<TemporaryExpenseMetaBulkUpdateResult.ItemResult> results = new ArrayList<>();
		int successCount = 0;

		for (var item : request.items()) {
			try {
				Long resourceAccountBookId =
						temporaryExpenseCommandService.findAccountBookIdByTempExpenseId(
								item.tempExpenseId());
				if (!accountBookId.equals(resourceAccountBookId)) {
					throw new BusinessException(ErrorCode.TEMP_EXPENSE_SCOPE_MISMATCH);
				}

				Long resourceMetaId =
						temporaryExpenseCommandService.findMetaIdByTempExpenseId(
								item.tempExpenseId());
				if (!tempExpenseMetaId.equals(resourceMetaId)) {
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
