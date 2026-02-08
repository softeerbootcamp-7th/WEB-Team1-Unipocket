package com.genesis.unipocket.expense.service;

import com.genesis.unipocket.expense.dto.common.ExpenseDto;
import com.genesis.unipocket.expense.dto.request.ExpenseManualCreateRequest;
import com.genesis.unipocket.expense.dto.request.ExpenseSearchFilter;
import com.genesis.unipocket.expense.dto.request.ExpenseUpdateRequest;
import com.genesis.unipocket.expense.persistence.entity.dto.ExpenseManualCreateArgs;
import com.genesis.unipocket.expense.persistence.entity.expense.ExpenseEntity;
import com.genesis.unipocket.expense.persistence.repository.ExpenseRepository;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
public class ExpenseService {
	private final ExpenseRepository expenseRepository;
	private final ExchangeRateService exchangeRateService;

	@Transactional
	public ExpenseDto createExpenseManual(
			ExpenseManualCreateRequest request, Long accountBookId, CurrencyCode baseCurrencyCode) {

		// 환율 정보 바탕으로 기준 환율 값 계산
		BigDecimal baseCurrencyAmount =
				exchangeRateService.convertAmount(
						request.localCurrencyAmount(),
						request.localCurrencyCode(),
						baseCurrencyCode,
						request.occurredAt());

		ExpenseEntity expenseEntity =
				ExpenseEntity.manual(
						ExpenseManualCreateArgs.of(
								request, accountBookId, baseCurrencyCode, baseCurrencyAmount));

		var savedEntity = expenseRepository.save(expenseEntity);

		return ExpenseDto.from(savedEntity);
	}

	public ExpenseDto getExpense(Long expenseId, Long accountBookId) {
		ExpenseEntity entity = findAndVerifyOwnership(expenseId, accountBookId);
		return ExpenseDto.from(entity);
	}

	public Page<ExpenseDto> getExpenses(
			Long accountBookId, ExpenseSearchFilter filter, Pageable pageable) {
		Page<ExpenseEntity> entities;

		if (filter == null || isFilterEmpty(filter)) {
			entities = expenseRepository.findByAccountBookId(accountBookId, pageable);
		} else {
			entities =
					expenseRepository.findByFilters(
							accountBookId,
							filter.startDate(),
							filter.endDate(),
							filter.category(),
							filter.minAmount(),
							filter.maxAmount(),
							filter.merchantName(),
							filter.travelId(),
							pageable);
		}

		return entities.map(ExpenseDto::from);
	}

	@Transactional
	public ExpenseDto updateExpense(
			Long expenseId,
			Long accountBookId,
			ExpenseUpdateRequest request,
			CurrencyCode baseCurrencyCode) {
		ExpenseEntity entity = findAndVerifyOwnership(expenseId, accountBookId);

		// 기본 필드 업데이트
		entity.updateMerchantName(request.merchantName());
		entity.updateCategory(request.category());
		entity.updatePaymentMethod(request.paymentMethod());
		entity.updateMemo(request.memo());
		entity.updateOccurredAt(request.occurredAt());
		entity.updateTravelId(request.travelId());

		// 통화/금액 변경 시 환율 재계산
		boolean currencyChanged = !entity.getLocalCurrency().equals(request.localCurrencyCode());
		boolean amountChanged =
				entity.getLocalAmount().compareTo(request.localCurrencyAmount()) != 0;

		if (currencyChanged || amountChanged) {
			BigDecimal baseCurrencyAmount =
					exchangeRateService.convertAmount(
							request.localCurrencyAmount(),
							request.localCurrencyCode(),
							baseCurrencyCode,
							request.occurredAt());

			entity.updateExchangeInfo(
					request.localCurrencyCode(),
					request.localCurrencyAmount(),
					baseCurrencyCode,
					baseCurrencyAmount);
		}

		return ExpenseDto.from(entity);
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
						.orElseThrow(() -> new BusinessException(ErrorCode.EXPENSE_NOT_FOUND));

		if (!entity.getAccountBookId().equals(accountBookId)) {
			throw new BusinessException(ErrorCode.EXPENSE_UNAUTHORIZED_ACCESS);
		}

		return entity;
	}

	private boolean isFilterEmpty(ExpenseSearchFilter filter) {
		return filter.startDate() == null
				&& filter.endDate() == null
				&& filter.category() == null
				&& filter.minAmount() == null
				&& filter.maxAmount() == null
				&& filter.merchantName() == null
				&& filter.travelId() == null;
	}
}
