package com.genesis.unipocket.analysis.command.persistence.repository.support;

import com.genesis.unipocket.global.common.enums.CountryCode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class AnalysisBatchAggregationRepository {

	@PersistenceContext private EntityManager em;

	public AmountCount aggregateCountryDailyRaw(
			CountryCode countryCode, LocalDateTime startUtc, LocalDateTime endUtc) {
		Object[] row =
				(Object[])
						em.createNativeQuery(
										"""
								SELECT COALESCE(SUM(e.local_currency_amount), 0), COUNT(*)
								FROM expenses e
								JOIN account_book ab ON e.account_book_id = ab.account_book_id
								WHERE ab.local_country_code = :countryCode
									AND e.occurred_at >= :startUtc
									AND e.occurred_at < :endUtc
									AND e.local_currency_amount IS NOT NULL
								""")
								.setParameter("countryCode", countryCode.name())
								.setParameter("startUtc", startUtc)
								.setParameter("endUtc", endUtc)
								.getSingleResult();
		return toAmountCount(row);
	}

	public AmountCount aggregateCountryDailyCleaned(
			CountryCode countryCode,
			LocalDateTime startUtc,
			LocalDateTime endUtc,
			BigDecimal minAmount,
			BigDecimal maxAmount) {
		Object[] row =
				(Object[])
						em.createNativeQuery(
										"""
								SELECT COALESCE(SUM(e.local_currency_amount), 0), COUNT(*)
								FROM expenses e
								JOIN account_book ab ON e.account_book_id = ab.account_book_id
								WHERE ab.local_country_code = :countryCode
									AND e.occurred_at >= :startUtc
									AND e.occurred_at < :endUtc
									AND e.local_currency_amount >= :minAmount
									AND e.local_currency_amount <= :maxAmount
								""")
								.setParameter("countryCode", countryCode.name())
								.setParameter("startUtc", startUtc)
								.setParameter("endUtc", endUtc)
								.setParameter("minAmount", minAmount)
								.setParameter("maxAmount", maxAmount)
								.getSingleResult();
		return toAmountCount(row);
	}

	@SuppressWarnings("unchecked")
	public List<AccountAmountCount> aggregateAccountDailyRaw(
			CountryCode countryCode, LocalDateTime startUtc, LocalDateTime endUtc) {
		List<Object[]> rows =
				em.createNativeQuery(
								"""
							SELECT ab.account_book_id, COALESCE(SUM(e.local_currency_amount), 0), COUNT(e.expense_id)
							FROM account_book ab
							LEFT JOIN expenses e ON e.account_book_id = ab.account_book_id
								AND e.occurred_at >= :startUtc
								AND e.occurred_at < :endUtc
								AND e.local_currency_amount IS NOT NULL
							WHERE ab.local_country_code = :countryCode
								AND ab.base_country_code = :countryCode
							GROUP BY ab.account_book_id
							""")
						.setParameter("countryCode", countryCode.name())
						.setParameter("startUtc", startUtc)
						.setParameter("endUtc", endUtc)
						.getResultList();
		return rows.stream().map(this::toAccountAmountCount).toList();
	}

	@SuppressWarnings("unchecked")
	public List<AccountAmountCount> aggregateAccountDailyCleaned(
			CountryCode countryCode,
			LocalDateTime startUtc,
			LocalDateTime endUtc,
			BigDecimal minAmount,
			BigDecimal maxAmount) {
		List<Object[]> rows =
				em.createNativeQuery(
								"""
							SELECT ab.account_book_id, COALESCE(SUM(e.local_currency_amount), 0), COUNT(e.expense_id)
							FROM account_book ab
							LEFT JOIN expenses e ON e.account_book_id = ab.account_book_id
								AND e.occurred_at >= :startUtc
								AND e.occurred_at < :endUtc
								AND e.local_currency_amount >= :minAmount
								AND e.local_currency_amount <= :maxAmount
							WHERE ab.local_country_code = :countryCode
								AND ab.base_country_code = :countryCode
							GROUP BY ab.account_book_id
							""")
						.setParameter("countryCode", countryCode.name())
						.setParameter("startUtc", startUtc)
						.setParameter("endUtc", endUtc)
						.setParameter("minAmount", minAmount)
						.setParameter("maxAmount", maxAmount)
						.getResultList();
		return rows.stream().map(this::toAccountAmountCount).toList();
	}

	private AmountCount toAmountCount(Object[] row) {
		BigDecimal amount = row[0] == null ? BigDecimal.ZERO : new BigDecimal(row[0].toString());
		long count = row[1] == null ? 0L : ((Number) row[1]).longValue();
		return new AmountCount(amount, count);
	}

	private AccountAmountCount toAccountAmountCount(Object[] row) {
		Long accountBookId = ((Number) row[0]).longValue();
		BigDecimal amount = row[1] == null ? BigDecimal.ZERO : new BigDecimal(row[1].toString());
		long count = row[2] == null ? 0L : ((Number) row[2]).longValue();
		return new AccountAmountCount(accountBookId, amount, count);
	}

	public record AmountCount(BigDecimal totalAmount, long expenseCount) {}

	public record AccountAmountCount(
			Long accountBookId, BigDecimal totalAmount, long expenseCount) {}
}
