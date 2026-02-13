package com.genesis.unipocket.analysis.query.persistence.repository;

import com.genesis.unipocket.analysis.common.enums.CurrencyType;
import com.genesis.unipocket.global.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CountryCode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
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

	public BigDecimal getMyMonthlyTotal(
			Long accountBookId, LocalDateTime start, LocalDateTime end, CurrencyType type) {
		String amountField = amountJpql(type);
		return em.createQuery(
						"SELECT COALESCE(SUM("
								+ amountField
								+ "), 0)"
								+ " FROM ExpenseEntity e"
								+ " WHERE e.accountBookId = :abId"
								+ " AND e.occurredAt >= :start"
								+ " AND e.occurredAt < :end",
						BigDecimal.class)
				.setParameter("abId", accountBookId)
				.setParameter("start", start)
				.setParameter("end", end)
				.getSingleResult();
	}

	public Object[] getOtherUsersTotalAndCount(
			CountryCode countryCode,
			String userId,
			LocalDateTime start,
			LocalDateTime end,
			CurrencyType type) {
		String amountCol = amountNative(type);
		String countryCol = countryNative(type);
		return (Object[])
				em.createNativeQuery(
								"SELECT COALESCE(SUM("
										+ amountCol
										+ "), 0),"
										+ " COUNT(DISTINCT ab.user_id)"
										+ " FROM expenses e"
										+ " JOIN account_book ab"
										+ " ON e.account_book_id = ab.account_book_id"
										+ " WHERE "
										+ countryCol
										+ " = :cc"
										+ " AND ab.user_id != :uid"
										+ " AND e.occurred_at >= :start"
										+ " AND e.occurred_at < :end")
						.setParameter("cc", countryCode.name())
						.setParameter("uid", userId)
						.setParameter("start", start)
						.setParameter("end", end)
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

	@SuppressWarnings("unchecked")
	public List<Object[]> getOtherUsersCategoryTotal(
			CountryCode countryCode,
			String userId,
			LocalDateTime start,
			LocalDateTime end,
			CurrencyType type) {
		String amountCol = amountNative(type);
		String countryCol = countryNative(type);
		return em.createNativeQuery(
						"SELECT e.category,"
								+ " SUM("
								+ amountCol
								+ ")"
								+ " FROM expenses e"
								+ " JOIN account_book ab"
								+ " ON e.account_book_id = ab.account_book_id"
								+ " WHERE "
								+ countryCol
								+ " = :cc"
								+ " AND ab.user_id != :uid"
								+ " AND e.occurred_at >= :start"
								+ " AND e.occurred_at < :end"
								+ " AND e.category != :income"
								+ " GROUP BY e.category")
				.setParameter("cc", countryCode.name())
				.setParameter("uid", userId)
				.setParameter("start", start)
				.setParameter("end", end)
				.setParameter("income", Category.INCOME.ordinal())
				.getResultList();
	}

	private String amountJpql(CurrencyType type) {
		return type == CurrencyType.BASE
				? "e.exchangeInfo.baseCurrencyAmount"
				: "e.exchangeInfo.localCurrencyAmount";
	}

	private String amountNative(CurrencyType type) {
		return type == CurrencyType.BASE ? "e.base_currency_amount" : "e.local_currency_amount";
	}

	private String countryNative(CurrencyType type) {
		return type == CurrencyType.BASE ? "ab.base_country_code" : "ab.local_country_code";
	}
}
