package com.genesis.unipocket.expense.query.persistence.repository;

import com.genesis.unipocket.expense.query.persistence.response.ExpenseQueryRow;
import com.genesis.unipocket.expense.query.presentation.request.ExpenseSearchFilter;
import com.genesis.unipocket.global.common.enums.Category;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public class ExpenseQueryRepository {

	@PersistenceContext private EntityManager em;

	public Optional<ExpenseQueryRow> findExpense(Long accountBookId, Long expenseId) {
		return em.createQuery(
						"SELECT new com.genesis.unipocket.expense.query.persistence.response.ExpenseQueryRow("
								+ "e.expenseId, e.accountBookId, e.travelId, e.category, "
								+ "CASE WHEN e.exchangeInfo.baseCurrencyAmount IS NOT NULL "
								+ "THEN e.exchangeInfo.baseCurrencyCode "
								+ "ELSE e.exchangeInfo.calculatedBaseCurrencyCode END, "
								+ "COALESCE(e.exchangeInfo.baseCurrencyAmount, e.exchangeInfo.calculatedBaseCurrencyAmount), "
								+ "e.exchangeInfo.exchangeRate, e.exchangeInfo.localCurrencyCode, e.exchangeInfo.localCurrencyAmount, "
								+ "e.occurredAt, e.updatedAt, e.merchant.displayMerchantName, e.approvalNumber, e.userCardId, "
								+ "e.expenseSourceInfo.expenseSource, e.expenseSourceInfo.fileLink, e.memo, e.cardNumber) "
								+ "FROM ExpenseEntity e "
								+ "WHERE e.accountBookId = :accountBookId AND e.expenseId = :expenseId",
						ExpenseQueryRow.class)
				.setParameter("accountBookId", accountBookId)
				.setParameter("expenseId", expenseId)
				.getResultList()
				.stream()
				.findFirst();
	}

	public Optional<Long> findAccountBookIdByExpenseId(Long expenseId) {
		return em.createQuery(
						"SELECT e.accountBookId FROM ExpenseEntity e WHERE e.expenseId = :expenseId",
						Long.class)
				.setParameter("expenseId", expenseId)
				.getResultList()
				.stream()
				.findFirst();
	}

	public Page<ExpenseQueryRow> findExpenses(
			Long accountBookId, ExpenseSearchFilter filter, Pageable pageable, String orderByClause) {
		String whereClause = buildWhereClause(filter);
		String selectJpql =
				"SELECT new com.genesis.unipocket.expense.query.persistence.response.ExpenseQueryRow("
						+ "e.expenseId, e.accountBookId, e.travelId, e.category, "
						+ "CASE WHEN e.exchangeInfo.baseCurrencyAmount IS NOT NULL "
						+ "THEN e.exchangeInfo.baseCurrencyCode "
						+ "ELSE e.exchangeInfo.calculatedBaseCurrencyCode END, "
						+ "COALESCE(e.exchangeInfo.baseCurrencyAmount, e.exchangeInfo.calculatedBaseCurrencyAmount), "
						+ "e.exchangeInfo.exchangeRate, e.exchangeInfo.localCurrencyCode, e.exchangeInfo.localCurrencyAmount, "
						+ "e.occurredAt, e.updatedAt, e.merchant.displayMerchantName, e.approvalNumber, e.userCardId, "
						+ "e.expenseSourceInfo.expenseSource, e.expenseSourceInfo.fileLink, e.memo, e.cardNumber) "
						+ "FROM ExpenseEntity e "
						+ whereClause
						+ " "
						+ orderByClause;

		TypedQuery<ExpenseQueryRow> selectQuery = em.createQuery(selectJpql, ExpenseQueryRow.class);
		bindFilter(selectQuery, accountBookId, filter);
		selectQuery.setFirstResult((int) pageable.getOffset());
		selectQuery.setMaxResults(pageable.getPageSize());
		List<ExpenseQueryRow> content = selectQuery.getResultList();

		String countJpql = "SELECT COUNT(e.expenseId) FROM ExpenseEntity e " + whereClause;
		TypedQuery<Long> countQuery = em.createQuery(countJpql, Long.class);
		bindFilter(countQuery, accountBookId, filter);
		long total = countQuery.getSingleResult();

		return new PageImpl<>(content, pageable, total);
	}

	public List<String> findMerchantNameSuggestions(Long accountBookId, String prefix, int limit) {
		return em.createQuery(
						"SELECT e.merchant.displayMerchantName "
								+ "FROM ExpenseEntity e "
								+ "WHERE e.accountBookId = :accountBookId "
								+ "AND e.merchant.displayMerchantName LIKE CONCAT(:prefix, '%') "
								+ "GROUP BY e.merchant.displayMerchantName "
								+ "ORDER BY MAX(e.updatedAt) DESC",
						String.class)
				.setParameter("accountBookId", accountBookId)
				.setParameter("prefix", prefix)
				.setMaxResults(limit)
				.getResultList();
	}

	private String buildWhereClause(ExpenseSearchFilter filter) {
		StringBuilder where = new StringBuilder("WHERE e.accountBookId = :accountBookId ");
		if (filter == null) {
			return where.toString();
		}
		if (filter.startDate() != null) {
			where.append("AND e.occurredAt >= :startDate ");
		}
		if (filter.endDate() != null) {
			where.append("AND e.occurredAt < :endDate ");
		}
		if (filter.category() != null) {
			where.append("AND e.category = :category ");
		}
		if (filter.minAmount() != null) {
			where.append(
					"AND COALESCE(e.exchangeInfo.baseCurrencyAmount, e.exchangeInfo.calculatedBaseCurrencyAmount) >= :minAmount ");
		}
		if (filter.maxAmount() != null) {
			where.append(
					"AND COALESCE(e.exchangeInfo.baseCurrencyAmount, e.exchangeInfo.calculatedBaseCurrencyAmount) <= :maxAmount ");
		}
		if (filter.merchantName() != null) {
			where.append("AND e.merchant.displayMerchantName LIKE CONCAT('%', :merchantName, '%') ");
		}
		if (filter.travelId() != null) {
			where.append("AND e.travelId = :travelId ");
		}
		return where.toString();
	}

	private void bindFilter(
			TypedQuery<?> query, Long accountBookId, ExpenseSearchFilter filter) {
		query.setParameter("accountBookId", accountBookId);
		if (filter == null) {
			return;
		}
		OffsetDateTime startDate =
				filter.startDate() != null ? filter.startDate() : null;
		OffsetDateTime endDate = filter.endDate() != null ? filter.endDate() : null;
		Category category = filter.category();
		BigDecimal minAmount = filter.minAmount();
		BigDecimal maxAmount = filter.maxAmount();
		String merchantName = filter.merchantName();
		Long travelId = filter.travelId();

		if (startDate != null) {
			query.setParameter("startDate", startDate);
		}
		if (endDate != null) {
			query.setParameter("endDate", endDate);
		}
		if (category != null) {
			query.setParameter("category", category);
		}
		if (minAmount != null) {
			query.setParameter("minAmount", minAmount);
		}
		if (maxAmount != null) {
			query.setParameter("maxAmount", maxAmount);
		}
		if (merchantName != null) {
			query.setParameter("merchantName", merchantName);
		}
		if (travelId != null) {
			query.setParameter("travelId", travelId);
		}
	}
}
