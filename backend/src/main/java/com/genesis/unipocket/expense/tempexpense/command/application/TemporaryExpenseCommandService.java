package com.genesis.unipocket.expense.tempexpense.command.application;

import com.genesis.unipocket.expense.common.enums.Category;
import com.genesis.unipocket.expense.tempexpense.command.application.command.TemporaryExpenseUpdateCommand;
import com.genesis.unipocket.expense.tempexpense.command.application.result.TemporaryExpenseResult;
import com.genesis.unipocket.expense.tempexpense.command.persistence.entity.TemporaryExpense;
import com.genesis.unipocket.expense.tempexpense.command.persistence.entity.TemporaryExpense.TemporaryExpenseStatus;
import com.genesis.unipocket.expense.tempexpense.command.persistence.repository.TemporaryExpenseRepository;
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
public class TemporaryExpenseCommandService {

	private final TemporaryExpenseRepository temporaryExpenseRepository;

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
	public TemporaryExpenseResult updateTemporaryExpense(
			Long tempExpenseId, TemporaryExpenseUpdateCommand command) {
		TemporaryExpense entity = findById(tempExpenseId);

		// 필드 값 결정 (command가 null이 아니면 command 값, 아니면 기존 값)
		String resolvedMerchantName =
				command.merchantName() != null ? command.merchantName() : entity.getMerchantName();
		var resolvedCategory =
				command.category() != null ? command.category() : entity.getCategory();
		var resolvedLocalCountryCode =
				command.localCountryCode() != null
						? command.localCountryCode()
						: entity.getLocalCountryCode();
		var resolvedLocalCurrencyAmount =
				command.localCurrencyAmount() != null
						? command.localCurrencyAmount()
						: entity.getLocalCurrencyAmount();
		var resolvedBaseCountryCode =
				command.baseCountryCode() != null
						? command.baseCountryCode()
						: entity.getBaseCountryCode();
		var resolvedBaseCurrencyAmount =
				command.baseCurrencyAmount() != null
						? command.baseCurrencyAmount()
						: entity.getBaseCurrencyAmount();
		String resolvedPaymentsMethod =
				command.paymentsMethod() != null
						? command.paymentsMethod()
						: entity.getPaymentsMethod();
		String resolvedMemo = command.memo() != null ? command.memo() : entity.getMemo();
		var resolvedOccurredAt =
				command.occurredAt() != null ? command.occurredAt() : entity.getOccurredAt();
		String resolvedCardLastFourDigits =
				command.cardLastFourDigits() != null
						? command.cardLastFourDigits()
						: entity.getCardLastFourDigits();

		// 상태 재평가
		TemporaryExpenseStatus resolvedStatus =
				reevaluateStatus(
						entity.getStatus(),
						resolvedMerchantName,
						resolvedLocalCurrencyAmount,
						resolvedOccurredAt,
						resolvedCategory);

		// 새로운 엔티티 생성 (불변 객체 패턴)
		TemporaryExpense updated =
				TemporaryExpense.builder()
						.tempExpenseId(entity.getTempExpenseId())
						.tempExpenseMetaId(entity.getTempExpenseMetaId())
						.merchantName(resolvedMerchantName)
						.category(resolvedCategory)
						.localCountryCode(resolvedLocalCountryCode)
						.localCurrencyAmount(resolvedLocalCurrencyAmount)
						.baseCountryCode(resolvedBaseCountryCode)
						.baseCurrencyAmount(resolvedBaseCurrencyAmount)
						.paymentsMethod(resolvedPaymentsMethod)
						.memo(resolvedMemo)
						.occurredAt(resolvedOccurredAt)
						.status(resolvedStatus)
						.cardLastFourDigits(resolvedCardLastFourDigits)
						.build();

		return TemporaryExpenseResult.from(temporaryExpenseRepository.save(updated));
	}

	/**
	 * 임시지출내역 삭제
	 */
	@Transactional
	public void deleteTemporaryExpense(Long tempExpenseId) {
		TemporaryExpense entity = findById(tempExpenseId);
		temporaryExpenseRepository.delete(entity);
	}

	/**
	 * 수정 후 상태 재평가
	 * <p>
	 * ABNORMAL 상태는 유지합니다 (자동 변경 불가).
	 * 필수 필드(merchantName, localCurrencyAmount, occurredAt, category)가
	 * 모두 채워지면 NORMAL, 하나라도 빠지면 INCOMPLETE.
	 * </p>
	 */
	private TemporaryExpenseStatus reevaluateStatus(
			TemporaryExpenseStatus originalStatus,
			String merchantName,
			java.math.BigDecimal localCurrencyAmount,
			java.time.LocalDateTime occurredAt,
			Category category) {
		// ABNORMAL 상태는 자동 변경하지 않음
		if (originalStatus == TemporaryExpenseStatus.ABNORMAL) {
			return TemporaryExpenseStatus.ABNORMAL;
		}

		// 필수 필드 검증
		boolean hasAllRequired =
				merchantName != null
						&& !merchantName.isBlank()
						&& localCurrencyAmount != null
						&& occurredAt != null
						&& category != null;

		return hasAllRequired ? TemporaryExpenseStatus.NORMAL : TemporaryExpenseStatus.INCOMPLETE;
	}
}
