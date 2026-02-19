package com.genesis.unipocket.expense.query.service;

import com.genesis.unipocket.expense.application.result.ExpenseResult;
import com.genesis.unipocket.expense.application.result.ExpenseTravelResult;
import com.genesis.unipocket.expense.command.facade.port.UserCardFetchService;
import com.genesis.unipocket.expense.command.facade.port.dto.UserCardInfo;
import com.genesis.unipocket.expense.command.persistence.entity.ExpenseEntity;
import com.genesis.unipocket.expense.command.persistence.repository.ExpenseRepository;
import com.genesis.unipocket.expense.query.presentation.request.ExpenseSearchFilter;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.media.command.application.MediaObjectStorage;
import com.genesis.unipocket.tempexpense.command.facade.port.AccountBookOwnershipValidator;
import java.time.Duration;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <b>지출내역 조회 서비스</b>
 *
 * @author codingbaraGo
 * @since 2026-02-03
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExpenseQueryService {

	private static final String DISPLAY_BASE_AMOUNT_SORT_EXPRESSION =
			"COALESCE(exchangeInfo.baseCurrencyAmount, exchangeInfo.calculatedBaseCurrencyAmount)";
	private static final String SIGNED_DISPLAY_BASE_AMOUNT_SORT_EXPRESSION =
			"CASE WHEN category = com.genesis.unipocket.global.common.enums.Category.INCOME THEN "
					+ DISPLAY_BASE_AMOUNT_SORT_EXPRESSION
					+ " ELSE (-"
					+ DISPLAY_BASE_AMOUNT_SORT_EXPRESSION
					+ ") END";
	private static final Set<String> ALLOWED_SORT_PROPERTIES =
			Set.of("occurredAt", "baseCurrencyAmount");

	private final ExpenseRepository expenseRepository;
	private final AccountBookOwnershipValidator accountBookOwnershipValidator;
	private final UserCardFetchService userCardFetchService;
	private final ExpenseMerchantSearchRateLimitService expenseMerchantSearchRateLimitService;
	private final MediaObjectStorage mediaObjectStorage;
	private final TravelInfoReader travelInfoReader;

	@Value("${app.media.presigned-get-expiration-seconds:600}")
	private int presignedGetExpirationSeconds;

	public ExpenseResult getExpense(Long expenseId, Long accountBookId, UUID userId) {
		accountBookOwnershipValidator.validateOwnership(accountBookId, userId.toString());
		ExpenseEntity entity = findAndVerifyOwnership(expenseId, accountBookId);
		return enrichWithCardInfo(entity);
	}

	public Page<ExpenseResult> getExpenses(
			Long accountBookId, UUID userId, ExpenseSearchFilter filter, Pageable pageable) {

		accountBookOwnershipValidator.validateOwnership(accountBookId, userId.toString());

		Sort refinedSort = null;
		for (Sort.Order order : pageable.getSort()) {
			if (!ALLOWED_SORT_PROPERTIES.contains(order.getProperty())) {
				throw new BusinessException(ErrorCode.EXPENSE_INVALID_SORT);
			}

			Sort mappedSort =
					"baseCurrencyAmount".equals(order.getProperty())
							? JpaSort.unsafe(
									order.getDirection(),
									SIGNED_DISPLAY_BASE_AMOUNT_SORT_EXPRESSION)
							: Sort.by(new Sort.Order(order.getDirection(), order.getProperty()));

			refinedSort = refinedSort == null ? mappedSort : refinedSort.and(mappedSort);
		}
		if (refinedSort == null) {
			refinedSort = Sort.by(Sort.Order.desc("occurredAt"));
		}

		Pageable refinedPageable =
				PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), refinedSort);

		var startDate =
				filter != null && filter.startDate() != null
						? filter.startDate().withOffsetSameInstant(ZoneOffset.UTC)
						: null;
		var endDate =
				filter != null && filter.endDate() != null
						? filter.endDate().withOffsetSameInstant(ZoneOffset.UTC)
						: null;
		var category = filter != null ? filter.category() : null;
		var minAmount = filter != null ? filter.minAmount() : null;
		var maxAmount = filter != null ? filter.maxAmount() : null;
		var merchantName = filter != null ? filter.merchantName() : null;
		var travelId = filter != null ? filter.travelId() : null;

		Page<ExpenseEntity> entities =
				expenseRepository.findByFilters(
						accountBookId,
						startDate,
						endDate,
						category,
						minAmount,
						maxAmount,
						merchantName,
						travelId,
						refinedPageable);

		return entities.map(this::enrichWithCardInfo);
	}

	private ExpenseResult enrichWithCardInfo(ExpenseEntity entity) {
		if (entity.getUserCardId() == null) {
			return ExpenseResult.from(entity);
		}
		UserCardInfo cardInfo = userCardFetchService.getUserCard(entity.getUserCardId());
		return ExpenseResult.from(entity, cardInfo);
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

	public List<String> searchMerchantNames(
			Long accountBookId, UUID userId, String query, Integer limit) {
		accountBookOwnershipValidator.validateOwnership(accountBookId, userId.toString());
		expenseMerchantSearchRateLimitService.validate(userId);

		if (query == null || query.isBlank()) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
		}

		int pageSize = limit == null ? 10 : limit;
		if (pageSize < 1 || pageSize > 20) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
		}

		String normalizedQuery = query.trim();
		return expenseRepository.findMerchantNameSuggestions(
				accountBookId, normalizedQuery, PageRequest.of(0, pageSize));
	}

	public String issueExpenseFileUrl(Long expenseId, Long accountBookId, UUID userId) {
		accountBookOwnershipValidator.validateOwnership(accountBookId, userId.toString());
		ExpenseEntity entity = findAndVerifyOwnership(expenseId, accountBookId);

		String fileLink =
				entity.getExpenseSourceInfo() != null
						? entity.getExpenseSourceInfo().getFileLink()
						: null;
		if (fileLink == null || fileLink.isBlank()) {
			throw new BusinessException(ErrorCode.EXPENSE_FILE_LINK_NOT_FOUND);
		}

		return mediaObjectStorage.getPresignedGetUrl(
				fileLink, Duration.ofSeconds(presignedGetExpirationSeconds));
	}

	public int getExpenseFileUrlExpirationSeconds() {
		return presignedGetExpirationSeconds;
	}

	public Map<Long, ExpenseTravelResult> getTravelInfoMap(
			Long accountBookId, Collection<Long> travelIds) {
		return travelInfoReader.readTravelInfoMap(accountBookId, travelIds);
	}

	public ExpenseTravelResult getTravelInfo(Long accountBookId, Long travelId) {
		if (travelId == null) {
			return null;
		}
		return getTravelInfoMap(accountBookId, List.of(travelId)).get(travelId);
	}
}
