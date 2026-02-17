package com.genesis.unipocket.expense.command.application;

import com.genesis.unipocket.analysis.command.application.AnalysisMonthlyDirtyMarkerService;
import com.genesis.unipocket.exchange.query.application.ExchangeRateService;
import com.genesis.unipocket.expense.command.application.command.ExpenseUpdateCommand;
import com.genesis.unipocket.expense.command.application.command.ExpenseCreateCommand;
import com.genesis.unipocket.expense.application.result.ExpenseResult;
import com.genesis.unipocket.expense.command.persistence.entity.ExpenseEntity;
import com.genesis.unipocket.expense.command.persistence.entity.dto.ExpenseManualCreateArgs;
import com.genesis.unipocket.expense.command.persistence.repository.ExpenseRepository;
import com.genesis.unipocket.expense.support.ExchangeAmountCalculator;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
	private final AnalysisMonthlyDirtyMarkerService analysisMonthlyDirtyMarkerService;

	@Transactional
	public ExpenseResult createExpenseManual(ExpenseCreateCommand command) {
		try {
			validateMerchantName(command.merchantName());
			ExpenseExchangeResolver.ResolvedExchange resolved =
					ExpenseExchangeResolver.resolve(exchangeRateService, command);

			ExpenseEntity expenseEntity =
					ExpenseEntity.manual(
							ExpenseManualCreateArgs.of(
									command,
									resolved.baseCurrencyAmount(),
									resolved.calculatedBaseCurrencyAmount(),
									resolved.calculatedBaseCurrencyCode(),
									resolved.exchangeRate(),
									resolved.localCurrencyAmount()));

			var savedEntity = expenseRepository.save(expenseEntity);
			analysisMonthlyDirtyMarkerService.markDirty(
					savedEntity.getAccountBookId(), savedEntity.getOccurredAt());

			return ExpenseResult.from(savedEntity);
		} catch (IllegalArgumentException e) {
			throw new BusinessException(ErrorCode.EXPENSE_INVALID_AMOUNT);
		}
	}

	@Transactional
	public ExpenseResult updateExpense(ExpenseUpdateCommand command) {
		try {
			ExpenseEntity entity =
					findAndVerifyOwnership(command.expenseId(), command.accountBookId());
			var previousOccurredAt = entity.getOccurredAt();

			// 기본 필드 업데이트
			validateMerchantName(command.merchantName());
			entity.updateMerchantName(command.merchantName());
			entity.updateCategory(command.category());
			entity.updateUserCardId(command.userCardId());
			entity.updateMemo(command.memo());
			entity.updateOccurredAt(command.occurredAt());
			entity.updateTravelId(command.travelId());

			ExpenseExchangeResolver.ResolvedExchange resolved =
					ExpenseExchangeResolver.resolve(exchangeRateService, command);
			entity.updateExchangeInfo(
					command.localCurrencyCode(),
					resolved.localCurrencyAmount(),
					command.baseCurrencyCode(),
					resolved.baseCurrencyAmount(),
					resolved.calculatedBaseCurrencyAmount(),
					resolved.calculatedBaseCurrencyCode(),
					resolved.exchangeRate());
			analysisMonthlyDirtyMarkerService.markDirty(
					entity.getAccountBookId(), previousOccurredAt, entity.getOccurredAt());

			return ExpenseResult.from(entity);
		} catch (IllegalArgumentException e) {
			throw new BusinessException(ErrorCode.EXPENSE_INVALID_AMOUNT);
		}
	}

	@Transactional
	public void deleteExpense(Long expenseId, Long accountBookId) {
		ExpenseEntity entity = findAndVerifyOwnership(expenseId, accountBookId);
		var occurredAt = entity.getOccurredAt();
		expenseRepository.delete(entity);
		analysisMonthlyDirtyMarkerService.markDirty(accountBookId, occurredAt);
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

			// 청크 단위 환율 캐시
			// 캐시 키: 원본/대상 통화 + 발생일(일 단위 환율)
			Map<String, BigDecimal> rateCache = new HashMap<>();
			Set<OffsetDateTime> occurredAts = new LinkedHashSet<>();

			for (ExpenseEntity expense : expenses) {
				occurredAts.add(expense.getOccurredAt());
				String cacheKey =
						expense.getLocalCurrency()
								+ ":"
								+ newBaseCurrencyCode
								+ ":"
								+ expense.getOccurredAt()
										.toLocalDate(); // 일 단위 환율 기준

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
						ExchangeAmountCalculator.calculateBaseAmount(
								expense.getLocalAmount(), exchangeRate);

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
			analysisMonthlyDirtyMarkerService.markDirty(accountBookId, occurredAts);
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

	private void validateMerchantName(String merchantName) {
		if (merchantName == null || merchantName.isBlank()) {
			throw new BusinessException(ErrorCode.EXPENSE_INVALID_MERCHANT_NAME);
		}
	}

}
