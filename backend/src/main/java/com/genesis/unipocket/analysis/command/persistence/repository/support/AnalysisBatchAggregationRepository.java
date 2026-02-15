package com.genesis.unipocket.analysis.command.persistence.repository.support;

import com.genesis.unipocket.analysis.command.persistence.entity.AnalysisMetricType;
import com.genesis.unipocket.analysis.command.persistence.entity.AnalysisQualityType;
import com.genesis.unipocket.global.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CountryCode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class AnalysisBatchAggregationRepository {

	@PersistenceContext private EntityManager em;

	public Optional<LocalDateTime> findEarliestOccurredAtUtcUpdatedAfter(
			CountryCode countryCode, LocalDateTime updatedAfterUtc) {
		Object row =
				em.createNativeQuery(
								"""
						SELECT MIN(e.occurred_at)
						FROM expenses e
						JOIN account_book ab ON e.account_book_id = ab.account_book_id
						WHERE ab.local_country_code = :countryCode
							AND e.updated_at > :updatedAfterUtc
						""")
						.setParameter("countryCode", countryCode.name())
						.setParameter("updatedAfterUtc", updatedAfterUtc)
						.getSingleResult();

		if (row == null) {
			return Optional.empty();
		}
		if (row instanceof LocalDateTime localDateTime) {
			return Optional.of(localDateTime);
		}
		if (row instanceof java.sql.Timestamp timestamp) {
			return Optional.of(timestamp.toLocalDateTime());
		}
		return Optional.of(LocalDateTime.parse(row.toString().replace(' ', 'T')));
	}

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

	public AmountCount aggregateComparableCountryDailyRaw(
			CountryCode countryCode, LocalDateTime startUtc, LocalDateTime endUtc) {
		Object[] row =
				(Object[])
						em.createNativeQuery(
										"""
								SELECT COALESCE(SUM(e.local_currency_amount), 0), COUNT(*)
								FROM expenses e
								JOIN account_book ab ON e.account_book_id = ab.account_book_id
								WHERE ab.local_country_code = :countryCode
									AND ab.base_country_code = :countryCode
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
	public List<CategoryAmountCount> aggregateComparableCountryDailyRawByCategory(
			CountryCode countryCode, LocalDateTime startUtc, LocalDateTime endUtc) {
		List<Object[]> rows =
				em.createNativeQuery(
								"""
							SELECT e.category, COALESCE(SUM(e.local_currency_amount), 0), COUNT(e.expense_id)
							FROM expenses e
							JOIN account_book ab ON e.account_book_id = ab.account_book_id
							WHERE ab.local_country_code = :countryCode
								AND ab.base_country_code = :countryCode
								AND e.occurred_at >= :startUtc
								AND e.occurred_at < :endUtc
								AND e.local_currency_amount IS NOT NULL
								AND e.category IS NOT NULL
							GROUP BY e.category
							""")
						.setParameter("countryCode", countryCode.name())
						.setParameter("startUtc", startUtc)
						.setParameter("endUtc", endUtc)
						.getResultList();
		return rows.stream().map(this::toCategoryAmountCount).toList();
	}

	@SuppressWarnings("unchecked")
	public List<AccountCategoryAmountCount> aggregateComparableAccountDailyRawByCategory(
			CountryCode countryCode, LocalDateTime startUtc, LocalDateTime endUtc) {
		List<Object[]> rows =
				em.createNativeQuery(
								"""
							SELECT ab.account_book_id, e.category, COALESCE(SUM(e.local_currency_amount), 0), COUNT(e.expense_id)
							FROM account_book ab
							LEFT JOIN expenses e ON e.account_book_id = ab.account_book_id
								AND e.occurred_at >= :startUtc
								AND e.occurred_at < :endUtc
								AND e.local_currency_amount IS NOT NULL
								AND e.category IS NOT NULL
							WHERE ab.local_country_code = :countryCode
								AND ab.base_country_code = :countryCode
							GROUP BY ab.account_book_id, e.category
							HAVING e.category IS NOT NULL
							""")
						.setParameter("countryCode", countryCode.name())
						.setParameter("startUtc", startUtc)
						.setParameter("endUtc", endUtc)
						.getResultList();
		return rows.stream().map(this::toAccountCategoryAmountCount).toList();
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

	@SuppressWarnings("unchecked")
	public List<ExpenseRow> findCountryDailyExpenseRows(
			CountryCode countryCode, LocalDateTime startUtc, LocalDateTime endUtc) {
		List<Object[]> rows =
				em.createNativeQuery(
								"""
							SELECT
								e.expense_id,
								e.account_book_id,
								e.category,
								e.local_currency_amount,
								e.local_currency_code,
								e.occurred_at,
								ab.local_country_code,
								ab.base_country_code
							FROM expenses e
							JOIN account_book ab ON e.account_book_id = ab.account_book_id
							WHERE ab.local_country_code = :countryCode
								AND e.occurred_at >= :startUtc
								AND e.occurred_at < :endUtc
							""")
						.setParameter("countryCode", countryCode.name())
						.setParameter("startUtc", startUtc)
						.setParameter("endUtc", endUtc)
						.getResultList();
		return rows.stream().map(this::toExpenseRow).toList();
	}

	@SuppressWarnings("unchecked")
	public List<ExpenseRow> findComparableExpenseRows(
			CountryCode countryCode, LocalDateTime startUtc, LocalDateTime endUtc) {
		List<Object[]> rows =
				em.createNativeQuery(
								"""
							SELECT
								e.expense_id,
								e.account_book_id,
								e.category,
								e.local_currency_amount,
								e.local_currency_code,
								e.occurred_at,
								ab.local_country_code,
								ab.base_country_code
							FROM expenses e
							JOIN account_book ab ON e.account_book_id = ab.account_book_id
							WHERE ab.local_country_code = :countryCode
								AND ab.base_country_code = :countryCode
								AND e.occurred_at >= :startUtc
								AND e.occurred_at < :endUtc
							""")
						.setParameter("countryCode", countryCode.name())
						.setParameter("startUtc", startUtc)
						.setParameter("endUtc", endUtc)
						.getResultList();
		return rows.stream().map(this::toExpenseRow).toList();
	}

	@SuppressWarnings("unchecked")
	public List<Long> findAccountBookIdsByLocalEqualsBaseCountry(CountryCode countryCode) {
		List<Object> rows =
				em.createNativeQuery(
								"""
							SELECT ab.account_book_id
							FROM account_book ab
							WHERE ab.local_country_code = :countryCode
								AND ab.base_country_code = :countryCode
							""")
						.setParameter("countryCode", countryCode.name())
						.getResultList();
		return rows.stream().map(row -> ((Number) row).longValue()).toList();
	}

	public AmountCount aggregateCountryMonthlyFromDaily(
			CountryCode countryCode,
			LocalDate monthStart,
			LocalDate nextMonthStart,
			AnalysisMetricType metricType,
			AnalysisQualityType qualityType) {
		Object[] row =
				(Object[])
						em.createNativeQuery(
										"""
								SELECT COALESCE(SUM(r.metric_value), 0), COUNT(*)
								FROM country_daily_analysis_result r
								WHERE r.country_code = :countryCode
									AND r.target_local_date >= :monthStart
									AND r.target_local_date < :nextMonthStart
									AND r.metric_type = :metricType
									AND r.quality_type = :qualityType
								""")
								.setParameter("countryCode", countryCode.name())
								.setParameter("monthStart", monthStart)
								.setParameter("nextMonthStart", nextMonthStart)
								.setParameter("metricType", metricType.name())
								.setParameter("qualityType", qualityType.name())
								.getSingleResult();
		return toAmountCount(row);
	}

	@SuppressWarnings("unchecked")
	public List<CategoryAmountCount> aggregateCountryMonthlyCategoryFromDaily(
			CountryCode countryCode,
			LocalDate monthStart,
			LocalDate nextMonthStart,
			AnalysisQualityType qualityType) {
		List<Object[]> rows =
				em.createNativeQuery(
								"""
							SELECT category, COALESCE(SUM(total_amount), 0), COALESCE(SUM(expense_count), 0)
							FROM country_daily_category_aggregate
							WHERE country_code = :countryCode
								AND target_local_date >= :monthStart
								AND target_local_date < :nextMonthStart
								AND quality_type = :qualityType
							GROUP BY category
							""")
						.setParameter("countryCode", countryCode.name())
						.setParameter("monthStart", monthStart)
						.setParameter("nextMonthStart", nextMonthStart)
						.setParameter("qualityType", qualityType.name())
						.getResultList();
		return rows.stream().map(this::toCategoryAmountCount).toList();
	}

	@SuppressWarnings("unchecked")
	public List<AccountAmountCount> aggregateComparableAccountMonthlyFromDaily(
			CountryCode countryCode,
			LocalDate monthStart,
			LocalDate nextMonthStart,
			AnalysisMetricType metricType,
			AnalysisQualityType qualityType) {
		List<Object[]> rows =
				em.createNativeQuery(
								"""
							SELECT r.account_book_id, COALESCE(SUM(r.metric_value), 0), COUNT(*)
							FROM account_daily_aggregate r
							JOIN account_book ab ON r.account_book_id = ab.account_book_id
							WHERE ab.local_country_code = :countryCode
								AND ab.base_country_code = :countryCode
								AND r.target_local_date >= :monthStart
								AND r.target_local_date < :nextMonthStart
								AND r.metric_type = :metricType
								AND r.quality_type = :qualityType
							GROUP BY r.account_book_id
							""")
						.setParameter("countryCode", countryCode.name())
						.setParameter("monthStart", monthStart)
						.setParameter("nextMonthStart", nextMonthStart)
						.setParameter("metricType", metricType.name())
						.setParameter("qualityType", qualityType.name())
						.getResultList();
		return rows.stream().map(this::toAccountAmountCount).toList();
	}

	@SuppressWarnings("unchecked")
	public List<AccountCategoryAmountCount> aggregateComparableAccountMonthlyCategoryFromDaily(
			CountryCode countryCode,
			LocalDate monthStart,
			LocalDate nextMonthStart,
			AnalysisQualityType qualityType) {
		List<Object[]> rows =
				em.createNativeQuery(
								"""
							SELECT r.account_book_id, r.category, COALESCE(SUM(r.total_amount), 0), COALESCE(SUM(r.expense_count), 0)
							FROM account_daily_category_aggregate r
							JOIN account_book ab ON r.account_book_id = ab.account_book_id
							WHERE ab.local_country_code = :countryCode
								AND ab.base_country_code = :countryCode
								AND r.target_local_date >= :monthStart
								AND r.target_local_date < :nextMonthStart
								AND r.quality_type = :qualityType
							GROUP BY r.account_book_id, r.category
							""")
						.setParameter("countryCode", countryCode.name())
						.setParameter("monthStart", monthStart)
						.setParameter("nextMonthStart", nextMonthStart)
						.setParameter("qualityType", qualityType.name())
						.getResultList();
		return rows.stream().map(this::toAccountCategoryAmountCount).toList();
	}

	public AmountCount aggregateAccountMonthlyFromDaily(
			Long accountBookId,
			LocalDate monthStart,
			LocalDate nextMonthStart,
			AnalysisMetricType metricType,
			AnalysisQualityType qualityType) {
		Object[] row =
				(Object[])
						em.createNativeQuery(
										"""
								SELECT COALESCE(SUM(r.metric_value), 0), COUNT(*)
								FROM account_daily_aggregate r
								WHERE r.account_book_id = :accountBookId
									AND r.target_local_date >= :monthStart
									AND r.target_local_date < :nextMonthStart
									AND r.metric_type = :metricType
									AND r.quality_type = :qualityType
								""")
								.setParameter("accountBookId", accountBookId)
								.setParameter("monthStart", monthStart)
								.setParameter("nextMonthStart", nextMonthStart)
								.setParameter("metricType", metricType.name())
								.setParameter("qualityType", qualityType.name())
								.getSingleResult();
		return toAmountCount(row);
	}

	@SuppressWarnings("unchecked")
	public List<CategoryAmountCount> aggregateAccountMonthlyCategoryFromDaily(
			Long accountBookId,
			LocalDate monthStart,
			LocalDate nextMonthStart,
			AnalysisQualityType qualityType) {
		List<Object[]> rows =
				em.createNativeQuery(
								"""
							SELECT category, COALESCE(SUM(total_amount), 0), COALESCE(SUM(expense_count), 0)
							FROM account_daily_category_aggregate
							WHERE account_book_id = :accountBookId
								AND target_local_date >= :monthStart
								AND target_local_date < :nextMonthStart
								AND quality_type = :qualityType
							GROUP BY category
							""")
						.setParameter("accountBookId", accountBookId)
						.setParameter("monthStart", monthStart)
						.setParameter("nextMonthStart", nextMonthStart)
						.setParameter("qualityType", qualityType.name())
						.getResultList();
		return rows.stream().map(this::toCategoryAmountCount).toList();
	}

	@SuppressWarnings("unchecked")
	public List<DailyAmountCount> aggregateCountryDailySeries(
			CountryCode countryCode,
			LocalDate rangeStart,
			LocalDate rangeEndExclusive,
			AnalysisQualityType qualityType) {
		List<Object[]> rows =
				em.createNativeQuery(
								"""
							SELECT r.target_local_date,
								COALESCE(SUM(CASE WHEN r.metric_type = 'TOTAL_LOCAL_AMOUNT' THEN r.metric_value ELSE 0 END), 0) AS total_amount,
								COALESCE(SUM(CASE WHEN r.metric_type = 'EXPENSE_COUNT' THEN r.metric_value ELSE 0 END), 0) AS expense_count
							FROM country_daily_analysis_result r
							WHERE r.country_code = :countryCode
								AND r.target_local_date >= :rangeStart
								AND r.target_local_date < :rangeEndExclusive
								AND r.quality_type = :qualityType
							GROUP BY r.target_local_date
							ORDER BY r.target_local_date ASC
							""")
						.setParameter("countryCode", countryCode.name())
						.setParameter("rangeStart", rangeStart)
						.setParameter("rangeEndExclusive", rangeEndExclusive)
						.setParameter("qualityType", qualityType.name())
						.getResultList();
		return rows.stream().map(this::toDailyAmountCount).toList();
	}

	@SuppressWarnings("unchecked")
	public List<DailyAmountCount> aggregateAccountDailySeries(
			Long accountBookId,
			LocalDate rangeStart,
			LocalDate rangeEndExclusive,
			AnalysisQualityType qualityType) {
		List<Object[]> rows =
				em.createNativeQuery(
								"""
							SELECT r.target_local_date,
								COALESCE(SUM(CASE WHEN r.metric_type = 'TOTAL_LOCAL_AMOUNT' THEN r.metric_value ELSE 0 END), 0) AS total_amount,
								COALESCE(SUM(CASE WHEN r.metric_type = 'EXPENSE_COUNT' THEN r.metric_value ELSE 0 END), 0) AS expense_count
							FROM account_daily_aggregate r
							WHERE r.account_book_id = :accountBookId
								AND r.target_local_date >= :rangeStart
								AND r.target_local_date < :rangeEndExclusive
								AND r.quality_type = :qualityType
							GROUP BY r.target_local_date
							ORDER BY r.target_local_date ASC
							""")
						.setParameter("accountBookId", accountBookId)
						.setParameter("rangeStart", rangeStart)
						.setParameter("rangeEndExclusive", rangeEndExclusive)
						.setParameter("qualityType", qualityType.name())
						.getResultList();
		return rows.stream().map(this::toDailyAmountCount).toList();
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

	private ExpenseRow toExpenseRow(Object[] row) {
		Long expenseId = row[0] == null ? null : ((Number) row[0]).longValue();
		Long accountBookId = row[1] == null ? null : ((Number) row[1]).longValue();
		Object categoryValue = row[2];
		BigDecimal amount = row[3] == null ? null : new BigDecimal(row[3].toString());
		String localCurrencyCode = row[4] == null ? null : row[4].toString();
		LocalDateTime occurredAtUtc = toLocalDateTime(row[5]);
		String localCountryCode = row[6] == null ? null : row[6].toString();
		String baseCountryCode = row[7] == null ? null : row[7].toString();
		return new ExpenseRow(
				expenseId,
				accountBookId,
				categoryValue,
				amount,
				localCurrencyCode,
				occurredAtUtc,
				localCountryCode,
				baseCountryCode);
	}

	private CategoryAmountCount toCategoryAmountCount(Object[] row) {
		Integer categoryOrdinal = parseCategoryOrdinal(row[0]);
		BigDecimal amount = row[1] == null ? BigDecimal.ZERO : new BigDecimal(row[1].toString());
		long count = row[2] == null ? 0L : ((Number) row[2]).longValue();
		return new CategoryAmountCount(categoryOrdinal, amount, count);
	}

	private AccountCategoryAmountCount toAccountCategoryAmountCount(Object[] row) {
		Long accountBookId = ((Number) row[0]).longValue();
		Integer categoryOrdinal = parseCategoryOrdinal(row[1]);
		BigDecimal amount = row[2] == null ? BigDecimal.ZERO : new BigDecimal(row[2].toString());
		long count = row[3] == null ? 0L : ((Number) row[3]).longValue();
		return new AccountCategoryAmountCount(accountBookId, categoryOrdinal, amount, count);
	}

	private DailyAmountCount toDailyAmountCount(Object[] row) {
		LocalDate targetDate = toLocalDate(row[0]);
		BigDecimal totalAmount =
				row[1] == null ? BigDecimal.ZERO : new BigDecimal(row[1].toString());
		long expenseCount = row[2] == null ? 0L : new BigDecimal(row[2].toString()).longValue();
		return new DailyAmountCount(targetDate, totalAmount, expenseCount);
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

	private LocalDate toLocalDate(Object value) {
		if (value instanceof LocalDate localDate) {
			return localDate;
		}
		if (value instanceof java.sql.Date date) {
			return date.toLocalDate();
		}
		return LocalDate.parse(value.toString());
	}

	private Integer parseCategoryOrdinal(Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof Number number) {
			return number.intValue();
		}
		String raw = value.toString();
		try {
			return Integer.parseInt(raw);
		} catch (NumberFormatException ignored) {
			try {
				return Category.valueOf(raw).ordinal();
			} catch (IllegalArgumentException e) {
				return null;
			}
		}
	}

	public record AmountCount(BigDecimal totalAmount, long expenseCount) {}

	public record AccountAmountCount(
			Long accountBookId, BigDecimal totalAmount, long expenseCount) {}

	public record CategoryAmountCount(
			Integer categoryOrdinal, BigDecimal totalAmount, long expenseCount) {}

	public record AccountCategoryAmountCount(
			Long accountBookId,
			Integer categoryOrdinal,
			BigDecimal totalAmount,
			long expenseCount) {}

	public record DailyAmountCount(
			LocalDate targetLocalDate, BigDecimal totalAmount, long expenseCount) {}

	public record ExpenseRow(
			Long expenseId,
			Long accountBookId,
			Object categoryValue,
			BigDecimal localAmount,
			String localCurrencyCode,
			LocalDateTime occurredAtUtc,
			String localCountryCode,
			String baseCountryCode) {}
}
