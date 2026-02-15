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
import com.genesis.unipocket.tempexpense.query.presentation.response.FileProcessingSummaryResponse;
import com.genesis.unipocket.tempexpense.query.presentation.response.ImageProcessingSummaryResponse;
import com.genesis.unipocket.tempexpense.query.presentation.response.TemporaryExpenseResponse;
import java.util.ArrayList;
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
	 * 파일(이미지) 단위 처리 현황 조회
	 */
	public FileProcessingSummaryResponse getFileProcessingSummary(Long accountBookId) {
		// 1. 가계부에 속한 메타 조회
		List<TempExpenseMeta> metas = tempExpenseMetaRepository.findByAccountBookId(accountBookId);
		if (metas.isEmpty()) {
			return new FileProcessingSummaryResponse(List.of(), 0, 0, 0);
		}

		// 2. 메타에 속한 파일 조회
		List<Long> metaIds = metas.stream().map(TempExpenseMeta::getTempExpenseMetaId).toList();
		List<File> files = fileRepository.findByTempExpenseMetaIdIn(metaIds);
		Map<Long, File> fileByMetaId =
				files.stream()
						.collect(
								Collectors.toMap(
										File::getTempExpenseMetaId,
										f -> f,
										(existing, replacement) -> existing));

		// 3. 메타별 임시지출내역 조회
		List<TemporaryExpense> allExpenses =
				temporaryExpenseRepository.findByTempExpenseMetaIdIn(metaIds);

		// 4. tempExpenseMetaId 기준으로 그룹핑
		Map<Long, List<TemporaryExpense>> expensesByFile =
				allExpenses.stream()
						.collect(Collectors.groupingBy(TemporaryExpense::getTempExpenseMetaId));

		// 5. 파일별 요약 생성
		List<FileProcessingSummaryResponse.FileSummary> metaSummaries = new ArrayList<>();
		int processedCount = 0;

		for (Long metaId : metaIds) {
			File file = fileByMetaId.get(metaId);
			List<TemporaryExpense> fileExpenses = expensesByFile.getOrDefault(metaId, List.of());

			Map<TemporaryExpenseStatus, Long> counts =
					fileExpenses.stream()
							.collect(
									Collectors.groupingBy(
											TemporaryExpense::getStatus, Collectors.counting()));
			int normalCount = counts.getOrDefault(TemporaryExpenseStatus.NORMAL, 0L).intValue();
			int incompleteCount =
					counts.getOrDefault(TemporaryExpenseStatus.INCOMPLETE, 0L).intValue();
			int abnormalCount = counts.getOrDefault(TemporaryExpenseStatus.ABNORMAL, 0L).intValue();

			boolean processed =
					!fileExpenses.isEmpty() && incompleteCount == 0 && abnormalCount == 0;
			if (processed) processedCount++;

			metaSummaries.add(
					new FileProcessingSummaryResponse.FileSummary(
							metaId,
							file != null ? file.getS3Key() : null,
							file != null && file.getFileType() != null
									? file.getFileType().name()
									: null,
							fileExpenses.size(),
							normalCount,
							incompleteCount,
							abnormalCount,
							processed));
		}

		return new FileProcessingSummaryResponse(
				metaSummaries, metaIds.size(), processedCount, metaIds.size() - processedCount);
	}

	/**
	 * 가계부 전체 이미지 처리 현황 요약 조회
	 */
	public ImageProcessingSummaryResponse getImageProcessingSummary(Long accountBookId) {
		FileProcessingSummaryResponse fileSummary = getFileProcessingSummary(accountBookId);

		if (fileSummary.files().isEmpty()) {
			return new ImageProcessingSummaryResponse(0, 0, 0, 0, 0, 0, 0);
		}

		int totalExpenses =
				fileSummary.files().stream()
						.mapToInt(FileProcessingSummaryResponse.FileSummary::totalExpenses)
						.sum();
		int normalExpenses =
				fileSummary.files().stream()
						.mapToInt(FileProcessingSummaryResponse.FileSummary::normalCount)
						.sum();
		int incompleteExpenses =
				fileSummary.files().stream()
						.mapToInt(FileProcessingSummaryResponse.FileSummary::incompleteCount)
						.sum();
		int abnormalExpenses =
				fileSummary.files().stream()
						.mapToInt(FileProcessingSummaryResponse.FileSummary::abnormalCount)
						.sum();

		return new ImageProcessingSummaryResponse(
				fileSummary.totalFiles(),
				fileSummary.processedFiles(),
				fileSummary.unprocessedFiles(),
				totalExpenses,
				normalExpenses,
				incompleteExpenses,
				abnormalExpenses);
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
