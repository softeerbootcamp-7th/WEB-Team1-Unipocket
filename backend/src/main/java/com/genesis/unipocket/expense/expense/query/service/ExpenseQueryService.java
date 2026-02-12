package com.genesis.unipocket.expense.expense.query.service;

import com.genesis.unipocket.expense.common.dto.ExpenseDto;
import com.genesis.unipocket.expense.common.port.AccountBookOwnershipValidator;
import com.genesis.unipocket.expense.expense.command.persistence.entity.ExpenseEntity;
import com.genesis.unipocket.expense.expense.command.persistence.repository.ExpenseRepository;
import com.genesis.unipocket.expense.expense.query.presentation.request.ExpenseSearchFilter;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <b>지출내역 조회 서비스</b>
 *
 * @author codingbaraGo
 * @since 2026-02-03
 */
@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class ExpenseQueryService {

	private static final Set<String> ALLOWED_SORT_PROPERTIES =
			Set.of("occurredAt", "baseCurrencyAmount");

	private final ExpenseRepository expenseRepository;
	private final AccountBookOwnershipValidator accountBookOwnershipValidator;

	public ExpenseDto getExpense(Long expenseId, Long accountBookId, UUID userId) {
		accountBookOwnershipValidator.validateOwnership(accountBookId, userId.toString());
		ExpenseEntity entity = findAndVerifyOwnership(expenseId, accountBookId);
		return ExpenseDto.from(entity);
	}

	public Page<ExpenseDto> getExpenses(
			Long accountBookId, UUID userId, ExpenseSearchFilter filter, Pageable pageable) {

		accountBookOwnershipValidator.validateOwnership(accountBookId, userId.toString());

		Sort refinedSort =
				Sort.by(
						pageable.getSort().stream()
								.map(
										order -> {
											if (!ALLOWED_SORT_PROPERTIES.contains(
													order.getProperty())) {
												throw new BusinessException(
														ErrorCode.EXPENSE_INVALID_SORT);
											}

											if ("baseCurrencyAmount".equals(order.getProperty())) {
												return new Sort.Order(
														order.getDirection(),
														"exchangeInfo.baseCurrencyAmount");
											}
											return order;
										})
								.toList());

		Pageable refinedPageable =
				PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), refinedSort);

		Page<ExpenseEntity> entities;

		if (filter == null || isFilterEmpty(filter)) {
			entities = expenseRepository.findByAccountBookId(accountBookId, refinedPageable);
		} else {
			var startDate =
					filter.startDate() != null
							? filter.startDate()
									.withOffsetSameInstant(ZoneOffset.UTC)
									.toLocalDateTime()
							: null;
			var endDate =
					filter.endDate() != null
							? filter.endDate()
									.withOffsetSameInstant(ZoneOffset.UTC)
									.toLocalDateTime()
							: LocalDateTime.now();

			entities =
					expenseRepository.findByFilters(
							accountBookId,
							startDate,
							endDate,
							filter.category(),
							filter.minAmount(),
							filter.maxAmount(),
							filter.merchantName(),
							filter.travelId(),
							refinedPageable);
		}

		return entities.map(ExpenseDto::from);
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
