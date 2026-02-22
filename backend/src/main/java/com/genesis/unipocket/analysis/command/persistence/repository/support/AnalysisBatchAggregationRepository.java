package com.genesis.unipocket.analysis.command.persistence.repository.support;

import com.genesis.unipocket.analysis.command.persistence.entity.AnalysisMetricType;
import com.genesis.unipocket.analysis.command.persistence.entity.AnalysisQualityType;
import com.genesis.unipocket.analysis.common.enums.CurrencyType;
import com.genesis.unipocket.analysis.common.util.CategoryOrdinalParser;
import com.genesis.unipocket.global.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CountryCode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class AnalysisBatchAggregationRepository {

	@PersistenceContext private EntityManager em;

	public AmountPairCount aggregateAccountBookMonthlyRaw(
			Long accountBookId, LocalDateTime startUtc, LocalDateTime endUtc) {
		Object[] row =
				(Object[])
						em.createNativeQuery(
										"""
								SELECT
									COALESCE(SUM(e.local_currency_amount), 0),
									COALESCE(SUM(COALESCE(e.base_currency_amount, e.calculated_base_currency_amount)), 0),
									COUNT(*)
								FROM expenses e
								WHERE e.account_book_id = :accountBookId
									AND e.occurred_at >= :startUtc
									AND e.occurred_at < :endUtc
									AND (e.category IS NULL OR e.category <> :incomeCategory)
								""")
								.setParameter("accountBookId", accountBookId)
								.setParameter("startUtc", startUtc)
								.setParameter("endUtc", endUtc)
								.setParameter("incomeCategory", Category.INCOME.ordinal())
								.getSingleResult();
		return toAmountPairCount(row);
	}

	@SuppressWarnings("unchecked")
	public List<CategoryAmountPairCount> aggregateAccountBookMonthlyRawByCategory(
			Long accountBookId, LocalDateTime startUtc, LocalDateTime endUtc) {
		List<Object[]> rows =
				em.createNativeQuery(
								"""
						SELECT
							e.category,
							COALESCE(SUM(e.local_currency_amount), 0),
							COALESCE(SUM(COALESCE(e.base_currency_amount, e.calculated_base_currency_amount)), 0),
							COUNT(e.expense_id)
						FROM expenses e
						WHERE e.account_book_id = :accountBookId
							AND e.occurred_at >= :startUtc
							AND e.occurred_at < :endUtc
							AND e.category IS NOT NULL
							AND e.category <> :incomeCategory
						GROUP BY e.category
						""")
						.setParameter("accountBookId", accountBookId)
						.setParameter("startUtc", startUtc)
						.setParameter("endUtc", endUtc)
						.setParameter("incomeCategory", Category.INCOME.ordinal())
						.getResultList();
		return rows.stream().map(this::toCategoryAmountPairCount).toList();
	}

	public AmountPairCount aggregateTravelRaw(Long accountBookId, Long travelId) {
		Object[] row =
				(Object[])
						em.createNativeQuery(
										"""
								SELECT
									COALESCE(SUM(e.local_currency_amount), 0),
									COALESCE(SUM(COALESCE(e.base_currency_amount, e.calculated_base_currency_amount)), 0),
									COUNT(*)
								FROM expenses e
								WHERE e.account_book_id = :accountBookId
									AND e.travel_id = :travelId
									AND (e.category IS NULL OR e.category <> :incomeCategory)
								""")
								.setParameter("accountBookId", accountBookId)
								.setParameter("travelId", travelId)
								.setParameter("incomeCategory", Category.INCOME.ordinal())
								.getSingleResult();
		return toAmountPairCount(row);
	}

	@SuppressWarnings("unchecked")
	public List<ExpenseRow> findExpenseRowsByAccountBook(
			Long accountBookId, LocalDateTime startUtc, LocalDateTime endUtc) {
		List<Object[]> rows =
				em.createNativeQuery(
								"""
						SELECT
							e.expense_id,
							e.account_book_id,
							e.category,
							e.local_currency_amount,
							COALESCE(e.base_currency_amount, e.calculated_base_currency_amount),
							e.local_currency_code,
							e.occurred_at,
							ab.local_country_code,
							ab.base_country_code
						FROM expenses e
						JOIN account_book ab ON e.account_book_id = ab.account_book_id
						WHERE e.account_book_id = :accountBookId
							AND e.occurred_at >= :startUtc
							AND e.occurred_at < :endUtc
						""")
						.setParameter("accountBookId", accountBookId)
						.setParameter("startUtc", startUtc)
						.setParameter("endUtc", endUtc)
						.getResultList();
		return rows.stream().map(this::toExpenseRow).toList();
	}

	public boolean hasAccountMonthlyAggregate(
			Long accountBookId,
			LocalDate monthStart,
			AnalysisMetricType metricType,
			AnalysisQualityType qualityType) {
		Number count =
				(Number)
						em.createNativeQuery(
										"""
								SELECT COUNT(*)
								FROM account_monthly_aggregate r
								WHERE r.account_book_id = :accountBookId
									AND r.target_year_month = :monthStart
									AND r.metric_type = :metricType
									AND r.quality_type = :qualityType
								""")
								.setParameter("accountBookId", accountBookId)
								.setParameter("monthStart", monthStart)
								.setParameter("metricType", metricType.name())
								.setParameter("qualityType", qualityType.name())
								.getSingleResult();
		return count != null && count.longValue() > 0L;
	}

	@SuppressWarnings("unchecked")
	public List<CategoryAmountCount> aggregateAccountMonthlyCategoryFromMonthly(
			Long accountBookId,
			LocalDate monthStart,
			AnalysisQualityType qualityType,
			CurrencyType currencyType) {
		List<Object[]> rows =
				em.createNativeQuery(
								"""
						SELECT category, COALESCE(SUM(total_amount), 0), COALESCE(SUM(expense_count), 0)
						FROM account_monthly_category_aggregate
						WHERE account_book_id = :accountBookId
							AND target_year_month = :monthStart
							AND quality_type = :qualityType
							AND currency_type = :currencyType
						GROUP BY category
						""")
						.setParameter("accountBookId", accountBookId)
						.setParameter("monthStart", monthStart)
						.setParameter("qualityType", qualityType.name())
						.setParameter("currencyType", currencyType.name())
						.getResultList();
		return rows.stream().map(this::toCategoryAmountCount).toList();
	}

	@SuppressWarnings("unchecked")
	public List<AccountAmountCount> aggregatePairMonthlyTotalByAccountFromMonthly(
			CountryCode localCountryCode,
			CountryCode baseCountryCode,
			LocalDate monthStart,
			AnalysisMetricType metricType,
			AnalysisQualityType qualityType) {
		List<Object[]> rows =
				em.createNativeQuery(
								"""
						SELECT r.account_book_id, COALESCE(SUM(r.metric_value), 0), COUNT(*)
						FROM account_monthly_aggregate r
						JOIN account_book ab ON r.account_book_id = ab.account_book_id
						WHERE ab.local_country_code = :localCountryCode
							AND ab.base_country_code = :baseCountryCode
							AND r.target_year_month = :monthStart
							AND r.metric_type = :metricType
							AND r.quality_type = :qualityType
						GROUP BY r.account_book_id
						""")
						.setParameter("localCountryCode", localCountryCode.name())
						.setParameter("baseCountryCode", baseCountryCode.name())
						.setParameter("monthStart", monthStart)
						.setParameter("metricType", metricType.name())
						.setParameter("qualityType", qualityType.name())
						.getResultList();
		return rows.stream().map(this::toAccountAmountCount).toList();
	}

	@SuppressWarnings("unchecked")
	public List<AccountCategoryAmountCount> aggregatePairMonthlyCategoryByAccountFromMonthly(
			CountryCode localCountryCode,
			CountryCode baseCountryCode,
			LocalDate monthStart,
			AnalysisQualityType qualityType,
			CurrencyType currencyType) {
		List<Object[]> rows =
				em.createNativeQuery(
								"""
						SELECT r.account_book_id, r.category, COALESCE(SUM(r.total_amount), 0), COALESCE(SUM(r.expense_count), 0)
						FROM account_monthly_category_aggregate r
						JOIN account_book ab ON r.account_book_id = ab.account_book_id
						WHERE ab.local_country_code = :localCountryCode
							AND ab.base_country_code = :baseCountryCode
							AND r.target_year_month = :monthStart
							AND r.quality_type = :qualityType
							AND r.currency_type = :currencyType
						GROUP BY r.account_book_id, r.category
						""")
						.setParameter("localCountryCode", localCountryCode.name())
						.setParameter("baseCountryCode", baseCountryCode.name())
						.setParameter("monthStart", monthStart)
						.setParameter("qualityType", qualityType.name())
						.setParameter("currencyType", currencyType.name())
						.getResultList();
		return rows.stream().map(this::toAccountCategoryAmountCount).toList();
	}

	private AccountAmountCount toAccountAmountCount(Object[] row) {
		Long accountBookId = ((Number) row[0]).longValue();
		BigDecimal amount = row[1] == null ? BigDecimal.ZERO : new BigDecimal(row[1].toString());
		long count = row[2] == null ? 0L : ((Number) row[2]).longValue();
		return new AccountAmountCount(accountBookId, amount, count);
	}

	private ExpenseRow toExpenseRow(Object[] row) {
		Long expenseId = row[0] == null ? null : ((Number) row[0]).longValue();
		Long accountBookId = row[1] == null ? null : ((Number) row[1]).longValue();
		Object categoryValue = row[2];
		BigDecimal localAmount = row[3] == null ? null : new BigDecimal(row[3].toString());
		BigDecimal baseAmount = row[4] == null ? null : new BigDecimal(row[4].toString());
		String localCurrencyCode = row[5] == null ? null : row[5].toString();
		LocalDateTime occurredAtUtc = toLocalDateTime(row[6]);
		String localCountryCode = row[7] == null ? null : row[7].toString();
		String baseCountryCode = row[8] == null ? null : row[8].toString();
		return new ExpenseRow(
				expenseId,
				accountBookId,
				categoryValue,
				localAmount,
				baseAmount,
				localCurrencyCode,
				occurredAtUtc,
				localCountryCode,
				baseCountryCode);
	}

	private AmountPairCount toAmountPairCount(Object[] row) {
		BigDecimal localAmount =
				row[0] == null ? BigDecimal.ZERO : new BigDecimal(row[0].toString());
		BigDecimal baseAmount =
				row[1] == null ? BigDecimal.ZERO : new BigDecimal(row[1].toString());
		long count = row[2] == null ? 0L : ((Number) row[2]).longValue();
		return new AmountPairCount(localAmount, baseAmount, count);
	}

	private CategoryAmountCount toCategoryAmountCount(Object[] row) {
		Integer categoryOrdinal = CategoryOrdinalParser.parse(row[0]);
		BigDecimal amount = row[1] == null ? BigDecimal.ZERO : new BigDecimal(row[1].toString());
		long count = row[2] == null ? 0L : ((Number) row[2]).longValue();
		return new CategoryAmountCount(categoryOrdinal, amount, count);
	}

	private CategoryAmountPairCount toCategoryAmountPairCount(Object[] row) {
		Integer categoryOrdinal = CategoryOrdinalParser.parse(row[0]);
		BigDecimal localAmount =
				row[1] == null ? BigDecimal.ZERO : new BigDecimal(row[1].toString());
		BigDecimal baseAmount =
				row[2] == null ? BigDecimal.ZERO : new BigDecimal(row[2].toString());
		long count = row[3] == null ? 0L : ((Number) row[3]).longValue();
		return new CategoryAmountPairCount(categoryOrdinal, localAmount, baseAmount, count);
	}

	private AccountCategoryAmountCount toAccountCategoryAmountCount(Object[] row) {
		Long accountBookId = ((Number) row[0]).longValue();
		Integer categoryOrdinal = CategoryOrdinalParser.parse(row[1]);
		BigDecimal amount = row[2] == null ? BigDecimal.ZERO : new BigDecimal(row[2].toString());
		long count = row[3] == null ? 0L : ((Number) row[3]).longValue();
		return new AccountCategoryAmountCount(accountBookId, categoryOrdinal, amount, count);
	}

	private LocalDateTime toLocalDateTime(Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof LocalDateTime localDateTime) {
			return localDateTime;
		}
		if (value instanceof java.sql.Timestamp timestamp) {
			return timestamp.toLocalDateTime();
		}
		return LocalDateTime.parse(value.toString().replace(' ', 'T'));
	}

	public record AmountPairCount(
			BigDecimal totalLocalAmount, BigDecimal totalBaseAmount, long expenseCount) {}

	public record AccountAmountCount(
			Long accountBookId, BigDecimal totalAmount, long expenseCount) {}

	public record CategoryAmountCount(
			Integer categoryOrdinal, BigDecimal totalAmount, long expenseCount) {}

	public record CategoryAmountPairCount(
			Integer categoryOrdinal,
			BigDecimal totalLocalAmount,
			BigDecimal totalBaseAmount,
			long expenseCount) {}

	public record AccountCategoryAmountCount(
			Long accountBookId,
			Integer categoryOrdinal,
			BigDecimal totalAmount,
			long expenseCount) {}

	public record ExpenseRow(
			Long expenseId,
			Long accountBookId,
			Object categoryValue,
			BigDecimal localAmount,
			BigDecimal baseAmount,
			String localCurrencyCode,
			LocalDateTime occurredAtUtc,
			String localCountryCode,
			String baseCountryCode) {}

	public record LocalCurrencyGroupRow(
			String localCurrencyCode, BigDecimal localAmountSum, long expenseCount) {}

	public record CategoryLocalCurrencyGroupRow(
			Object categoryValue,
			String localCurrencyCode,
			BigDecimal localAmountSum,
			long expenseCount) {}

	@SuppressWarnings("unchecked")
	public List<LocalCurrencyGroupRow> aggregateLocalAmountGroupedByCurrency(
			Long accountBookId, LocalDateTime startUtc, LocalDateTime endUtc) {
		List<Object[]> rows =
				em.createNativeQuery(
								"""
						SELECT
							e.local_currency_code,
							COALESCE(SUM(e.local_currency_amount), 0),
							COUNT(*)
						FROM expenses e
						WHERE e.account_book_id = :accountBookId
							AND e.occurred_at >= :startUtc
							AND e.occurred_at < :endUtc
							AND (e.category IS NULL OR e.category <> :incomeCategory)
						GROUP BY e.local_currency_code
						""")
						.setParameter("accountBookId", accountBookId)
						.setParameter("startUtc", startUtc)
						.setParameter("endUtc", endUtc)
						.setParameter("incomeCategory", Category.INCOME.ordinal())
						.getResultList();
		return rows.stream().map(this::toLocalCurrencyGroupRow).toList();
	}

	@SuppressWarnings("unchecked")
	public List<CategoryLocalCurrencyGroupRow> aggregateLocalAmountGroupedByCurrencyAndCategory(
			Long accountBookId, LocalDateTime startUtc, LocalDateTime endUtc) {
		List<Object[]> rows =
				em.createNativeQuery(
								"""
						SELECT
							e.category,
							e.local_currency_code,
							COALESCE(SUM(e.local_currency_amount), 0),
							COUNT(e.expense_id)
						FROM expenses e
						WHERE e.account_book_id = :accountBookId
							AND e.occurred_at >= :startUtc
							AND e.occurred_at < :endUtc
							AND e.category IS NOT NULL
							AND e.category <> :incomeCategory
						GROUP BY e.category, e.local_currency_code
						""")
						.setParameter("accountBookId", accountBookId)
						.setParameter("startUtc", startUtc)
						.setParameter("endUtc", endUtc)
						.setParameter("incomeCategory", Category.INCOME.ordinal())
						.getResultList();
		return rows.stream().map(this::toCategoryLocalCurrencyGroupRow).toList();
	}

	@SuppressWarnings("unchecked")
	public List<LocalCurrencyGroupRow> aggregateTravelLocalAmountGroupedByCurrency(
			Long accountBookId, Long travelId) {
		List<Object[]> rows =
				em.createNativeQuery(
								"""
						SELECT
							e.local_currency_code,
							COALESCE(SUM(e.local_currency_amount), 0),
							COUNT(*)
						FROM expenses e
						WHERE e.account_book_id = :accountBookId
							AND e.travel_id = :travelId
							AND (e.category IS NULL OR e.category <> :incomeCategory)
						GROUP BY e.local_currency_code
						""")
						.setParameter("accountBookId", accountBookId)
						.setParameter("travelId", travelId)
						.setParameter("incomeCategory", Category.INCOME.ordinal())
						.getResultList();
		return rows.stream().map(this::toLocalCurrencyGroupRow).toList();
	}

	private LocalCurrencyGroupRow toLocalCurrencyGroupRow(Object[] row) {
		String localCurrencyCode = row[0] == null ? null : row[0].toString();
		BigDecimal localAmountSum =
				row[1] == null ? BigDecimal.ZERO : new BigDecimal(row[1].toString());
		long expenseCount = row[2] == null ? 0L : ((Number) row[2]).longValue();
		return new LocalCurrencyGroupRow(localCurrencyCode, localAmountSum, expenseCount);
	}

	private CategoryLocalCurrencyGroupRow toCategoryLocalCurrencyGroupRow(Object[] row) {
		Object categoryValue = row[0];
		String localCurrencyCode = row[1] == null ? null : row[1].toString();
		BigDecimal localAmountSum =
				row[2] == null ? BigDecimal.ZERO : new BigDecimal(row[2].toString());
		long expenseCount = row[3] == null ? 0L : ((Number) row[3]).longValue();
		return new CategoryLocalCurrencyGroupRow(
				categoryValue, localCurrencyCode, localAmountSum, expenseCount);
	}
}
