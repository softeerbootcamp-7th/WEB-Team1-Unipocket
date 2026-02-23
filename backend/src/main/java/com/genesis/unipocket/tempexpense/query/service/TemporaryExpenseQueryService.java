package com.genesis.unipocket.tempexpense.query.service;

import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.tempexpense.common.enums.TemporaryExpenseStatus;
import com.genesis.unipocket.tempexpense.common.util.TempExpenseFileNameResolver;
import com.genesis.unipocket.tempexpense.query.persistence.repository.TemporaryExpenseQueryRepository;
import com.genesis.unipocket.tempexpense.query.persistence.response.TemporaryExpenseFileRow;
import com.genesis.unipocket.tempexpense.query.persistence.response.TemporaryExpenseMetaRow;
import com.genesis.unipocket.tempexpense.query.persistence.response.TemporaryExpenseMetaSummaryRow;
import com.genesis.unipocket.tempexpense.query.presentation.response.TemporaryExpenseMetaFilesResponse;
import com.genesis.unipocket.tempexpense.query.presentation.response.TemporaryExpenseMetaListResponse;
import com.genesis.unipocket.tempexpense.query.presentation.response.TemporaryExpenseResponse;
import com.genesis.unipocket.tempexpense.query.service.port.TempExpenseMediaAccessService;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TemporaryExpenseQueryService {

	private final TemporaryExpenseQueryRepository temporaryExpenseQueryRepository;
	private final TempExpenseMediaAccessService tempExpenseMediaAccessService;

	@Value("${app.media.presigned-get-expiration-seconds:600}")
	private int presignedGetExpirationSeconds;

	public TemporaryExpenseMetaListResponse getTemporaryExpenseMetas(Long accountBookId) {
		List<TemporaryExpenseMetaSummaryRow> metas =
				temporaryExpenseQueryRepository.findMetaSummariesByAccountBookId(accountBookId);
		if (metas.isEmpty()) {
			return new TemporaryExpenseMetaListResponse(List.of());
		}

		List<TemporaryExpenseMetaListResponse.MetaSummary> summaries =
				metas.stream().map(this::toMetaSummary).toList();

		return new TemporaryExpenseMetaListResponse(summaries);
	}

	public TemporaryExpenseMetaFilesResponse getTemporaryExpenseMetaFiles(
			Long accountBookId, Long tempExpenseMetaId) {
		TemporaryExpenseMetaRow meta = resolveScopedMeta(accountBookId, tempExpenseMetaId);

		List<TemporaryExpenseFileRow> files =
				temporaryExpenseQueryRepository.findFilesByMetaId(tempExpenseMetaId);
		if (files.isEmpty()) {
			return new TemporaryExpenseMetaFilesResponse(
					tempExpenseMetaId, meta.createdAt(), List.of());
		}

		Map<Long, List<TemporaryExpenseResponse>> expensesByFileId =
				temporaryExpenseQueryRepository
						.findExpensesByFileIds(
								files.stream().map(TemporaryExpenseFileRow::fileId).toList())
						.stream()
						.map(TemporaryExpenseResponse::from)
						.collect(Collectors.groupingBy(TemporaryExpenseResponse::fileId));

		List<TemporaryExpenseMetaFilesResponse.FileExpenses> fileExpenses =
				files.stream()
						.map(
								file ->
										toFileExpenses(
												file,
												expensesByFileId.getOrDefault(
														file.fileId(), List.of())))
						.toList();

		return new TemporaryExpenseMetaFilesResponse(
				tempExpenseMetaId, meta.createdAt(), fileExpenses);
	}

	public TemporaryExpenseMetaFilesResponse.FileExpenses getTemporaryExpenseMetaFile(
			Long accountBookId, Long tempExpenseMetaId, Long fileId) {
		TemporaryExpenseFileRow file = getValidatedFile(accountBookId, tempExpenseMetaId, fileId);

		List<TemporaryExpenseResponse> expenses =
				temporaryExpenseQueryRepository.findExpensesByFileId(fileId).stream()
						.map(TemporaryExpenseResponse::from)
						.toList();

		return toFileExpenses(file, expenses);
	}

	public String issueTemporaryExpenseFileUrl(
			Long accountBookId, Long tempExpenseMetaId, Long fileId) {
		TemporaryExpenseFileRow file = getValidatedFile(accountBookId, tempExpenseMetaId, fileId);
		return tempExpenseMediaAccessService.issueGetPath(
				file.s3Key(), Duration.ofSeconds(presignedGetExpirationSeconds));
	}

	public int getTemporaryExpenseFileUrlExpirationSeconds() {
		return presignedGetExpirationSeconds;
	}

	private TemporaryExpenseMetaListResponse.MetaSummary toMetaSummary(
			TemporaryExpenseMetaSummaryRow meta) {
		int normalCount = (int) meta.normalCount();
		int incompleteCount = (int) meta.incompleteCount();
		int abnormalCount = (int) meta.abnormalCount();
		int totalExpenseCount = normalCount + incompleteCount + abnormalCount;
		return new TemporaryExpenseMetaListResponse.MetaSummary(
				meta.tempExpenseMetaId(),
				meta.createdAt(),
				(int) meta.fileCount(),
				totalExpenseCount,
				normalCount,
				incompleteCount,
				abnormalCount);
	}

	private TemporaryExpenseMetaRow resolveScopedMeta(Long accountBookId, Long tempExpenseMetaId) {
		return temporaryExpenseQueryRepository
				.findMetaInAccountBook(accountBookId, tempExpenseMetaId)
				.orElseThrow(
						() -> {
							boolean exists =
									temporaryExpenseQueryRepository
											.findMetaById(tempExpenseMetaId)
											.isPresent();
							return new BusinessException(
									exists
											? ErrorCode.TEMP_EXPENSE_SCOPE_MISMATCH
											: ErrorCode.TEMP_EXPENSE_META_NOT_FOUND);
						});
	}

	private TemporaryExpenseFileRow getValidatedFile(
			Long accountBookId, Long tempExpenseMetaId, Long fileId) {
		resolveScopedMeta(accountBookId, tempExpenseMetaId);
		return temporaryExpenseQueryRepository
				.findScopedFile(accountBookId, tempExpenseMetaId, fileId)
				.orElseThrow(() -> new BusinessException(ErrorCode.TEMP_EXPENSE_SCOPE_MISMATCH));
	}

	private TemporaryExpenseMetaFilesResponse.FileExpenses toFileExpenses(
			TemporaryExpenseFileRow file, List<TemporaryExpenseResponse> expenses) {
		StatusCount statusCount = countStatuses(expenses);
		return new TemporaryExpenseMetaFilesResponse.FileExpenses(
				file.fileId(),
				file.s3Key(),
				TempExpenseFileNameResolver.resolveOrFallback(file.fileName(), file.s3Key()),
				file.fileType() != null ? String.valueOf(file.fileType()) : null,
				statusCount.normalCount(),
				statusCount.incompleteCount(),
				statusCount.abnormalCount(),
				expenses);
	}

	private StatusCount countStatuses(List<TemporaryExpenseResponse> expenses) {
		int normalCount = 0;
		int incompleteCount = 0;
		int abnormalCount = 0;

		for (TemporaryExpenseResponse expense : expenses) {
			if (TemporaryExpenseStatus.NORMAL.name().equals(expense.status())) {
				normalCount++;
			} else if (TemporaryExpenseStatus.INCOMPLETE.name().equals(expense.status())) {
				incompleteCount++;
			} else if (TemporaryExpenseStatus.ABNORMAL.name().equals(expense.status())) {
				abnormalCount++;
			}
		}

		return new StatusCount(normalCount, incompleteCount, abnormalCount);
	}

	private record StatusCount(int normalCount, int incompleteCount, int abnormalCount) {}
}
