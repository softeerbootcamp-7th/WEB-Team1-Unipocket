package com.genesis.unipocket.analysis.command.application;

import com.genesis.unipocket.accountbook.command.persistence.entity.AccountBookEntity;
import com.genesis.unipocket.accountbook.command.persistence.repository.AccountBookCommandRepository;
import com.genesis.unipocket.analysis.command.config.AnalysisBatchProperties;
import com.genesis.unipocket.analysis.command.persistence.entity.AccountMonthlyAggregateEntity;
import com.genesis.unipocket.analysis.command.persistence.entity.AccountMonthlyCategoryAggregateEntity;
import com.genesis.unipocket.analysis.command.persistence.entity.AnalysisBatchJobStatus;
import com.genesis.unipocket.analysis.command.persistence.entity.AnalysisMetricType;
import com.genesis.unipocket.analysis.command.persistence.entity.AnalysisMonthlyDirtyEntity;
import com.genesis.unipocket.analysis.command.persistence.entity.AnalysisQualityType;
import com.genesis.unipocket.analysis.command.persistence.repository.AccountMonthlyAggregateRepository;
import com.genesis.unipocket.analysis.command.persistence.repository.AccountMonthlyCategoryAggregateRepository;
import com.genesis.unipocket.analysis.command.persistence.repository.AnalysisMonthlyDirtyRepository;
import com.genesis.unipocket.analysis.command.persistence.repository.PairMonthlyAggregateRepository;
import com.genesis.unipocket.analysis.command.persistence.repository.PairMonthlyCategoryAggregateRepository;
import com.genesis.unipocket.analysis.command.persistence.repository.support.AnalysisBatchAggregationRepository;
import com.genesis.unipocket.analysis.command.persistence.repository.support.AnalysisBatchAggregationRepository.AccountAmountCount;
import com.genesis.unipocket.analysis.command.persistence.repository.support.AnalysisBatchAggregationRepository.AccountCategoryAmountCount;
import com.genesis.unipocket.analysis.command.persistence.repository.support.AnalysisBatchAggregationRepository.AmountPairCount;
import com.genesis.unipocket.analysis.command.persistence.repository.support.AnalysisBatchAggregationRepository.CategoryAmountPairCount;
import com.genesis.unipocket.analysis.command.persistence.repository.support.AnalysisBatchAggregationRepository.ExpenseRow;
import com.genesis.unipocket.analysis.common.enums.CurrencyType;
import com.genesis.unipocket.analysis.common.util.CategoryOrdinalParser;
import com.genesis.unipocket.analysis.common.util.QuantileUtil;
import com.genesis.unipocket.global.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CountryCode;
import com.genesis.unipocket.global.util.CountryCodeTimezoneMapper;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class CountryMonthlyDirtyAggregationService {

	private static final MathContext MC = MathContext.DECIMAL64;
	private static final Collection<AnalysisBatchJobStatus> CLAIMABLE_STATUSES =
			List.of(AnalysisBatchJobStatus.PENDING, AnalysisBatchJobStatus.RETRY);

	private final AnalysisMonthlyDirtyRepository monthlyDirtyRepository;
	private final AccountBookCommandRepository accountBookRepository;
	private final AnalysisBatchAggregationRepository aggregationRepository;
	private final AccountMonthlyAggregateRepository accountMonthlyAggregateRepository;
	private final AccountMonthlyCategoryAggregateRepository
			accountMonthlyCategoryAggregateRepository;
	private final PairMonthlyAggregateRepository pairMonthlyAggregateRepository;
	private final PairMonthlyCategoryAggregateRepository pairMonthlyCategoryAggregateRepository;
	private final AnalysisBatchProperties properties;
	private final PlatformTransactionManager transactionManager;

	public void processCountryDirtyRows(CountryCode countryCode) {
		LocalDateTime nowUtc = LocalDateTime.now(ZoneOffset.UTC);
		List<Long> candidateIds =
				monthlyDirtyRepository.findDispatchableIdsByCountryCode(
						countryCode,
						CLAIMABLE_STATUSES,
						nowUtc,
						PageRequest.of(0, properties.getDispatchBatchSize()));
		if (candidateIds.isEmpty()) {
			return;
		}

		TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
		Set<PairMonthKey> affectedPairMonths = new LinkedHashSet<>();
		for (Long dirtyId : candidateIds) {
			Boolean claimed =
					txTemplate.execute(
							status -> {
								LocalDateTime claimTime = LocalDateTime.now(ZoneOffset.UTC);
								int updated =
										monthlyDirtyRepository.claimDirty(
												dirtyId,
												AnalysisBatchJobStatus.RUNNING,
												CLAIMABLE_STATUSES,
												claimTime,
												claimTime.plusMinutes(
														properties.getLeaseMinutes()));
								return updated == 1;
							});

			if (!Boolean.TRUE.equals(claimed)) {
				continue;
			}

			try {
				PairMonthKey affectedKey =
						txTemplate.execute(status -> processOneDirtyRow(dirtyId));
				if (affectedKey != null) {
					affectedPairMonths.add(affectedKey);
				}
			} catch (Exception e) {
				log.error("Failed to process monthly dirty row. dirtyId={}", dirtyId, e);
				txTemplate.executeWithoutResult(status -> markFailure(dirtyId, e));
			}
		}

		for (PairMonthKey pairMonthKey : affectedPairMonths) {
			txTemplate.executeWithoutResult(status -> refreshPairMonthlyAggregates(pairMonthKey));
		}
	}

	private PairMonthKey processOneDirtyRow(Long dirtyId) {
		AnalysisMonthlyDirtyEntity dirty =
				monthlyDirtyRepository
						.findById(dirtyId)
						.orElseThrow(
								() ->
										new IllegalStateException(
												"Monthly dirty row not found: " + dirtyId));
		if (dirty.getStatus() != AnalysisBatchJobStatus.RUNNING) {
			return null;
		}

		AccountBookEntity accountBook =
				accountBookRepository
						.findById(dirty.getAccountBookId())
						.orElseThrow(
								() ->
										new IllegalStateException(
												"Account book not found while processing monthly"
														+ " dirty: "
														+ dirty.getAccountBookId()));

		if (accountBook.getLocalCountryCode() != dirty.getCountryCode()) {
			dirty.markSuccess(LocalDateTime.now(ZoneOffset.UTC));
			return null;
		}

		LocalDate monthStart = dirty.getTargetYearMonth().withDayOfMonth(1);
		LocalDate nextMonthStart = monthStart.plusMonths(1);
		ZoneId zoneId = CountryCodeTimezoneMapper.getZoneId(dirty.getCountryCode());
		LocalDateTime startUtc = toUtc(monthStart, zoneId);
		LocalDateTime endUtc = toUtc(nextMonthStart, zoneId);

		AmountPairCount rawAmountCount =
				aggregationRepository.aggregateAccountBookMonthlyRaw(
						dirty.getAccountBookId(), startUtc, endUtc);
		List<CategoryAmountPairCount> rawCategoryRows =
				aggregationRepository.aggregateAccountBookMonthlyRawByCategory(
						dirty.getAccountBookId(), startUtc, endUtc);
		upsertMonthlyMetrics(
				dirty.getAccountBookId(),
				dirty.getCountryCode(),
				monthStart,
				rawAmountCount,
				AnalysisQualityType.RAW);
		upsertMonthlyCategoryMetrics(
				dirty.getAccountBookId(),
				dirty.getCountryCode(),
				monthStart,
				rawCategoryRows,
				AnalysisQualityType.RAW);

		List<ExpenseRow> monthlyRows =
				aggregationRepository.findExpenseRowsByAccountBook(
						dirty.getAccountBookId(), startUtc, endUtc);
		CleanedMonthlyAggregation cleaned = calculateCleaned(monthlyRows);
		upsertMonthlyMetrics(
				dirty.getAccountBookId(),
				dirty.getCountryCode(),
				monthStart,
				new AmountPairCount(
						cleaned.totalLocalAmount(),
						cleaned.totalBaseAmount(),
						cleaned.expenseCount()),
				AnalysisQualityType.CLEANED);
		upsertMonthlyCategoryMetrics(
				dirty.getAccountBookId(),
				dirty.getCountryCode(),
				monthStart,
				cleaned.categoryAmountCounts(),
				AnalysisQualityType.CLEANED);

		dirty.markSuccess(LocalDateTime.now(ZoneOffset.UTC));
		return new PairMonthKey(
				accountBook.getLocalCountryCode(), accountBook.getBaseCountryCode(), monthStart);
	}

	private CleanedMonthlyAggregation calculateCleaned(List<ExpenseRow> rows) {
		Map<Integer, List<BigDecimal>> localAmountsByCategory = new HashMap<>();
		Map<Integer, List<BigDecimal>> baseAmountsByCategory = new HashMap<>();
		List<ValidAmountRow> validRows = new ArrayList<>(rows.size());

		for (ExpenseRow row : rows) {
			BigDecimal localAmount = row.localAmount();
			if (localAmount == null) {
				continue;
			}
			if (localAmount.compareTo(properties.getCleanedMinAmount()) < 0) {
				continue;
			}
			if (localAmount.compareTo(properties.getCleanedMaxAmount()) > 0) {
				continue;
			}
			Integer categoryOrdinal = parseCategoryOrdinal(row.categoryValue());
			if (categoryOrdinal == null) {
				continue;
			}
			BigDecimal baseAmount = row.baseAmount();
			validRows.add(new ValidAmountRow(categoryOrdinal, localAmount, baseAmount));
			localAmountsByCategory
					.computeIfAbsent(categoryOrdinal, unused -> new ArrayList<>())
					.add(localAmount);
			if (baseAmount != null && baseAmount.compareTo(BigDecimal.ZERO) > 0) {
				baseAmountsByCategory
						.computeIfAbsent(categoryOrdinal, unused -> new ArrayList<>())
						.add(baseAmount);
			}
		}

		Map<Integer, Bounds> localBoundsByCategory =
				computeBoundsForCategories(localAmountsByCategory);
		Map<Integer, Bounds> baseBoundsByCategory =
				computeBoundsForCategories(baseAmountsByCategory);

		BigDecimal totalLocalAmount = BigDecimal.ZERO;
		BigDecimal totalBaseAmount = BigDecimal.ZERO;
		long expenseCount = 0L;
		Map<Integer, CategoryAccumulator> categoryMap = new HashMap<>();
		for (ValidAmountRow row : validRows) {
			BigDecimal cleanedLocalAmount =
					applyBounds(
							localBoundsByCategory.get(row.categoryOrdinal()), row.localAmount());
			BigDecimal cleanedBaseAmount =
					row.baseAmount() == null ? BigDecimal.ZERO : row.baseAmount();
			if (cleanedBaseAmount.compareTo(BigDecimal.ZERO) > 0) {
				cleanedBaseAmount =
						applyBounds(
								baseBoundsByCategory.get(row.categoryOrdinal()), cleanedBaseAmount);
			}
			BigDecimal boundedBaseAmount = cleanedBaseAmount;

			totalLocalAmount = totalLocalAmount.add(cleanedLocalAmount);
			totalBaseAmount = totalBaseAmount.add(boundedBaseAmount);
			expenseCount += 1L;
			categoryMap.compute(
					row.categoryOrdinal(),
					(unused, current) ->
							(current == null ? new CategoryAccumulator() : current)
									.add(cleanedLocalAmount, boundedBaseAmount, 1L));
		}

		List<CategoryAmountPairCount> categoryRows = new ArrayList<>(categoryMap.size());
		for (Map.Entry<Integer, CategoryAccumulator> entry : categoryMap.entrySet()) {
			categoryRows.add(
					new CategoryAmountPairCount(
							entry.getKey(),
							entry.getValue().localAmount(),
							entry.getValue().baseAmount(),
							entry.getValue().count()));
		}
		return new CleanedMonthlyAggregation(
				totalLocalAmount, totalBaseAmount, expenseCount, categoryRows);
	}

	private Bounds computeBounds(List<BigDecimal> sorted) {
		if (sorted.isEmpty()) {
			return Bounds.notApplicable();
		}
		if (sorted.size() < properties.getOutlierMinSampleSize()) {
			return Bounds.notApplicable();
		}
		BigDecimal q1 = QuantileUtil.linearInterpolatedQuantile(sorted, 0.25d, MC);
		BigDecimal q3 = QuantileUtil.linearInterpolatedQuantile(sorted, 0.75d, MC);
		BigDecimal iqr = q3.subtract(q1, MC);
		BigDecimal delta =
				iqr.multiply(BigDecimal.valueOf(properties.getOutlierIqrMultiplier()), MC);
		BigDecimal lower = q1.subtract(delta, MC);
		BigDecimal upper = q3.add(delta, MC);

		lower = lower.max(properties.getCleanedMinAmount());
		upper = upper.min(properties.getCleanedMaxAmount());
		if (lower.compareTo(upper) > 0) {
			return Bounds.notApplicable();
		}
		return Bounds.applicable(lower, upper);
	}

	private Map<Integer, Bounds> computeBoundsForCategories(
			Map<Integer, List<BigDecimal>> amountsByCategory) {
		Map<Integer, Bounds> boundsByCategory = new HashMap<>(amountsByCategory.size());
		for (Map.Entry<Integer, List<BigDecimal>> entry : amountsByCategory.entrySet()) {
			List<BigDecimal> sorted = new ArrayList<>(entry.getValue());
			sorted.sort(Comparator.naturalOrder());
			boundsByCategory.put(entry.getKey(), computeBounds(sorted));
		}
		return boundsByCategory;
	}

	private BigDecimal applyBounds(Bounds bounds, BigDecimal amount) {
		if (amount == null) {
			return BigDecimal.ZERO;
		}
		if (bounds == null || !bounds.applicable()) {
			return amount;
		}
		if (amount.compareTo(bounds.lower()) < 0) {
			return bounds.lower();
		}
		if (amount.compareTo(bounds.upper()) > 0) {
			return bounds.upper();
		}
		return amount;
	}

	private void upsertMonthlyMetrics(
			Long accountBookId,
			CountryCode countryCode,
			LocalDate monthStart,
			AmountPairCount amountCount,
			AnalysisQualityType qualityType) {
		upsertMonthlyMetric(
				accountBookId,
				countryCode,
				monthStart,
				AnalysisMetricType.TOTAL_LOCAL_AMOUNT,
				qualityType,
				amountCount.totalLocalAmount());
		upsertMonthlyMetric(
				accountBookId,
				countryCode,
				monthStart,
				AnalysisMetricType.TOTAL_BASE_AMOUNT,
				qualityType,
				amountCount.totalBaseAmount());
		upsertMonthlyMetric(
				accountBookId,
				countryCode,
				monthStart,
				AnalysisMetricType.EXPENSE_COUNT,
				qualityType,
				BigDecimal.valueOf(amountCount.expenseCount()));
	}

	private void upsertMonthlyMetric(
			Long accountBookId,
			CountryCode countryCode,
			LocalDate monthStart,
			AnalysisMetricType metricType,
			AnalysisQualityType qualityType,
			BigDecimal value) {
		accountMonthlyAggregateRepository
				.findByAccountBookIdAndTargetYearMonthAndMetricTypeAndQualityType(
						accountBookId, monthStart, metricType, qualityType)
				.ifPresentOrElse(
						existing -> existing.updateMetricValue(value),
						() ->
								accountMonthlyAggregateRepository.save(
										AccountMonthlyAggregateEntity.of(
												accountBookId,
												countryCode,
												monthStart,
												metricType,
												qualityType,
												value)));
	}

	private void upsertMonthlyCategoryMetrics(
			Long accountBookId,
			CountryCode countryCode,
			LocalDate monthStart,
			List<CategoryAmountPairCount> rows,
			AnalysisQualityType qualityType) {
		accountMonthlyCategoryAggregateRepository
				.deleteByAccountBookIdAndTargetYearMonthAndQualityType(
						accountBookId, monthStart, qualityType);
		if (rows.isEmpty()) {
			return;
		}

		List<AccountMonthlyCategoryAggregateEntity> toSave = new ArrayList<>(rows.size() * 2);
		for (CategoryAmountPairCount row : rows) {
			Category category = toCategory(row.categoryOrdinal());
			toSave.add(
					AccountMonthlyCategoryAggregateEntity.of(
							accountBookId,
							countryCode,
							monthStart,
							category,
							qualityType,
							CurrencyType.LOCAL,
							row.totalLocalAmount(),
							row.expenseCount()));
			toSave.add(
					AccountMonthlyCategoryAggregateEntity.of(
							accountBookId,
							countryCode,
							monthStart,
							category,
							qualityType,
							CurrencyType.BASE,
							row.totalBaseAmount(),
							row.expenseCount()));
		}
		accountMonthlyCategoryAggregateRepository.saveAll(toSave);
	}

	private void refreshPairMonthlyAggregates(PairMonthKey pairMonthKey) {
		if (pairMonthKey.baseCountryCode() == null) {
			return;
		}
		refreshPairMonthlyAggregatesByCurrency(
				pairMonthKey, CurrencyType.LOCAL, AnalysisMetricType.TOTAL_LOCAL_AMOUNT);
		refreshPairMonthlyAggregatesByCurrency(
				pairMonthKey, CurrencyType.BASE, AnalysisMetricType.TOTAL_BASE_AMOUNT);
	}

	private void refreshPairMonthlyAggregatesByCurrency(
			PairMonthKey pairMonthKey, CurrencyType currencyType, AnalysisMetricType metricType) {
		List<AccountAmountCount> monthlyRows =
				aggregationRepository.aggregatePairMonthlyTotalByAccountFromMonthly(
						pairMonthKey.localCountryCode(),
						pairMonthKey.baseCountryCode(),
						pairMonthKey.targetYearMonth(),
						metricType,
						AnalysisQualityType.CLEANED);

		pairMonthlyCategoryAggregateRepository
				.deleteByLocalCountryCodeAndBaseCountryCodeAndTargetYearMonthAndQualityTypeAndCurrencyType(
						pairMonthKey.localCountryCode(),
						pairMonthKey.baseCountryCode(),
						pairMonthKey.targetYearMonth(),
						AnalysisQualityType.CLEANED,
						currencyType);

		if (monthlyRows.isEmpty()) {
			pairMonthlyAggregateRepository
					.findByLocalCountryCodeAndBaseCountryCodeAndTargetYearMonthAndQualityTypeAndMetricType(
							pairMonthKey.localCountryCode(),
							pairMonthKey.baseCountryCode(),
							pairMonthKey.targetYearMonth(),
							AnalysisQualityType.CLEANED,
							metricType)
					.ifPresent(pairMonthlyAggregateRepository::delete);
			return;
		}

		PairIqrBounds iqrBounds = computePairIqrBounds(monthlyRows);
		List<AccountAmountCount> filteredRows = filterByIqr(monthlyRows, iqrBounds);
		List<AccountAmountCount> effectiveRows =
				filteredRows.isEmpty() ? monthlyRows : filteredRows;
		Set<Long> includedAccountIds =
				effectiveRows.stream()
						.map(AccountAmountCount::accountBookId)
						.collect(java.util.stream.Collectors.toSet());

		BigDecimal totalMetricSum =
				effectiveRows.stream()
						.map(AccountAmountCount::totalAmount)
						.reduce(BigDecimal.ZERO, BigDecimal::add);
		long includedAccountCount = effectiveRows.size();
		BigDecimal averageMetricValue = divideScale(totalMetricSum, includedAccountCount, 4);

		pairMonthlyAggregateRepository
				.findByLocalCountryCodeAndBaseCountryCodeAndTargetYearMonthAndQualityTypeAndMetricType(
						pairMonthKey.localCountryCode(),
						pairMonthKey.baseCountryCode(),
						pairMonthKey.targetYearMonth(),
						AnalysisQualityType.CLEANED,
						metricType)
				.ifPresentOrElse(
						existing ->
								existing.update(
										includedAccountCount,
										totalMetricSum,
										averageMetricValue,
										iqrBounds == null ? null : iqrBounds.lower(),
										iqrBounds == null ? null : iqrBounds.upper()),
						() ->
								pairMonthlyAggregateRepository.save(
										com.genesis.unipocket.analysis.command.persistence.entity
												.PairMonthlyAggregateEntity.of(
												pairMonthKey.localCountryCode(),
												pairMonthKey.baseCountryCode(),
												pairMonthKey.targetYearMonth(),
												AnalysisQualityType.CLEANED,
												metricType,
												includedAccountCount,
												totalMetricSum,
												averageMetricValue,
												iqrBounds == null ? null : iqrBounds.lower(),
												iqrBounds == null ? null : iqrBounds.upper())));

		List<AccountCategoryAmountCount> categoryRows =
				aggregationRepository.aggregatePairMonthlyCategoryByAccountFromMonthly(
						pairMonthKey.localCountryCode(),
						pairMonthKey.baseCountryCode(),
						pairMonthKey.targetYearMonth(),
						AnalysisQualityType.CLEANED,
						currencyType);
		Map<Integer, BigDecimal> categoryTotalMap = new HashMap<>();
		for (AccountCategoryAmountCount row : categoryRows) {
			if (!includedAccountIds.contains(row.accountBookId())) {
				continue;
			}
			if (row.categoryOrdinal() == null) {
				continue;
			}
			categoryTotalMap.merge(row.categoryOrdinal(), row.totalAmount(), BigDecimal::add);
		}

		List<
						com.genesis.unipocket.analysis.command.persistence.entity
								.PairMonthlyCategoryAggregateEntity>
				toSave = new ArrayList<>();
		for (Category category : Category.values()) {
			if (category == Category.INCOME) {
				continue;
			}
			BigDecimal totalAmount =
					categoryTotalMap.getOrDefault(category.ordinal(), BigDecimal.ZERO);
			BigDecimal averageAmount = divideScale(totalAmount, includedAccountCount, 4);
			toSave.add(
					com.genesis.unipocket.analysis.command.persistence.entity
							.PairMonthlyCategoryAggregateEntity.of(
							pairMonthKey.localCountryCode(),
							pairMonthKey.baseCountryCode(),
							pairMonthKey.targetYearMonth(),
							AnalysisQualityType.CLEANED,
							currencyType,
							category,
							includedAccountCount,
							totalAmount,
							averageAmount));
		}
		if (!toSave.isEmpty()) {
			pairMonthlyCategoryAggregateRepository.saveAll(toSave);
		}
	}

	private PairIqrBounds computePairIqrBounds(List<AccountAmountCount> monthlyRows) {
		if (monthlyRows == null || monthlyRows.size() < 4) {
			return null;
		}
		List<BigDecimal> sorted =
				monthlyRows.stream()
						.map(AccountAmountCount::totalAmount)
						.sorted(Comparator.naturalOrder())
						.toList();
		BigDecimal q1 = QuantileUtil.linearInterpolatedQuantile(sorted, 0.25d, MC);
		BigDecimal q3 = QuantileUtil.linearInterpolatedQuantile(sorted, 0.75d, MC);
		BigDecimal iqr = q3.subtract(q1, MC);
		if (iqr.compareTo(BigDecimal.ZERO) < 0) {
			return null;
		}
		BigDecimal delta =
				iqr.multiply(BigDecimal.valueOf(properties.getOutlierIqrMultiplier()), MC);
		return new PairIqrBounds(q1.subtract(delta, MC), q3.add(delta, MC));
	}

	private List<AccountAmountCount> filterByIqr(
			List<AccountAmountCount> monthlyRows, PairIqrBounds iqrBounds) {
		if (iqrBounds == null) {
			return monthlyRows;
		}
		return monthlyRows.stream()
				.filter(
						row ->
								row.totalAmount().compareTo(iqrBounds.lower()) >= 0
										&& row.totalAmount().compareTo(iqrBounds.upper()) <= 0)
				.toList();
	}

	private BigDecimal divideScale(BigDecimal value, long divisor, int scale) {
		if (divisor <= 0L) {
			return BigDecimal.ZERO;
		}
		return value.divide(BigDecimal.valueOf(divisor), scale, RoundingMode.HALF_UP);
	}

	private void markFailure(Long dirtyId, Exception exception) {
		monthlyDirtyRepository
				.findById(dirtyId)
				.ifPresent(
						dirty -> {
							if (dirty.getStatus() != AnalysisBatchJobStatus.RUNNING) {
								return;
							}
							LocalDateTime nowUtc = LocalDateTime.now(ZoneOffset.UTC);
							int currentAttempt = dirty.getAttemptCount();
							if (currentAttempt >= properties.getMaxRetry()) {
								dirty.markDead(
										nowUtc,
										exception.getClass().getSimpleName(),
										exception.getMessage());
								return;
							}
							long multiplier = 1L << Math.max(0, currentAttempt - 1);
							long delayMinutes =
									Math.max(1L, properties.getRetryBaseMinutes() * multiplier);
							dirty.markRetry(
									nowUtc.plusMinutes(delayMinutes),
									exception.getClass().getSimpleName(),
									exception.getMessage());
						});
	}

	private Integer parseCategoryOrdinal(Object value) {
		return CategoryOrdinalParser.parse(value);
	}

	private Category toCategory(Integer ordinal) {
		if (ordinal == null || ordinal < 0 || ordinal >= Category.values().length) {
			throw new IllegalArgumentException("Invalid category ordinal: " + ordinal);
		}
		return Category.values()[ordinal];
	}

	private LocalDateTime toUtc(LocalDate localDate, ZoneId zoneId) {
		return localDate.atStartOfDay(zoneId).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
	}

	private record ValidAmountRow(
			Integer categoryOrdinal, BigDecimal localAmount, BigDecimal baseAmount) {}

	private record Bounds(boolean applicable, BigDecimal lower, BigDecimal upper) {
		static Bounds applicable(BigDecimal lower, BigDecimal upper) {
			return new Bounds(true, lower, upper);
		}

		static Bounds notApplicable() {
			return new Bounds(false, null, null);
		}
	}

	private record CategoryAccumulator(BigDecimal localAmount, BigDecimal baseAmount, long count) {
		CategoryAccumulator() {
			this(BigDecimal.ZERO, BigDecimal.ZERO, 0L);
		}

		CategoryAccumulator add(
				BigDecimal deltaLocalAmount, BigDecimal deltaBaseAmount, long deltaCount) {
			return new CategoryAccumulator(
					localAmount.add(deltaLocalAmount),
					baseAmount.add(deltaBaseAmount),
					count + deltaCount);
		}
	}

	private record CleanedMonthlyAggregation(
			BigDecimal totalLocalAmount,
			BigDecimal totalBaseAmount,
			long expenseCount,
			List<CategoryAmountPairCount> categoryAmountCounts) {}

	private record PairMonthKey(
			CountryCode localCountryCode, CountryCode baseCountryCode, LocalDate targetYearMonth) {}

	private record PairIqrBounds(BigDecimal lower, BigDecimal upper) {}
}
