package com.genesis.unipocket.expense.facade;

import com.genesis.unipocket.accountbook.service.AccountBookService;
import com.genesis.unipocket.expense.dto.request.TemporaryExpenseUpdateRequest;
import com.genesis.unipocket.expense.dto.response.TemporaryExpenseResponse;
import com.genesis.unipocket.expense.persistence.entity.dto.TemporaryExpenseUpdateCommand;
import com.genesis.unipocket.expense.persistence.entity.expense.File;
import com.genesis.unipocket.expense.persistence.entity.expense.TempExpenseMeta;
import com.genesis.unipocket.expense.persistence.entity.expense.TemporaryExpense;
import com.genesis.unipocket.expense.persistence.repository.FileRepository;
import com.genesis.unipocket.expense.persistence.repository.TempExpenseMetaRepository;
import com.genesis.unipocket.expense.service.TemporaryExpenseService;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * <b>임시지출내역 Facade 클래스</b>
 * <p>
 * 임시지출내역 도메인에 대한 요청 처리
 *
 * @author 김동균
 * @since 2026-02-08
 */
@Service
@AllArgsConstructor
public class TemporaryExpenseOrchestrator {

	private final TemporaryExpenseService temporaryExpenseService;
	private final FileRepository fileRepository;
	private final TempExpenseMetaRepository tempExpenseMetaRepository;
	private final AccountBookService accountBookService;

	/**
	 * 가계부 ID로 임시지출내역 목록 조회 (상태 필터 선택)
	 *
	 * @param accountBookId 가계부 ID
	 * @param status        필터링할 상태 (null이면 전체 조회)
	 * @param userId        사용자 ID
	 * @return 임시지출내역 목록
	 */
	public List<TemporaryExpenseResponse> getTemporaryExpenses(
			Long accountBookId, TemporaryExpense.TemporaryExpenseStatus status, UUID userId) {
		// Validate ownership
		accountBookService.getAccountBook(accountBookId, userId.toString());

		List<TemporaryExpense> entities;
		if (status != null) {
			entities = temporaryExpenseService.findByAccountBookIdAndStatus(accountBookId, status);
		} else {
			entities = temporaryExpenseService.findByAccountBookId(accountBookId);
		}
		return TemporaryExpenseResponse.fromList(entities);
	}

	/**
	 * 임시지출내역 단건 조회
	 */
	public TemporaryExpenseResponse getTemporaryExpense(Long tempExpenseId, UUID userId) {
		// Validate ownership by checking accountBookId
		TemporaryExpense tempExpense = temporaryExpenseService.findById(tempExpenseId);
		Long accountBookId = getAccountBookIdFromTempExpense(tempExpense);
		accountBookService.getAccountBook(accountBookId, userId.toString());

		return TemporaryExpenseResponse.from(tempExpense);
	}

	/**
	 * 임시지출내역 수정
	 */
	public TemporaryExpenseResponse updateTemporaryExpense(
			Long tempExpenseId, TemporaryExpenseUpdateRequest request, UUID userId) {
		// Validate ownership
		TemporaryExpense tempExpense = temporaryExpenseService.findById(tempExpenseId);
		Long accountBookId = getAccountBookIdFromTempExpense(tempExpense);
		accountBookService.getAccountBook(accountBookId, userId.toString());

		TemporaryExpense updated =
				temporaryExpenseService.updateTemporaryExpense(
						tempExpenseId, TemporaryExpenseUpdateCommand.from(request));
		return TemporaryExpenseResponse.from(updated);
	}

	/**
	 * 임시지출내역 삭제
	 */
	public void deleteTemporaryExpense(Long tempExpenseId, UUID userId) {
		// Validate ownership
		TemporaryExpense tempExpense = temporaryExpenseService.findById(tempExpenseId);
		Long accountBookId = getAccountBookIdFromTempExpense(tempExpense);
		accountBookService.getAccountBook(accountBookId, userId.toString());

		temporaryExpenseService.deleteTemporaryExpense(tempExpenseId);
	}

	/**
	 * Helper method to get accountBookId from temporary expense
	 */
	private Long getAccountBookIdFromTempExpense(TemporaryExpense tempExpense) {
		// Get accountBookId via file → meta
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
