package com.genesis.unipocket.analysis.query.persistence.repository;

import com.genesis.unipocket.analysis.common.enums.CurrencyType;
import com.genesis.unipocket.global.common.enums.Category;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.stereotype.Repository;

@Repository
public class AnalysisQueryRepository {

	@PersistenceContext private EntityManager em;

	public Object[] getAccountBookCountryCodes(Long accountBookId) {
		return em.createQuery(
						"SELECT ab.localCountryCode, ab.baseCountryCode"
								+ " FROM AccountBookEntity ab"
								+ " WHERE ab.id = :id",
						Object[].class)
				.setParameter("id", accountBookId)
				.getSingleResult();
	}

	public Stream<Object[]> getMySpendEvents(
			Long accountBookId, OffsetDateTime start, OffsetDateTime end, CurrencyType type) {
		String amountField = amountJpql(type);
		return em.createQuery(
						"SELECT e.occurredAt,"
								+ " "
								+ amountField
								+ ""
								+ " FROM ExpenseEntity e"
								+ " WHERE e.accountBookId = :abId"
								+ " AND e.occurredAt >= :start"
								+ " AND e.occurredAt < :end"
								+ " AND e.category <> :income"
								+ " ORDER BY e.occurredAt ASC",
						Object[].class)
				.setParameter("abId", accountBookId)
				.setParameter("start", start)
				.setParameter("end", end)
				.setParameter("income", Category.INCOME)
				.getResultStream();
	}

	@SuppressWarnings("unchecked")
	public List<Object[]> getMyCategorySpent(
			Long accountBookId, OffsetDateTime start, OffsetDateTime end, CurrencyType type) {
		String amountField = amountJpql(type);
		return em.createQuery(
						"SELECT e.category,"
								+ " SUM("
								+ amountField
								+ ")"
								+ " FROM ExpenseEntity e"
								+ " WHERE e.accountBookId = :abId"
								+ " AND e.occurredAt >= :start"
								+ " AND e.occurredAt < :end"
								+ " AND e.category <> :income"
								+ " GROUP BY e.category",
						Object[].class)
				.setParameter("abId", accountBookId)
				.setParameter("start", start)
				.setParameter("end", end)
				.setParameter("income", Category.INCOME)
				.getResultList();
	}

	public Stream<Object[]> getMySpendEventsWithCurrency(
			Long accountBookId, OffsetDateTime start, OffsetDateTime end) {
		return em.createQuery(
						"SELECT e.occurredAt, e.exchangeInfo.localCurrencyAmount,"
								+ " e.exchangeInfo.localCurrencyCode"
								+ " FROM ExpenseEntity e"
								+ " WHERE e.accountBookId = :abId"
								+ " AND e.occurredAt >= :start"
								+ " AND e.occurredAt < :end"
								+ " AND e.category <> :income"
								+ " ORDER BY e.occurredAt ASC",
						Object[].class)
				.setParameter("abId", accountBookId)
				.setParameter("start", start)
				.setParameter("end", end)
				.setParameter("income", Category.INCOME)
				.getResultStream();
	}

	public List<Object[]> getMyCategorySpentGroupedByCurrency(
			Long accountBookId, OffsetDateTime start, OffsetDateTime end) {
		return em.createQuery(
						"SELECT e.category, e.exchangeInfo.localCurrencyCode,"
								+ " SUM(e.exchangeInfo.localCurrencyAmount)"
								+ " FROM ExpenseEntity e"
								+ " WHERE e.accountBookId = :abId"
								+ " AND e.occurredAt >= :start"
								+ " AND e.occurredAt < :end"
								+ " AND e.category <> :income"
								+ " GROUP BY e.category, e.exchangeInfo.localCurrencyCode",
						Object[].class)
				.setParameter("abId", accountBookId)
				.setParameter("start", start)
				.setParameter("end", end)
				.setParameter("income", Category.INCOME)
				.getResultList();
	}

	private String amountJpql(CurrencyType type) {
		return type == CurrencyType.BASE
				? "COALESCE(e.exchangeInfo.baseCurrencyAmount,"
						+ " e.exchangeInfo.calculatedBaseCurrencyAmount)"
				: "e.exchangeInfo.localCurrencyAmount";
	}
}
