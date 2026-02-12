package com.genesis.unipocket.expense.query.service;

import com.genesis.unipocket.expense.command.persistence.entity.expense.File;
import com.genesis.unipocket.expense.command.persistence.entity.expense.TempExpenseMeta;
import com.genesis.unipocket.expense.command.persistence.entity.expense.TemporaryExpense;
import com.genesis.unipocket.expense.command.persistence.entity.expense.TemporaryExpense.TemporaryExpenseStatus;
import com.genesis.unipocket.expense.command.persistence.repository.FileRepository;
import com.genesis.unipocket.expense.command.persistence.repository.TempExpenseMetaRepository;
import com.genesis.unipocket.expense.command.persistence.repository.TemporaryExpenseRepository;
import com.genesis.unipocket.expense.common.validator.AccountBookOwnershipValidator;
import com.genesis.unipocket.expense.query.presentation.response.FileProcessingSummaryResponse;
import com.genesis.unipocket.expense.query.presentation.response.ImageProcessingSummaryResponse;
import com.genesis.unipocket.expense.query.presentation.response.TemporaryExpenseResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
	private final AccountBookOwnershipValidator accountBookOwnershipValidator;

	public List<TemporaryExpenseResponse> getTemporaryExpenses(
			Long accountBookId, TemporaryExpense.TemporaryExpenseStatus status, UUID userId) {
		accountBookOwnershipValidator.validateOwnership(accountBookId, userId.toString());

		List<TemporaryExpense> entities =
				status != null
						? temporaryExpenseRepository.findByAccountBookIdAndStatus(
								accountBookId, status)
						: temporaryExpenseRepository.findByAccountBookId(accountBookId);

		return TemporaryExpenseResponse.fromList(entities);
	}

	public TemporaryExpenseResponse getTemporaryExpense(Long tempExpenseId, UUID userId) {
		TemporaryExpense tempExpense = findById(tempExpenseId);
		Long accountBookId = getAccountBookIdFromTempExpense(tempExpense);
		accountBookOwnershipValidator.validateOwnership(accountBookId, userId.toString());

		return TemporaryExpenseResponse.from(tempExpense);
	}

	/**
	 * 파일(이미지) 단위 처리 현황 조회
	 */
	public FileProcessingSummaryResponse getFileProcessingSummary(Long accountBookId, UUID userId) {
		accountBookOwnershipValidator.validateOwnership(accountBookId, userId.toString());

		// 1. 가계부에 속한 메타 조회
		List<TempExpenseMeta> metas = tempExpenseMetaRepository.findByAccountBookId(accountBookId);
		if (metas.isEmpty()) {
			return new FileProcessingSummaryResponse(List.of(), 0, 0, 0);
		}

		// 2. 메타에 속한 파일 조회
		List<Long> metaIds = metas.stream().map(TempExpenseMeta::getTempExpenseMetaId).toList();
		List<File> files = fileRepository.findByTempExpenseMetaIdIn(metaIds);

		// 3. 파일별 임시지출내역 조회
		List<Long> fileIds = files.stream().map(File::getFileId).toList();
		List<TemporaryExpense> allExpenses = temporaryExpenseRepository.findByFileIdIn(fileIds);

		// 4. fileId 기준으로 그룹핑
		Map<Long, List<TemporaryExpense>> expensesByFile =
				allExpenses.stream().collect(Collectors.groupingBy(TemporaryExpense::getFileId));

		// 5. 파일별 요약 생성
		List<FileProcessingSummaryResponse.FileSummary> fileSummaries = new ArrayList<>();
		int processedCount = 0;

		for (File file : files) {
			List<TemporaryExpense> fileExpenses =
					expensesByFile.getOrDefault(file.getFileId(), List.of());

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

			fileSummaries.add(
					new FileProcessingSummaryResponse.FileSummary(
							file.getFileId(),
							file.getS3Key(),
							file.getFileType() != null ? file.getFileType().name() : null,
							fileExpenses.size(),
							normalCount,
							incompleteCount,
							abnormalCount,
							processed));
		}

		return new FileProcessingSummaryResponse(
				fileSummaries, files.size(), processedCount, files.size() - processedCount);
	}

	/**
	 * 가계부 전체 이미지 처리 현황 요약 조회
	 */
	public ImageProcessingSummaryResponse getImageProcessingSummary(
			Long accountBookId, UUID userId) {
		// getFileProcessingSummary는 내부적으로 ownership 검증을 수행하므로, 여기서 중복으로 호출할 필요가 없습니다.
		FileProcessingSummaryResponse fileSummary = getFileProcessingSummary(accountBookId, userId);

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
				.orElseThrow(
						() ->
								new IllegalArgumentException(
										"임시지출내역을 찾을 수 없습니다. ID: " + tempExpenseId));
	}

	private Long getAccountBookIdFromTempExpense(TemporaryExpense tempExpense) {
		File file =
				fileRepository
						.findById(tempExpense.getFileId())
						.orElseThrow(() -> new IllegalArgumentException("파일을 찾을 수 없습니다."));
		TempExpenseMeta meta =
				tempExpenseMetaRepository
						.findById(file.getTempExpenseMetaId())
						.orElseThrow(() -> new IllegalArgumentException("메타데이터를 찾을 수 없습니다."));
		return meta.getAccountBookId();
	}
}
