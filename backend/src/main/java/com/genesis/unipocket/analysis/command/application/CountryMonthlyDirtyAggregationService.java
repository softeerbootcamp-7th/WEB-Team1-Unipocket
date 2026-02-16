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
import com.genesis.unipocket.analysis.command.persistence.repository.support.AnalysisBatchAggregationRepository;
import com.genesis.unipocket.analysis.command.persistence.repository.support.AnalysisBatchAggregationRepository.AmountPairCount;
import com.genesis.unipocket.analysis.command.persistence.repository.support.AnalysisBatchAggregationRepository.CategoryAmountPairCount;
import com.genesis.unipocket.analysis.command.persistence.repository.support.AnalysisBatchAggregationRepository.ExpenseRow;
import com.genesis.unipocket.analysis.common.enums.CurrencyType;
import com.genesis.unipocket.global.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CountryCode;
import com.genesis.unipocket.global.util.CountryCodeTimezoneMapper;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
				txTemplate.executeWithoutResult(status -> processOneDirtyRow(dirtyId));
			} catch (Exception e) {
				log.error("Failed to process monthly dirty row. dirtyId={}", dirtyId, e);
				txTemplate.executeWithoutResult(status -> markFailure(dirtyId, e));
			}
		}
	}

	private void processOneDirtyRow(Long dirtyId) {
		AnalysisMonthlyDirtyEntity dirty =
				monthlyDirtyRepository
						.findById(dirtyId)
						.orElseThrow(
								() ->
										new IllegalStateException(
												"Monthly dirty row not found: " + dirtyId));
		if (dirty.getStatus() != AnalysisBatchJobStatus.RUNNING) {
			return;
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
			return;
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

		Map<Integer, Bounds> localBoundsByCategory = new HashMap<>(localAmountsByCategory.size());
		for (Map.Entry<Integer, List<BigDecimal>> entry : localAmountsByCategory.entrySet()) {
			List<BigDecimal> sorted = new ArrayList<>(entry.getValue());
			Collections.sort(sorted);
			localBoundsByCategory.put(entry.getKey(), computeBounds(sorted));
		}
		Map<Integer, Bounds> baseBoundsByCategory = new HashMap<>(baseAmountsByCategory.size());
		for (Map.Entry<Integer, List<BigDecimal>> entry : baseAmountsByCategory.entrySet()) {
			List<BigDecimal> sorted = new ArrayList<>(entry.getValue());
			Collections.sort(sorted);
			baseBoundsByCategory.put(entry.getKey(), computeBounds(sorted));
		}

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

			totalLocalAmount = totalLocalAmount.add(cleanedLocalAmount);
			totalBaseAmount = totalBaseAmount.add(cleanedBaseAmount);
			expenseCount += 1L;
			CategoryAccumulator current =
					categoryMap.computeIfAbsent(
							row.categoryOrdinal(), unused -> new CategoryAccumulator());
			categoryMap.put(
					row.categoryOrdinal(), current.add(cleanedLocalAmount, cleanedBaseAmount, 1L));
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
		double lowerP = normalizeTailP(properties.getOutlierLowerTailP());
		double upperP = normalizeTailP(properties.getOutlierUpperTailP());
		BigDecimal lower = quantile(sorted, lowerP);
		BigDecimal upper = quantile(sorted, 1d - upperP);

		lower = lower.max(properties.getCleanedMinAmount());
		upper = upper.min(properties.getCleanedMaxAmount());
		if (lower.compareTo(upper) > 0) {
			return Bounds.notApplicable();
		}
		return Bounds.applicable(lower, upper);
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
		if (value == null) {
			return null;
		}
		int ordinal;
		if (value instanceof Number number) {
			ordinal = number.intValue();
		} else {
			try {
				ordinal = Integer.parseInt(value.toString());
			} catch (NumberFormatException e) {
				try {
					ordinal = Category.valueOf(value.toString()).ordinal();
				} catch (IllegalArgumentException ignored) {
					return null;
				}
			}
		}
		return ordinal < 0 || ordinal >= Category.values().length ? null : ordinal;
	}

	private Category toCategory(Integer ordinal) {
		if (ordinal == null || ordinal < 0 || ordinal >= Category.values().length) {
			throw new IllegalArgumentException("Invalid category ordinal: " + ordinal);
		}
		return Category.values()[ordinal];
	}

	private double normalizeTailP(double p) {
		if (Double.isNaN(p) || p < 0d) {
			return 0d;
		}
		return Math.min(p, 0.49d);
	}

	private BigDecimal quantile(List<BigDecimal> sorted, double p) {
		if (sorted.isEmpty()) {
			return BigDecimal.ZERO;
		}
		if (sorted.size() == 1) {
			return sorted.get(0);
		}
		double clampedP = Math.max(0d, Math.min(1d, p));
		double idx = clampedP * (sorted.size() - 1);
		int lowerIdx = (int) Math.floor(idx);
		int upperIdx = (int) Math.ceil(idx);
		BigDecimal lower = sorted.get(lowerIdx);
		BigDecimal upper = sorted.get(upperIdx);
		if (lowerIdx == upperIdx) {
			return lower;
		}
		BigDecimal ratio = BigDecimal.valueOf(idx - lowerIdx);
		return lower.add(upper.subtract(lower, MC).multiply(ratio, MC), MC);
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
}
