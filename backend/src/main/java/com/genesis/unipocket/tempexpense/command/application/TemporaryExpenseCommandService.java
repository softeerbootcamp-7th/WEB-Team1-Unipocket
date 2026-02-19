package com.genesis.unipocket.tempexpense.command.application;

import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.tempexpense.command.application.command.TemporaryExpenseUpdateCommand;
import com.genesis.unipocket.tempexpense.command.application.result.TemporaryExpenseResult;
import com.genesis.unipocket.tempexpense.command.facade.port.AccountBookRateInfoProvider;
import com.genesis.unipocket.tempexpense.command.persistence.entity.TempExpenseMeta;
import com.genesis.unipocket.tempexpense.command.persistence.entity.TemporaryExpense;
import com.genesis.unipocket.tempexpense.command.persistence.repository.TempExpenseMetaRepository;
import com.genesis.unipocket.tempexpense.command.persistence.repository.TemporaryExpenseRepository;
import com.genesis.unipocket.tempexpense.common.enums.TemporaryExpenseStatus;
import com.genesis.unipocket.tempexpense.common.validation.TemporaryExpenseValidator;
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
	private final TempExpenseMetaRepository tempExpenseMetaRepository;
	private final AccountBookRateInfoProvider accountBookRateInfoProvider;
	private final TemporaryExpenseValidator temporaryExpenseValidator;

	/**
	 * 임시지출내역 단건 조회
	 */
	public TemporaryExpense findById(Long tempExpenseId) {
		return temporaryExpenseRepository
				.findById(tempExpenseId)
				.orElseThrow(() -> new BusinessException(ErrorCode.TEMP_EXPENSE_NOT_FOUND));
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
		var resolvedBaseCountryCode = resolveBaseCountryCode(command, entity);
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
				temporaryExpenseValidator.resolveStatus(
						entity.getStatus(),
						resolvedMerchantName,
						resolvedCategory,
						resolvedLocalCountryCode,
						resolvedLocalCurrencyAmount,
						resolvedBaseCountryCode,
						resolvedOccurredAt);

		// 새로운 엔티티 생성 (불변 객체 패턴)
		TemporaryExpense updated =
				TemporaryExpense.builder()
						.tempExpenseId(entity.getTempExpenseId())
						.tempExpenseMetaId(entity.getTempExpenseMetaId())
						.fileId(entity.getFileId())
						.merchantName(resolvedMerchantName)
						.category(resolvedCategory)
						.localCountryCode(resolvedLocalCountryCode)
						.localCurrencyAmount(resolvedLocalCurrencyAmount)
						.baseCountryCode(resolvedBaseCountryCode)
						.baseCurrencyAmount(resolvedBaseCurrencyAmount)
						.exchangeRate(entity.getExchangeRate())
						.paymentsMethod(resolvedPaymentsMethod)
						.memo(resolvedMemo)
						.occurredAt(resolvedOccurredAt)
						.status(resolvedStatus)
						.cardLastFourDigits(resolvedCardLastFourDigits)
						.approvalNumber(entity.getApprovalNumber())
						.build();

		return TemporaryExpenseResult.from(temporaryExpenseRepository.save(updated));
	}

	private CurrencyCode resolveBaseCountryCode(
			TemporaryExpenseUpdateCommand command, TemporaryExpense entity) {
		if (command.baseCountryCode() != null) {
			return command.baseCountryCode();
		}
		if (entity.getBaseCountryCode() != null) {
			return entity.getBaseCountryCode();
		}
		if (command.baseCurrencyAmount() != null) {
			TempExpenseMeta meta =
					tempExpenseMetaRepository
							.findById(entity.getTempExpenseMetaId())
							.orElseThrow(
									() ->
											new BusinessException(
													ErrorCode.TEMP_EXPENSE_META_NOT_FOUND));
			return accountBookRateInfoProvider
					.getRateInfo(meta.getAccountBookId())
					.baseCurrencyCode();
		}
		return null;
	}

	/**
	 * 임시지출내역 삭제
	 */
	@Transactional
	public void deleteTemporaryExpense(Long tempExpenseId) {
		TemporaryExpense entity = findById(tempExpenseId);
		temporaryExpenseRepository.delete(entity);
	}

	public Long findAccountBookIdByTempExpenseId(Long tempExpenseId) {
		TemporaryExpense tempExpense = findById(tempExpenseId);
		TempExpenseMeta meta =
				tempExpenseMetaRepository
						.findById(tempExpense.getTempExpenseMetaId())
						.orElseThrow(
								() -> new BusinessException(ErrorCode.TEMP_EXPENSE_META_NOT_FOUND));
		return meta.getAccountBookId();
	}

	public Long findMetaIdByTempExpenseId(Long tempExpenseId) {
		TemporaryExpense tempExpense = findById(tempExpenseId);
		return tempExpense.getTempExpenseMetaId();
	}
}
