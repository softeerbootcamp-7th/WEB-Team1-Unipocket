package com.genesis.unipocket.expense.command.application;

import com.genesis.unipocket.expense.command.application.command.ExpenseCreateCommand;
import com.genesis.unipocket.expense.command.application.command.ExpenseUpdateCommand;
import com.genesis.unipocket.expense.command.application.result.ExpenseResult;
import com.genesis.unipocket.expense.command.persistence.entity.dto.ExpenseManualCreateArgs;
import com.genesis.unipocket.expense.command.persistence.entity.expense.ExpenseEntity;
import com.genesis.unipocket.expense.command.persistence.repository.ExpenseRepository;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <b>지출내역 엔티티 관련 서비스 클래스</b>
 *
 * @author codingbaraGo
 * @since 2026-02-03
 */
@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class ExpenseCommandService {
	private final ExpenseRepository expenseRepository;
	private final ExchangeRateService exchangeRateService;

	@Transactional
	public ExpenseResult createExpenseManual(ExpenseCreateCommand command) {

		// 환율 정보 바탕으로 기준 환율 값 계산
		BigDecimal baseCurrencyAmount =
				exchangeRateService.convertAmount(
						command.localCurrencyAmount(),
						command.localCurrencyCode(),
						command.baseCurrencyCode(),
						command.occurredAt());

		ExpenseEntity expenseEntity =
				ExpenseEntity.manual(
						ExpenseManualCreateArgs.of(command, baseCurrencyAmount));

		var savedEntity = expenseRepository.save(expenseEntity);

		return ExpenseResult.from(savedEntity);
	}

	@Transactional
	public ExpenseResult updateExpense(ExpenseUpdateCommand command) {
		ExpenseEntity entity =
				findAndVerifyOwnership(command.expenseId(), command.accountBookId());

		// 기본 필드 업데이트
		entity.updateMerchantName(command.merchantName());
		entity.updateCategory(command.category());
		entity.updatePaymentMethod(command.paymentMethod());
		entity.updateMemo(command.memo());
		entity.updateOccurredAt(command.occurredAt());
		entity.updateTravelId(command.travelId());

		// 통화/금액 변경 시 환율 재계산
		boolean currencyChanged =
				!entity.getLocalCurrency().equals(command.localCurrencyCode());
		boolean amountChanged =
				entity.getLocalAmount().compareTo(command.localCurrencyAmount()) != 0;

		if (currencyChanged || amountChanged) {
			BigDecimal baseCurrencyAmount =
					exchangeRateService.convertAmount(
							command.localCurrencyAmount(),
							command.localCurrencyCode(),
							command.baseCurrencyCode(),
							command.occurredAt());

			entity.updateExchangeInfo(
					command.localCurrencyCode(),
					command.localCurrencyAmount(),
					command.baseCurrencyCode(),
					baseCurrencyAmount);
		}

		return ExpenseResult.from(entity);
	}

	@Transactional
	public void deleteExpense(Long expenseId, Long accountBookId) {
		ExpenseEntity entity = findAndVerifyOwnership(expenseId, accountBookId);
		expenseRepository.delete(entity);
	}

	private ExpenseEntity findAndVerifyOwnership(Long expenseId, Long accountBookId) {
		ExpenseEntity entity =
				expenseRepository
						.findById(expenseId)
						.orElseThrow(
								() ->
										new com.genesis.unipocket.global.exception.BusinessException(
												com.genesis.unipocket.global.exception.ErrorCode
														.EXPENSE_NOT_FOUND));

		if (!entity.getAccountBookId().equals(accountBookId)) {
			throw new com.genesis.unipocket.global.exception.BusinessException(
					com.genesis.unipocket.global.exception.ErrorCode.EXPENSE_UNAUTHORIZED_ACCESS);
		}

		return entity;
	}
}
