package com.genesis.unipocket.expense.command.application;

import com.genesis.unipocket.analysis.command.application.AnalysisMonthlyDirtyMarkerService;
import com.genesis.unipocket.exchange.common.service.ExchangeRateService;
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
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionOperations;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class ExpenseCommandService {
	private static final int MAX_MERCHANT_NAME_LENGTH = 40;

	private final ExpenseRepository expenseRepository;
	private final ExchangeRateService exchangeRateService;
	private final AnalysisMonthlyDirtyMarkerService analysisMonthlyDirtyMarkerService;
	private final TransactionOperations transactionOperations;

	@Transactional
	public ExpenseResult createExpenseManual(ExpenseCreateCommand command) {
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
	}

	@Transactional
	public ExpenseResult updateExpense(ExpenseUpdateCommand command) {
		ExpenseEntity entity = findAndVerifyOwnership(command.expenseId(), command.accountBookId());
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
	}

	@Transactional
	public void deleteExpense(Long expenseId, Long accountBookId) {
		ExpenseEntity entity = findAndVerifyOwnership(expenseId, accountBookId);
		var occurredAt = entity.getOccurredAt();
		expenseRepository.delete(entity);
		analysisMonthlyDirtyMarkerService.markDirty(accountBookId, occurredAt);
	}

	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public void updateBaseCurrency(Long accountBookId, CurrencyCode newBaseCurrencyCode) {
		List<Object[]> localCurrencyDatePairs =
				expenseRepository.findDistinctLocalCurrencyDatePairsByAccountBookId(accountBookId);

		for (Object[] pair : localCurrencyDatePairs) {
			CurrencyCode localCurrencyCode;
			if (pair[0] instanceof CurrencyCode cc) {
				localCurrencyCode = cc;
			} else if (pair[0] instanceof String str) {
				localCurrencyCode = CurrencyCode.valueOf(str);
			} else {
				throw new IllegalStateException(
						"Expected CurrencyCode or String but got: "
								+ (pair[0] == null ? "null" : pair[0].getClass().getName()));
			}
			if (pair[1] == null) {
				throw new IllegalStateException(
						"occurredAt date is null for accountBookId: " + accountBookId);
			}
			LocalDate occurredDate = toLocalDate(pair[1]);
			OffsetDateTime dayStart = occurredDate.atStartOfDay().atOffset(ZoneOffset.UTC);
			OffsetDateTime nextDayStart = dayStart.plusDays(1);

			// 임시 계산용 rate(메서드 로컬): localCurrency -> newBaseCurrency
			BigDecimal localToBaseRate =
					localCurrencyCode == newBaseCurrencyCode
							? BigDecimal.ONE
							: exchangeRateService.getExchangeRate(
									localCurrencyCode, newBaseCurrencyCode, dayStart);

			transactionOperations.execute(
					status -> {
						expenseRepository.bulkUpdateBaseCurrencyByLocalCurrencyAndOccurredAtRange(
								accountBookId,
								localCurrencyCode,
								newBaseCurrencyCode,
								localToBaseRate,
								dayStart,
								nextDayStart);
						// 부모 트랜잭션인 AccountBookCommandService.update()에서
						// 이미 markDirtyAllMonths를 호출해 Lock을 쥐고 있으므로 여기서 다시 호출하면 50s Lock Timeout
						// (Self
						// Deadlock) 발생
						return null;
					});
		}
	}

	private LocalDate toLocalDate(Object dateObj) {
		if (dateObj instanceof java.sql.Date sqlDate) {
			return sqlDate.toLocalDate();
		} else if (dateObj instanceof java.sql.Timestamp sqlTimestamp) {
			return sqlTimestamp.toLocalDateTime().toLocalDate();
		} else if (dateObj instanceof java.time.LocalDate localDate) {
			return localDate;
		} else if (dateObj instanceof java.time.LocalDateTime localDateTime) {
			return localDateTime.toLocalDate();
		} else if (dateObj instanceof String strDate) {
			return LocalDate.parse(strDate);
		}
		throw new IllegalStateException("Unexpected date type: " + dateObj.getClass().getName());
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
		if (merchantName == null) {
			throw new BusinessException(ErrorCode.EXPENSE_INVALID_MERCHANT_NAME);
		}

		String normalized = merchantName.trim();
		if (normalized.isEmpty() || normalized.length() > MAX_MERCHANT_NAME_LENGTH) {
			throw new BusinessException(ErrorCode.EXPENSE_INVALID_MERCHANT_NAME);
		}
	}
}
