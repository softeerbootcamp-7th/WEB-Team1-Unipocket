package com.genesis.unipocket.analysis.query.persistence.repository;

import com.genesis.unipocket.analysis.common.enums.CurrencyType;
import com.genesis.unipocket.global.common.enums.Category;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.List;
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

	public List<Object[]> getMyDailySpent(
			Long accountBookId, LocalDateTime start, LocalDateTime end, CurrencyType type) {
		String amountField = amountJpql(type);
		return em.createQuery(
						"SELECT CAST(e.occurredAt AS DATE),"
								+ " SUM("
								+ amountField
								+ ")"
								+ " FROM ExpenseEntity e"
								+ " WHERE e.accountBookId = :abId"
								+ " AND e.occurredAt >= :start"
								+ " AND e.occurredAt < :end"
								+ " GROUP BY CAST(e.occurredAt AS DATE)"
								+ " ORDER BY CAST(e.occurredAt AS DATE) ASC",
						Object[].class)
				.setParameter("abId", accountBookId)
				.setParameter("start", start)
				.setParameter("end", end)
				.getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<Object[]> getMyCategorySpent(
			Long accountBookId, LocalDateTime start, LocalDateTime end, CurrencyType type) {
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

	private String amountJpql(CurrencyType type) {
		return type == CurrencyType.BASE
				? "e.exchangeInfo.baseCurrencyAmount"
				: "e.exchangeInfo.localCurrencyAmount";
	}
}
