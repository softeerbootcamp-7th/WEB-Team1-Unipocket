package com.genesis.unipocket.expense.application;

import com.genesis.unipocket.expense.application.dto.TemporaryExpenseUpdateCommand;
import com.genesis.unipocket.expense.persistence.entity.expense.TemporaryExpense;
import com.genesis.unipocket.expense.persistence.repository.TemporaryExpenseRepository;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <b>임시지출내역 서비스 클래스</b>
 *
 * @author 김동균
 * @since 2026-02-08
 */
@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class TemporaryExpenseService {

	private final TemporaryExpenseRepository temporaryExpenseRepository;

	/**
	 * 가계부 ID로 임시지출내역 목록 조회
	 */
	public List<TemporaryExpense> findByAccountBookId(Long accountBookId) {
		return temporaryExpenseRepository.findByAccountBookId(accountBookId);
	}

	/**
	 * 가계부 ID + 상태로 임시지출내역 목록 조회
	 */
	public List<TemporaryExpense> findByAccountBookIdAndStatus(
			Long accountBookId, TemporaryExpense.TemporaryExpenseStatus status) {
		return temporaryExpenseRepository.findByAccountBookIdAndStatus(accountBookId, status);
	}

	/**
	 * 임시지출내역 단건 조회
	 */
	public TemporaryExpense findById(Long tempExpenseId) {
		return temporaryExpenseRepository
				.findById(tempExpenseId)
				.orElseThrow(
						() ->
								new IllegalArgumentException(
										"임시지출내역을 찾을 수 없습니다. ID: " + tempExpenseId));
	}

	/**
	 * 임시지출내역 수정
	 */
	@Transactional
	public TemporaryExpense updateTemporaryExpense(
			Long tempExpenseId, TemporaryExpenseUpdateCommand command) {
		TemporaryExpense entity = findById(tempExpenseId);

		// 새로운 엔티티 생성 (불변 객체 패턴)
		TemporaryExpense updated =
				TemporaryExpense.builder()
						.tempExpenseId(entity.getTempExpenseId())
						.fileId(entity.getFileId())
						.merchantName(
								command.merchantName() != null
										? command.merchantName()
										: entity.getMerchantName())
						.category(
								command.category() != null
										? command.category()
										: entity.getCategory())
						.localCountryCode(
								command.localCountryCode() != null
										? command.localCountryCode()
										: entity.getLocalCountryCode())
						.localCurrencyAmount(
								command.localCurrencyAmount() != null
										? command.localCurrencyAmount()
										: entity.getLocalCurrencyAmount())
						.baseCountryCode(
								command.baseCountryCode() != null
										? command.baseCountryCode()
										: entity.getBaseCountryCode())
						.baseCurrencyAmount(
								command.baseCurrencyAmount() != null
										? command.baseCurrencyAmount()
										: entity.getBaseCurrencyAmount())
						.paymentsMethod(
								command.paymentsMethod() != null
										? command.paymentsMethod()
										: entity.getPaymentsMethod())
						.memo(command.memo() != null ? command.memo() : entity.getMemo())
						.occurredAt(
								command.occurredAt() != null
										? command.occurredAt()
										: entity.getOccurredAt())
						.status(entity.getStatus())
						.cardLastFourDigits(
								command.cardLastFourDigits() != null
										? command.cardLastFourDigits()
										: entity.getCardLastFourDigits())
						.build();

		return temporaryExpenseRepository.save(updated);
	}

	/**
	 * 임시지출내역 삭제
	 */
	@Transactional
	public void deleteTemporaryExpense(Long tempExpenseId) {
		TemporaryExpense entity = findById(tempExpenseId);
		temporaryExpenseRepository.delete(entity);
	}
}
