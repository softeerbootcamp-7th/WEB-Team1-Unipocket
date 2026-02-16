package com.genesis.unipocket.tempexpense.query.facade;

import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.tempexpense.command.facade.port.AccountBookOwnershipValidator;
import com.genesis.unipocket.tempexpense.common.enums.TemporaryExpenseStatus;
import com.genesis.unipocket.tempexpense.common.infrastructure.ParsingProgressPublisher;
import com.genesis.unipocket.tempexpense.query.presentation.response.TemporaryExpenseMetaFilesResponse;
import com.genesis.unipocket.tempexpense.query.presentation.response.TemporaryExpenseMetaListResponse;
import com.genesis.unipocket.tempexpense.query.presentation.response.TemporaryExpenseResponse;
import com.genesis.unipocket.tempexpense.query.service.TemporaryExpenseQueryService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@RequiredArgsConstructor
public class TemporaryExpenseQueryFacade {

	private static final long PARSING_SSE_TIMEOUT_MS = 10 * 60 * 1000L;

	private final TemporaryExpenseQueryService temporaryExpenseQueryService;
	private final ParsingProgressPublisher progressPublisher;
	private final AccountBookOwnershipValidator accountBookOwnershipValidator;

	public List<TemporaryExpenseResponse> getTemporaryExpenses(
			Long accountBookId, TemporaryExpenseStatus status, UUID userId) {
		validateOwnership(accountBookId, userId);
		return temporaryExpenseQueryService.getTemporaryExpenses(accountBookId, status);
	}

	public TemporaryExpenseResponse getTemporaryExpense(
			Long accountBookId, Long tempExpenseId, UUID userId) {
		validateOwnership(accountBookId, userId);
		return temporaryExpenseQueryService.getTemporaryExpense(accountBookId, tempExpenseId);
	}

	public TemporaryExpenseMetaListResponse getTemporaryExpenseMetas(
			Long accountBookId, UUID userId) {
		validateOwnership(accountBookId, userId);
		return temporaryExpenseQueryService.getTemporaryExpenseMetas(accountBookId);
	}

	public TemporaryExpenseMetaFilesResponse getTemporaryExpenseMetaFiles(
			Long accountBookId, Long tempExpenseMetaId, UUID userId) {
		validateOwnership(accountBookId, userId);
		return temporaryExpenseQueryService.getTemporaryExpenseMetaFiles(
				accountBookId, tempExpenseMetaId);
	}

	public SseEmitter streamParsingProgress(Long accountBookId, String taskId, UUID userId) {
		validateOwnership(accountBookId, userId);

		if (!progressPublisher.isTaskOwnedBy(taskId, accountBookId)) {
			throw new BusinessException(ErrorCode.TEMP_EXPENSE_PARSE_TASK_NOT_FOUND);
		}

		SseEmitter emitter = new SseEmitter(PARSING_SSE_TIMEOUT_MS);

		progressPublisher.addEmitter(taskId, emitter);
		emitter.onCompletion(() -> progressPublisher.removeEmitter(taskId));
		emitter.onTimeout(() -> progressPublisher.removeEmitter(taskId));
		emitter.onError((e) -> progressPublisher.removeEmitter(taskId));

		return emitter;
	}

	private void validateOwnership(Long accountBookId, UUID userId) {
		accountBookOwnershipValidator.validateOwnership(accountBookId, userId.toString());
	}
}
