package com.genesis.unipocket.widget.query.persistence;

import com.genesis.unipocket.global.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CountryCode;
import com.genesis.unipocket.widget.common.enums.CurrencyType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class WidgetQueryRepository {

	@PersistenceContext private EntityManager em;

	// ── 공통 헬퍼 ──────────────────────────────────────

	private String amountExpr(CurrencyType type) {
		return type == CurrencyType.LOCAL
				? "e.exchangeInfo.localCurrencyAmount"
				: "e.exchangeInfo.baseCurrencyAmount";
	}

	private String travelFilter(Long travelId) {
		return travelId != null ? " AND e.travelId = :travelId" : "";
	}

	private String periodFilter(LocalDateTime periodStart, LocalDateTime periodEnd) {
		StringBuilder sb = new StringBuilder();
		if (periodStart != null) {
			sb.append(" AND e.occurredAt >= :periodStart");
		}
		if (periodEnd != null) {
			sb.append(" AND e.occurredAt < :periodEnd");
		}
		return sb.toString();
	}

	private void bindTravel(Query query, Long travelId) {
		if (travelId != null) {
			query.setParameter("travelId", travelId);
		}
	}

	private void bindPeriod(Query query, LocalDateTime periodStart, LocalDateTime periodEnd) {
		if (periodStart != null) {
			query.setParameter("periodStart", periodStart);
		}
		if (periodEnd != null) {
			query.setParameter("periodEnd", periodEnd);
		}
	}

	// ── ACCOUNT BOOK 정보 ──────────────────────────────

	public CountryCode getAccountBookCountryCode(Long accountBookId, CurrencyType type) {
		String field = type == CurrencyType.LOCAL ? "ab.localCountryCode" : "ab.baseCountryCode";
		return em.createQuery(
						"SELECT "
								+ field
								+ " FROM AccountBookEntity ab WHERE ab.id = :accountBookId",
						CountryCode.class)
				.setParameter("accountBookId", accountBookId)
				.getSingleResult();
	}

	public BigDecimal getBudget(Long accountBookId) {
		BigDecimal budget =
				em.createQuery(
								"SELECT ab.budget FROM AccountBookEntity ab"
										+ " WHERE ab.id = :accountBookId",
								BigDecimal.class)
						.setParameter("accountBookId", accountBookId)
						.getSingleResult();
		return budget != null ? budget : BigDecimal.ZERO;
	}

	// ── BUDGET ──────────────────────────────────────────

	public BigDecimal getTotalSpentByAccountBookId(
			Long accountBookId, Long travelId, CurrencyType type) {

		String jpql =
				"SELECT COALESCE(SUM("
						+ amountExpr(type)
						+ "), 0)"
						+ " FROM ExpenseEntity e"
						+ " WHERE e.accountBookId = :accountBookId"
						+ travelFilter(travelId);

		var query =
				em.createQuery(jpql, BigDecimal.class).setParameter("accountBookId", accountBookId);
		bindTravel(query, travelId);
		return query.getSingleResult();
	}

	// ── PERIOD ──────────────────────────────────────────

	public BigDecimal findSpentInRange(
			Long accountBookId,
			Long travelId,
			CurrencyType type,
			LocalDateTime start,
			LocalDateTime end) {

		String jpql =
				"SELECT COALESCE(SUM("
						+ amountExpr(type)
						+ "), 0)"
						+ " FROM ExpenseEntity e"
						+ " WHERE e.accountBookId = :accountBookId"
						+ " AND e.occurredAt >= :rangeStart"
						+ " AND e.occurredAt < :rangeEnd"
						+ travelFilter(travelId);

		var query =
				em.createQuery(jpql, BigDecimal.class)
						.setParameter("accountBookId", accountBookId)
						.setParameter("rangeStart", start)
						.setParameter("rangeEnd", end);
		bindTravel(query, travelId);
		return query.getSingleResult();
	}

	// ── CATEGORY ────────────────────────────────────────

	@SuppressWarnings("unchecked")
	public List<Object[]> findCategorySpentByAccountBookId(
			Long accountBookId,
			Long travelId,
			CurrencyType type,
			LocalDateTime periodStart,
			LocalDateTime periodEnd) {

		String jpql =
				"SELECT e.category, SUM("
						+ amountExpr(type)
						+ ")"
						+ " FROM ExpenseEntity e"
						+ " WHERE e.accountBookId = :accountBookId"
						+ " AND e.category <> :income"
						+ travelFilter(travelId)
						+ periodFilter(periodStart, periodEnd)
						+ " GROUP BY e.category"
						+ " ORDER BY SUM("
						+ amountExpr(type)
						+ ") DESC";

		var query =
				em.createQuery(jpql, Object[].class)
						.setParameter("accountBookId", accountBookId)
						.setParameter("income", Category.INCOME);
		bindTravel(query, travelId);
		bindPeriod(query, periodStart, periodEnd);
		return query.getResultList();
	}

	// ── COMPARISON ──────────────────────────────────────

	public BigDecimal findMonthlyTotalByAccountBookId(
			Long accountBookId,
			Long travelId,
			CurrencyType type,
			LocalDateTime monthStart,
			LocalDateTime monthEnd) {

		String jpql =
				"SELECT COALESCE(SUM("
						+ amountExpr(type)
						+ "), 0)"
						+ " FROM ExpenseEntity e"
						+ " WHERE e.accountBookId = :accountBookId"
						+ " AND e.occurredAt >= :monthStart"
						+ " AND e.occurredAt < :monthEnd"
						+ travelFilter(travelId);

		var query =
				em.createQuery(jpql, BigDecimal.class)
						.setParameter("accountBookId", accountBookId)
						.setParameter("monthStart", monthStart)
						.setParameter("monthEnd", monthEnd);
		bindTravel(query, travelId);
		return query.getSingleResult();
	}

	public BigDecimal findAverageMonthlySpentByCountryCode(
			CountryCode localCountryCode,
			CurrencyType type,
			LocalDateTime monthStart,
			LocalDateTime monthEnd) {

		String amountCol =
				type == CurrencyType.LOCAL ? "e.local_currency_amount" : "e.base_currency_amount";

		Object result =
				em.createNativeQuery(
								"SELECT COALESCE(AVG(sub.total), 0) FROM ("
										+ "SELECT SUM("
										+ amountCol
										+ ") AS total FROM expenses e JOIN account_book ab ON"
										+ " ab.account_book_id = e.account_book_id WHERE"
										+ " ab.local_country_code = :countryCode AND e.occurred_at"
										+ " >= :monthStart AND e.occurred_at < :monthEnd GROUP BY"
										+ " e.account_book_id) sub")
						.setParameter("countryCode", localCountryCode.name())
						.setParameter("monthStart", monthStart)
						.setParameter("monthEnd", monthEnd)
						.getSingleResult();
		return result instanceof BigDecimal bd ? bd : new BigDecimal(result.toString());
	}

	// ── PAYMENT ─────────────────────────────────────────

	@SuppressWarnings("unchecked")
	public List<Object[]> findPaymentMethodSpentByAccountBookId(
			Long accountBookId,
			Long travelId,
			CurrencyType type,
			LocalDateTime periodStart,
			LocalDateTime periodEnd) {

		String jpql =
				"SELECT e.paymentMethod, SUM("
						+ amountExpr(type)
						+ ")"
						+ " FROM ExpenseEntity e"
						+ " WHERE e.accountBookId = :accountBookId"
						+ travelFilter(travelId)
						+ periodFilter(periodStart, periodEnd)
						+ " GROUP BY e.paymentMethod"
						+ " ORDER BY SUM("
						+ amountExpr(type)
						+ ") DESC";

		var query =
				em.createQuery(jpql, Object[].class).setParameter("accountBookId", accountBookId);
		bindTravel(query, travelId);
		bindPeriod(query, periodStart, periodEnd);
		return query.getResultList();
	}

	// ── CURRENCY ────────────────────────────────────────

	@SuppressWarnings("unchecked")
	public List<Object[]> findCurrencySpentByAccountBookId(
			Long accountBookId,
			Long travelId,
			CurrencyType type,
			LocalDateTime periodStart,
			LocalDateTime periodEnd) {

		String jpql =
				"SELECT e.exchangeInfo.localCurrencyCode, SUM("
						+ amountExpr(type)
						+ ")"
						+ " FROM ExpenseEntity e"
						+ " WHERE e.accountBookId = :accountBookId"
						+ travelFilter(travelId)
						+ periodFilter(periodStart, periodEnd)
						+ " GROUP BY e.exchangeInfo.localCurrencyCode"
						+ " ORDER BY SUM("
						+ amountExpr(type)
						+ ") DESC";

		var query =
				em.createQuery(jpql, Object[].class).setParameter("accountBookId", accountBookId);
		bindTravel(query, travelId);
		bindPeriod(query, periodStart, periodEnd);
		return query.getResultList();
	}
}
