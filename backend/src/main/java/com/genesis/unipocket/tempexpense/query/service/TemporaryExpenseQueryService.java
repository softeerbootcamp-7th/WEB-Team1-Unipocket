package com.genesis.unipocket.tempexpense.query.service;

import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.tempexpense.command.persistence.entity.File;
import com.genesis.unipocket.tempexpense.command.persistence.entity.TempExpenseMeta;
import com.genesis.unipocket.tempexpense.command.persistence.entity.TemporaryExpense;
import com.genesis.unipocket.tempexpense.command.persistence.repository.FileRepository;
import com.genesis.unipocket.tempexpense.command.persistence.repository.TempExpenseMetaRepository;
import com.genesis.unipocket.tempexpense.command.persistence.repository.TemporaryExpenseRepository;
import com.genesis.unipocket.tempexpense.common.enums.TemporaryExpenseStatus;
import com.genesis.unipocket.tempexpense.query.presentation.response.TemporaryExpenseMetaFilesResponse;
import com.genesis.unipocket.tempexpense.query.presentation.response.TemporaryExpenseMetaListResponse;
import com.genesis.unipocket.tempexpense.query.presentation.response.TemporaryExpenseResponse;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <b>임시지출내역 조회 서비스</b>
 *
 * @author 김동균
 * @since 2026-02-10
 */
@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class TemporaryExpenseQueryService {

	private final TemporaryExpenseRepository temporaryExpenseRepository;
	private final FileRepository fileRepository;
	private final TempExpenseMetaRepository tempExpenseMetaRepository;

	public List<TemporaryExpenseResponse> getTemporaryExpenses(
			Long accountBookId, TemporaryExpenseStatus status) {
		List<TemporaryExpense> entities =
				status != null
						? temporaryExpenseRepository.findByAccountBookIdAndStatus(
								accountBookId, status)
						: temporaryExpenseRepository.findByAccountBookId(accountBookId);

		return TemporaryExpenseResponse.fromList(entities);
	}

	public TemporaryExpenseResponse getTemporaryExpense(Long accountBookId, Long tempExpenseId) {
		TemporaryExpense tempExpense = findById(tempExpenseId);
		Long resourceAccountBookId = getAccountBookIdFromTempExpense(tempExpense);
		if (!accountBookId.equals(resourceAccountBookId)) {
			throw new BusinessException(ErrorCode.TEMP_EXPENSE_SCOPE_MISMATCH);
		}

		return TemporaryExpenseResponse.from(tempExpense);
	}

	/**
	 * 가계부에 속한 메타 목록 조회
	 */
	public TemporaryExpenseMetaListResponse getTemporaryExpenseMetas(Long accountBookId) {
		List<TempExpenseMeta> metas = tempExpenseMetaRepository.findByAccountBookId(accountBookId);
		if (metas.isEmpty()) {
			return new TemporaryExpenseMetaListResponse(List.of());
		}

		List<Long> metaIds = metas.stream().map(TempExpenseMeta::getTempExpenseMetaId).toList();
		Map<Long, Integer> fileCountByMetaId =
				fileRepository.countFilesByTempExpenseMetaIdIn(metaIds).stream()
						.collect(
								Collectors.toMap(
										row -> (Long) row[0],
										row -> ((Long) row[1]).intValue()));

		Map<Long, List<TemporaryExpense>> expensesByMetaId =
				temporaryExpenseRepository.findByTempExpenseMetaIdIn(metaIds).stream()
						.collect(Collectors.groupingBy(TemporaryExpense::getTempExpenseMetaId));

		List<TemporaryExpenseMetaListResponse.MetaSummary> summaries =
				metas.stream()
						.map(
								meta -> {
									List<TemporaryExpense> expenses =
											expensesByMetaId.getOrDefault(
													meta.getTempExpenseMetaId(), List.of());
									Map<TemporaryExpenseStatus, Long> counts =
											expenses.stream()
													.collect(
															Collectors.groupingBy(
																	TemporaryExpense::getStatus,
																	Collectors.counting()));
									return new TemporaryExpenseMetaListResponse.MetaSummary(
											meta.getTempExpenseMetaId(),
											meta.getCreatedAt(),
											fileCountByMetaId.getOrDefault(
													meta.getTempExpenseMetaId(), 0),
											expenses.size(),
											counts.getOrDefault(TemporaryExpenseStatus.NORMAL, 0L)
													.intValue(),
											counts.getOrDefault(
															TemporaryExpenseStatus.INCOMPLETE, 0L)
													.intValue(),
											counts.getOrDefault(TemporaryExpenseStatus.ABNORMAL, 0L)
													.intValue());
								})
						.toList();

		return new TemporaryExpenseMetaListResponse(summaries);
	}

	/**
	 * 메타 내부 파일별 임시지출 상세 조회
	 */
	public TemporaryExpenseMetaFilesResponse getTemporaryExpenseMetaFiles(
			Long accountBookId, Long tempExpenseMetaId) {
		TempExpenseMeta meta =
				tempExpenseMetaRepository
						.findById(tempExpenseMetaId)
						.orElseThrow(
								() -> new BusinessException(ErrorCode.TEMP_EXPENSE_META_NOT_FOUND));
		if (!accountBookId.equals(meta.getAccountBookId())) {
			throw new BusinessException(ErrorCode.TEMP_EXPENSE_SCOPE_MISMATCH);
		}

		List<File> files = fileRepository.findByTempExpenseMetaId(tempExpenseMetaId);
		if (files.isEmpty()) {
			return new TemporaryExpenseMetaFilesResponse(
					tempExpenseMetaId, meta.getCreatedAt(), List.of());
		}

		Map<Long, List<TemporaryExpense>> expensesByFileId =
				temporaryExpenseRepository
						.findByFileIdIn(files.stream().map(File::getFileId).toList())
						.stream()
						.collect(Collectors.groupingBy(TemporaryExpense::getFileId));

		List<TemporaryExpenseMetaFilesResponse.FileExpenses> fileExpenses =
				files.stream()
						.map(
								file ->
										new TemporaryExpenseMetaFilesResponse.FileExpenses(
												file.getFileId(),
												file.getS3Key(),
												file.getFileType() != null
														? file.getFileType().name()
														: null,
												expensesByFileId
														.getOrDefault(file.getFileId(), List.of())
														.stream()
														.map(TemporaryExpenseResponse::from)
														.toList()))
						.toList();

		return new TemporaryExpenseMetaFilesResponse(
				tempExpenseMetaId, meta.getCreatedAt(), fileExpenses);
	}

	private TemporaryExpense findById(Long tempExpenseId) {
		return temporaryExpenseRepository
				.findById(tempExpenseId)
				.orElseThrow(() -> new BusinessException(ErrorCode.TEMP_EXPENSE_NOT_FOUND));
	}

	private Long getAccountBookIdFromTempExpense(TemporaryExpense tempExpense) {
		TempExpenseMeta meta =
				tempExpenseMetaRepository
						.findById(tempExpense.getTempExpenseMetaId())
						.orElseThrow(
								() -> new BusinessException(ErrorCode.TEMP_EXPENSE_META_NOT_FOUND));
		return meta.getAccountBookId();
	}
}
