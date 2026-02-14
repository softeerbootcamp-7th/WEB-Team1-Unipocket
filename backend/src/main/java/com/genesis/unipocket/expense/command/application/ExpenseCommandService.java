package com.genesis.unipocket.expense.command.application;

import com.genesis.unipocket.exchange.query.application.ExchangeRateService;
import com.genesis.unipocket.expense.command.application.command.ExpenseCreateCommand;
import com.genesis.unipocket.expense.command.application.command.ExpenseUpdateCommand;
import com.genesis.unipocket.expense.command.application.result.ExpenseResult;
import com.genesis.unipocket.expense.command.persistence.entity.ExpenseEntity;
import com.genesis.unipocket.expense.command.persistence.entity.dto.ExpenseManualCreateArgs;
import com.genesis.unipocket.expense.command.persistence.repository.ExpenseRepository;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

		// TODO: 환율 정보 바탕으로 기준 환율 값 계산
		BigDecimal baseCurrencyAmount = exchangeRateService.convertAmount(
				command.localCurrencyAmount(),
				command.localCurrencyCode(),
				command.baseCurrencyCode(),
				command.occurredAt());

		ExpenseEntity expenseEntity = ExpenseEntity.manual(ExpenseManualCreateArgs.of(command, baseCurrencyAmount));

		var savedEntity = expenseRepository.save(expenseEntity);

		return ExpenseResult.from(savedEntity);
	}

	@Transactional
	public ExpenseResult updateExpense(ExpenseUpdateCommand command) {
		ExpenseEntity entity = findAndVerifyOwnership(command.expenseId(), command.accountBookId());

		// 기본 필드 업데이트
		entity.updateMerchantName(command.merchantName());
		entity.updateCategory(command.category());
		entity.updateUserCardId(command.userCardId());
		entity.updateMemo(command.memo());
		entity.updateOccurredAt(command.occurredAt());
		entity.updateTravelId(command.travelId());

		// 통화/금액 변경 시 환율 재계산
		boolean currencyChanged = !entity.getLocalCurrency().equals(command.localCurrencyCode());
		boolean amountChanged = entity.getLocalAmount().compareTo(command.localCurrencyAmount()) != 0;

		if (currencyChanged || amountChanged) {
			BigDecimal baseCurrencyAmount = exchangeRateService.convertAmount(
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

	@Transactional
	public void updateBaseCurrency(Long accountBookId, CurrencyCode newBaseCurrencyCode) {
		List<ExpenseEntity> expenses = expenseRepository.findAllByAccountBookId(accountBookId);

		// 1. 필요한 환율 정보 식별 (LocalCurrency, Date)
		Set<ExchangeRateKey> rateKeys = new HashSet<>();
		for (ExpenseEntity expense : expenses) {
			rateKeys.add(
					new ExchangeRateKey(
							expense.getLocalCurrency(), expense.getOccurredAt().toLocalDate()));
		}

		// 2. 환율 정보 조회 (Batch 조회 대신 반복 조회 - 로직 단순화)
		// 실제로는 Batch 조회가 성능상 유리하지만, 현재 ExchangeRateService 구조상 반복 호출
		Map<ExchangeRateKey, BigDecimal> rateMap = new HashMap<>();
		for (ExchangeRateKey key : rateKeys) {
			if (key.currencyCode == newBaseCurrencyCode) {
				rateMap.put(key, BigDecimal.ONE);
			} else {
				BigDecimal rate = exchangeRateService.getExchangeRate(
						key.currencyCode,
						newBaseCurrencyCode,
						key.date.atStartOfDay());
				rateMap.put(key, rate);
			}
		}

		// 3. 지출 내역 업데이트
		for (ExpenseEntity expense : expenses) {
			ExchangeRateKey key = new ExchangeRateKey(
					expense.getLocalCurrency(), expense.getOccurredAt().toLocalDate());
			BigDecimal rate = rateMap.get(key);

			// BaseAmount 재계산
			BigDecimal newBaseAmount = expense.getLocalAmount().multiply(rate).setScale(2,
					java.math.RoundingMode.HALF_UP);

			expense.updateExchangeInfo(
					expense.getLocalCurrency(),
					expense.getLocalAmount(),
					newBaseCurrencyCode,
					newBaseAmount);
		}

		expenseRepository.saveAll(expenses);
	}

	private ExpenseEntity findAndVerifyOwnership(Long expenseId, Long accountBookId) {
		ExpenseEntity entity = expenseRepository
				.findById(expenseId)
				.orElseThrow(() -> new BusinessException(ErrorCode.EXPENSE_NOT_FOUND));

		if (!entity.getAccountBookId().equals(accountBookId)) {
			throw new BusinessException(ErrorCode.EXPENSE_UNAUTHORIZED_ACCESS);
		}

		return entity;
	}

	private record ExchangeRateKey(CurrencyCode currencyCode, LocalDate date) {
	}
}
