package com.genesis.unipocket.widget.query.persistence;

import com.genesis.unipocket.expense.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CountryCode;
import com.genesis.unipocket.widget.common.enums.CurrencyType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class WidgetQueryRepository {

	@PersistenceContext private EntityManager em;

	// ── BUDGET ──────────────────────────────────────────

	public CountryCode getAccountBookCountryCode(Long accountBookId, CurrencyType type) {
		String qlString = "";
		if (type.equals(CurrencyType.BASE)) {
			qlString += "SELECT ab.baseCountryCode ";
		} else if (type.equals(CurrencyType.LOCAL)) {
			qlString += "SELECT ab.localCountryCode ";
		}
		qlString += "FROM AccountBookEntity ab WHERE ab.id = :accountBookId";

		return em.createQuery(qlString, CountryCode.class)
				.setParameter("accountBookId", accountBookId)
				.getSingleResult();
	}

	public BigDecimal getBudget(Long accountBookId) {
		Long budget =
				em.createQuery(
								"SELECT ab.budget FROM AccountBookEntity ab"
										+ " WHERE ab.id = :accountBookId",
								Long.class)
						.setParameter("accountBookId", accountBookId)
						.getSingleResult();
		return budget != null ? BigDecimal.valueOf(budget) : BigDecimal.ZERO;
	}

	public BigDecimal getTotalSpentByAccountBookId(Long accountBookId, CurrencyType type) {
		String qlString = "";
		if (type.equals(CurrencyType.BASE)) {
			qlString += "SELECT COALESCE(SUM(e.exchangeInfo.baseCurrencyAmount), 0) ";
		} else if (type.equals(CurrencyType.LOCAL)) {
			qlString += "SELECT COALESCE(SUM(e.exchangeInfo.localCurrencyAmount), 0) ";
		}
		qlString += "FROM ExpenseEntity e WHERE e.accountBookId = :accountBookId";
		return em.createQuery(qlString, BigDecimal.class)
				.setParameter("accountBookId", accountBookId)
				.getSingleResult();
	}

	// ── PERIOD ──────────────────────────────────────────

	public List<Object[]> findDailySpentByAccountBookId(Long accountBookId) {
		List<Object[]> result =
				em.createQuery(
								"SELECT CAST(e.occurredAt AS DATE), "
										+ "SUM(e.exchangeInfo.baseCurrencyAmount) "
										+ "FROM ExpenseEntity e "
										+ "WHERE e.accountBookId = :accountBookId "
										+ "GROUP BY CAST(e.occurredAt AS DATE) "
										+ "ORDER BY CAST(e.occurredAt AS DATE) DESC",
								Object[].class)
						.setParameter("accountBookId", accountBookId)
						.setMaxResults(7)
						.getResultList();

		Collections.reverse(result);
		return result;
	}

	public List<Object[]> findWeeklySpentByAccountBookId(Long accountBookId) {
		List<Object[]> result =
				em.createQuery(
								"SELECT YEAR(e.occurredAt), MONTH(e.occurredAt), FUNCTION('WEEK',"
									+ " e.occurredAt), SUM(e.exchangeInfo.baseCurrencyAmount) FROM"
									+ " ExpenseEntity e WHERE e.accountBookId = :accountBookId"
									+ " GROUP BY YEAR(e.occurredAt), MONTH(e.occurredAt),"
									+ " FUNCTION('WEEK', e.occurredAt) ORDER BY YEAR(e.occurredAt)"
									+ " DESC, FUNCTION('WEEK', e.occurredAt) DESC",
								Object[].class)
						.setParameter("accountBookId", accountBookId)
						.setMaxResults(5)
						.getResultList();

		Collections.reverse(result);
		return result;
	}

	public List<Object[]> findMonthlySpentByAccountBookId(Long accountBookId) {
		String monthExpr =
				"CONCAT(YEAR(e.occurredAt), '-', "
						+ "CASE WHEN MONTH(e.occurredAt) < 10 "
						+ "THEN CONCAT('0', MONTH(e.occurredAt)) "
						+ "ELSE CAST(MONTH(e.occurredAt) AS STRING) END)";

		List<Object[]> result =
				em.createQuery(
								"SELECT "
										+ monthExpr
										+ ", SUM(e.exchangeInfo.baseCurrencyAmount) "
										+ "FROM ExpenseEntity e "
										+ "WHERE e.accountBookId = :accountBookId "
										+ "GROUP BY "
										+ monthExpr
										+ " "
										+ "ORDER BY "
										+ monthExpr
										+ " DESC",
								Object[].class)
						.setParameter("accountBookId", accountBookId)
						.setMaxResults(6)
						.getResultList();

		Collections.reverse(result);
		return result;
	}

	// ── CATEGORY ────────────────────────────────────────

	public List<Object[]> findCategorySpentByAccountBookId(Long accountBookId) {
		return em.createQuery(
						"SELECT e.category, SUM(e.exchangeInfo.baseCurrencyAmount)"
								+ " FROM ExpenseEntity e"
								+ " WHERE e.accountBookId = :accountBookId"
								+ " AND e.category <> :income"
								+ " GROUP BY e.category"
								+ " ORDER BY SUM(e.exchangeInfo.baseCurrencyAmount) DESC",
						Object[].class)
				.setParameter("accountBookId", accountBookId)
				.setParameter("income", Category.INCOME)
				.getResultList();
	}

	// ── PAYMENT ─────────────────────────────────────────

	public List<Object[]> findPaymentMethodSpentByAccountBookId(Long accountBookId) {
		return em.createQuery(
						"SELECT e.paymentMethod, SUM(e.exchangeInfo.baseCurrencyAmount)"
								+ " FROM ExpenseEntity e"
								+ " WHERE e.accountBookId = :accountBookId"
								+ " AND e.paymentMethod IS NOT NULL"
								+ " GROUP BY e.paymentMethod"
								+ " ORDER BY SUM(e.exchangeInfo.baseCurrencyAmount) DESC",
						Object[].class)
				.setParameter("accountBookId", accountBookId)
				.getResultList();
	}

	// ── CURRENCY ────────────────────────────────────────

	public List<Object[]> findCurrencySpentByAccountBookId(Long accountBookId) {
		return em.createQuery(
						"SELECT e.exchangeInfo.localCurrencyCode,"
								+ " SUM(e.exchangeInfo.baseCurrencyAmount)"
								+ " FROM ExpenseEntity e"
								+ " WHERE e.accountBookId = :accountBookId"
								+ " GROUP BY e.exchangeInfo.localCurrencyCode"
								+ " ORDER BY SUM(e.exchangeInfo.baseCurrencyAmount) DESC",
						Object[].class)
				.setParameter("accountBookId", accountBookId)
				.getResultList();
	}
}
