package com.genesis.unipocket.expense.facade;

import com.genesis.unipocket.expense.application.TemporaryExpenseService;
import com.genesis.unipocket.expense.facade.converter.TemporaryExpenseFacadeConverter;
import com.genesis.unipocket.expense.persistence.entity.expense.TemporaryExpense;
import com.genesis.unipocket.expense.presentation.dto.TemporaryExpenseResponse;
import com.genesis.unipocket.expense.presentation.dto.TemporaryExpenseUpdateRequest;
import java.util.List;
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
	private final TemporaryExpenseFacadeConverter converter;

	/**
	 * 가계부 ID로 임시지출내역 목록 조회 (상태 필터 선택)
	 *
	 * @param accountBookId 가계부 ID
	 * @param status        필터링할 상태 (null이면 전체 조회)
	 * @param userId        사용자 ID
	 * @return 임시지출내역 목록
	 */
	public List<TemporaryExpenseResponse> getTemporaryExpenses(
			Long accountBookId, TemporaryExpense.TemporaryExpenseStatus status, Long userId) {
		// TODO: userId - accountBookId 소유권 검증

		List<TemporaryExpense> entities;
		if (status != null) {
			entities = temporaryExpenseService.findByAccountBookIdAndStatus(accountBookId, status);
		} else {
			entities = temporaryExpenseService.findByAccountBookId(accountBookId);
		}
		return converter.toResponseList(entities);
	}

	/**
	 * 임시지출내역 단건 조회
	 */
	public TemporaryExpenseResponse getTemporaryExpense(Long tempExpenseId, Long userId) {
		// TODO: userId - tempExpenseId 소유권 검증 (accountBookId 경유)

		TemporaryExpense entity = temporaryExpenseService.findById(tempExpenseId);
		return converter.toResponse(entity);
	}

	/**
	 * 임시지출내역 수정
	 */
	public TemporaryExpenseResponse updateTemporaryExpense(
			Long tempExpenseId, TemporaryExpenseUpdateRequest request, Long userId) {
		// TODO: userId - tempExpenseId 소유권 검증

		TemporaryExpense updated =
				temporaryExpenseService.updateTemporaryExpense(
						tempExpenseId, converter.toCommand(request));
		return converter.toResponse(updated);
	}

	/**
	 * 임시지출내역 삭제
	 */
	public void deleteTemporaryExpense(Long tempExpenseId, Long userId) {
		// TODO: userId - tempExpenseId 소유권 검증

		temporaryExpenseService.deleteTemporaryExpense(tempExpenseId);
	}
}
