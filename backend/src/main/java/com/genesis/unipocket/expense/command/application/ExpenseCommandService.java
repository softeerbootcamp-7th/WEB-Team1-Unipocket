package com.genesis.unipocket.expense.command.application;

import com.genesis.unipocket.exchange.query.application.ExchangeRateService;
import com.genesis.unipocket.expense.command.application.command.ExpenseCreateCommand;
import com.genesis.unipocket.expense.command.application.command.ExpenseUpdateCommand;
import com.genesis.unipocket.expense.command.application.result.ExpenseResult;
import com.genesis.unipocket.expense.command.persistence.entity.ExpenseEntity;
import com.genesis.unipocket.expense.command.persistence.entity.dto.ExpenseManualCreateArgs;
import com.genesis.unipocket.expense.command.persistence.repository.ExpenseRepository;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
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
		BigDecimal exchangeRate =
				exchangeRateService.getExchangeRate(
						command.localCurrencyCode(),
						command.baseCurrencyCode(),
						command.occurredAt());

		BigDecimal baseCurrencyAmount =
				command.localCurrencyAmount()
						.multiply(exchangeRate)
						.setScale(2, BigDecimal.ROUND_HALF_UP);

		ExpenseEntity expenseEntity =
				ExpenseEntity.manual(
						ExpenseManualCreateArgs.of(
								command, baseCurrencyAmount, null, null, exchangeRate));

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
		boolean amountChanged =
				entity.getLocalAmount().compareTo(command.localCurrencyAmount()) != 0;

		if (currencyChanged || amountChanged) {
			BigDecimal exchangeRate =
					exchangeRateService.getExchangeRate(
							command.localCurrencyCode(),
							command.baseCurrencyCode(),
							command.occurredAt());

			BigDecimal baseCurrencyAmount =
					command.localCurrencyAmount()
							.multiply(exchangeRate)
							.setScale(2, BigDecimal.ROUND_HALF_UP);

			boolean convertedMode =
					entity.getExchangeInfo() != null
							&& entity.getExchangeInfo().getCalculatedBaseCurrencyAmount() != null;

			entity.updateExchangeInfo(
					command.localCurrencyCode(),
					command.localCurrencyAmount(),
					entity.getOriginalBaseCurrency(),
					convertedMode ? entity.getOriginalBaseAmount() : baseCurrencyAmount,
					convertedMode ? baseCurrencyAmount : null,
					convertedMode ? command.baseCurrencyCode() : null,
					exchangeRate);
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
		int pageSize = 1000;
		int pageNumber = 0;
		Page<ExpenseEntity> page;

		do {
			page =
					expenseRepository.findByAccountBookId(
							accountBookId,
							org.springframework.data.domain.PageRequest.of(pageNumber, pageSize));
			List<ExpenseEntity> expenses = page.getContent();

			if (expenses.isEmpty()) {
				break;
			}

			// Local cache for exchange rates to avoid N+1 lookups within the chunk.
			// Keyed by source/target currency and occurred date (daily rate granularity).
			java.util.Map<String, BigDecimal> rateCache = new java.util.HashMap<>();

			for (ExpenseEntity expense : expenses) {
				String cacheKey =
						expense.getLocalCurrency()
								+ ":"
								+ newBaseCurrencyCode
								+ ":"
								+ expense.getOccurredAt()
										.toLocalDate(); // Assuming daily rate granularity

				BigDecimal exchangeRate = rateCache.get(cacheKey);

				if (exchangeRate == null) {
					exchangeRate =
							exchangeRateService.getExchangeRate(
									expense.getLocalCurrency(),
									newBaseCurrencyCode,
									expense.getOccurredAt());
					rateCache.put(cacheKey, exchangeRate);
				}

				BigDecimal baseCurrencyAmount =
						expense.getLocalAmount()
								.multiply(exchangeRate)
								.setScale(2, RoundingMode.HALF_UP);

				expense.updateExchangeInfo(
						expense.getLocalCurrency(),
						expense.getLocalAmount(),
						expense.getOriginalBaseCurrency(),
						expense.getOriginalBaseAmount(),
						baseCurrencyAmount,
						newBaseCurrencyCode,
						exchangeRate);
			}
			expenseRepository.saveAll(expenses);
			pageNumber++;
		} while (page.hasNext());
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
}
