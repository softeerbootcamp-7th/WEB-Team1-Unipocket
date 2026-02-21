package com.genesis.unipocket.expense.query.service;

import com.genesis.unipocket.expense.common.facade.port.UserCardFetchService;
import com.genesis.unipocket.expense.common.facade.port.dto.UserCardInfo;
import com.genesis.unipocket.expense.query.persistence.repository.ExpenseQueryRepository;
import com.genesis.unipocket.expense.query.persistence.response.ExpenseQueryRow;
import com.genesis.unipocket.expense.query.port.AccountBookOwnershipValidator;
import com.genesis.unipocket.expense.query.port.ExpenseMediaAccessService;
import com.genesis.unipocket.expense.query.port.TravelInfoReader;
import com.genesis.unipocket.expense.query.port.dto.ExpenseTravelResult;
import com.genesis.unipocket.expense.query.presentation.request.ExpenseSearchFilter;
import com.genesis.unipocket.expense.query.service.dto.ExpenseQueryResult;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import java.time.Duration;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

	private final ExpenseQueryRepository expenseQueryRepository;
	private final AccountBookOwnershipValidator accountBookOwnershipValidator;
	private final UserCardFetchService userCardReadService;
	private final ExpenseMediaAccessService expenseMediaAccessService;
	private final TravelInfoReader travelInfoReader;

	@Value("${app.media.presigned-get-expiration-seconds:600}")
	private int presignedGetExpirationSeconds;

	public ExpenseQueryResult getExpense(Long expenseId, Long accountBookId, UUID userId) {
		accountBookOwnershipValidator.validateOwnership(accountBookId, userId.toString());
		return enrichWithCardInfo(findAndValidateScope(expenseId, accountBookId));
	}

	public Page<ExpenseQueryResult> getExpenses(
			Long accountBookId, UUID userId, ExpenseSearchFilter filter, Pageable pageable) {
		accountBookOwnershipValidator.validateOwnership(accountBookId, userId.toString());
		String orderByClause = buildOrderByClause(pageable.getSort());
		return expenseQueryRepository
				.findExpenses(accountBookId, filter, pageable, orderByClause)
				.map(this::enrichWithCardInfo);
	}

	private String buildOrderByClause(Sort sort) {
		if (sort == null || sort.isUnsorted()) {
			return "ORDER BY e.occurredAt DESC";
		}
		List<String> clauses = new ArrayList<>();
		for (Sort.Order order : sort) {
			if (!ALLOWED_SORT_PROPERTIES.contains(order.getProperty())) {
				throw new BusinessException(ErrorCode.EXPENSE_INVALID_SORT);
			}
			String direction = order.getDirection().isAscending() ? "ASC" : "DESC";
			if ("baseCurrencyAmount".equals(order.getProperty())) {
				clauses.add(SIGNED_DISPLAY_BASE_AMOUNT_SORT_EXPRESSION + " " + direction);
				continue;
			}
			clauses.add("e.occurredAt " + direction);
		}
		if (clauses.isEmpty()) {
			return "ORDER BY e.occurredAt DESC";
		}
		return "ORDER BY " + String.join(", ", clauses);
	}

	private ExpenseQueryResult enrichWithCardInfo(ExpenseQueryRow row) {
		if (row.userCardId() == null) {
			return toResult(row, null);
		}

		UserCardInfo cardInfo = userCardReadService.getUserCard(row.userCardId()).orElse(null);
		return toResult(row, cardInfo);
	}

	private ExpenseQueryResult toResult(ExpenseQueryRow row, UserCardInfo cardInfo) {
		Long resolvedUserCardId = cardInfo != null ? row.userCardId() : null;
		return new ExpenseQueryResult(
				row.expenseId(),
				row.accountBookId(),
				row.travelId(),
				row.category(),
				row.baseCurrencyCode(),
				row.baseCurrencyAmount(),
				row.exchangeRate(),
				row.localCurrencyCode(),
				row.localCurrencyAmount(),
				row.occurredAt(),
				row.updatedAt() != null ? row.updatedAt().atOffset(ZoneOffset.UTC) : null,
				row.merchantName(),
				row.approvalNumber(),
				resolvedUserCardId,
				cardInfo != null ? cardInfo.cardCompany() : null,
				cardInfo != null ? cardInfo.nickName() : null,
				cardInfo != null ? cardInfo.cardNumber() : null,
				row.expenseSource(),
				row.fileLink(),
				row.memo(),
				row.cardNumber());
	}

	private ExpenseQueryRow findAndValidateScope(Long expenseId, Long accountBookId) {
		return expenseQueryRepository
				.findExpense(accountBookId, expenseId)
				.orElseThrow(
						() -> {
							Long ownerAccountBookId =
									expenseQueryRepository
											.findAccountBookIdByExpenseId(expenseId)
											.orElse(null);
							if (ownerAccountBookId == null) {
								return new BusinessException(ErrorCode.EXPENSE_NOT_FOUND);
							}
							return new BusinessException(ErrorCode.EXPENSE_UNAUTHORIZED_ACCESS);
						});
	}

	public List<String> searchMerchantNames(
			Long accountBookId, UUID userId, String query, Integer limit) {
		accountBookOwnershipValidator.validateOwnership(accountBookId, userId.toString());

		if (query == null || query.isBlank()) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
		}

		int pageSize = limit == null ? 10 : limit;
		if (pageSize < 1 || pageSize > 20) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
		}

		String normalizedQuery = query.trim();
		return expenseQueryRepository.findMerchantNameSuggestions(
				accountBookId, normalizedQuery, pageSize);
	}

	public String issueExpenseFileUrl(Long expenseId, Long accountBookId, UUID userId) {
		accountBookOwnershipValidator.validateOwnership(accountBookId, userId.toString());
		String fileLink = findAndValidateScope(expenseId, accountBookId).fileLink();
		if (fileLink == null || fileLink.isBlank()) {
			throw new BusinessException(ErrorCode.EXPENSE_FILE_LINK_NOT_FOUND);
		}

		return expenseMediaAccessService.issueGetPath(
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
