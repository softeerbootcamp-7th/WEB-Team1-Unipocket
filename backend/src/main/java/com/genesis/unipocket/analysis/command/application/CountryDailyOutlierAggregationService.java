package com.genesis.unipocket.analysis.command.application;

import com.genesis.unipocket.analysis.command.config.AnalysisBatchProperties;
import com.genesis.unipocket.analysis.command.persistence.entity.AnalysisOutlierAuditEntity;
import com.genesis.unipocket.analysis.command.persistence.repository.support.AnalysisBatchAggregationRepository.AccountAmountCount;
import com.genesis.unipocket.analysis.command.persistence.repository.support.AnalysisBatchAggregationRepository.AmountCount;
import com.genesis.unipocket.analysis.command.persistence.repository.support.AnalysisBatchAggregationRepository.ExpenseRow;
import com.genesis.unipocket.global.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CountryCode;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CountryDailyOutlierAggregationService {

	private static final MathContext MC = MathContext.DECIMAL64;
	private static final BigDecimal MAD_SCALE = new BigDecimal("1.4826");
	private static final String RULE_TYPE_HARD = "HARD";
	private static final String RULE_TYPE_SOFT = "SOFT";
	private static final String ACTION_DROP = "DROP";
	private static final String ACTION_WINSORIZE_LOW = "WINSORIZE_LOW";
	private static final String ACTION_WINSORIZE_HIGH = "WINSORIZE_HIGH";
	private static final String ACTION_FLAG_ONLY = "FLAG_ONLY";

	private final AnalysisBatchProperties properties;

	public CleanedAggregationResult calculate(
			CountryCode countryCode,
			LocalDate targetDate,
			LocalDateTime startUtc,
			LocalDateTime endUtc,
			List<ExpenseRow> targetExpenseRows,
			List<ExpenseRow> referenceExpenseRows,
			List<Long> eligibleAccountBookIds) {
		Set<Long> eligibleAccountSet = new HashSet<>(eligibleAccountBookIds);
		List<AnalysisOutlierAuditEntity> audits = new ArrayList<>();
		List<ValidExpenseRow> validTargetRows = new ArrayList<>(targetExpenseRows.size());
		Set<Long> targetSeenExpenseIds = new HashSet<>(targetExpenseRows.size());

		for (ExpenseRow row : targetExpenseRows) {
			HardValidation hardValidation =
					validateHard(row, countryCode, startUtc, endUtc, targetSeenExpenseIds, true);
			if (!hardValidation.valid()) {
				audits.add(
						audit(
								countryCode,
								targetDate,
								row.expenseId(),
								RULE_TYPE_HARD,
								hardValidation.ruleName(),
								row.localAmount(),
								null,
								null,
								ACTION_DROP));
				continue;
			}

			validTargetRows.add(
					new ValidExpenseRow(
							row.expenseId(),
							row.accountBookId(),
							hardValidation.categoryOrdinal(),
							row.localAmount(),
							eligibleAccountSet.contains(row.accountBookId())));
		}

		List<ValidExpenseRow> validReferenceRows = new ArrayList<>(referenceExpenseRows.size());
		Set<Long> referenceSeenExpenseIds = new HashSet<>(referenceExpenseRows.size());
		for (ExpenseRow row : referenceExpenseRows) {
			HardValidation hardValidation =
					validateHard(row, countryCode, null, null, referenceSeenExpenseIds, false);
			if (!hardValidation.valid()) {
				continue;
			}
			validReferenceRows.add(
					new ValidExpenseRow(
							row.expenseId(),
							row.accountBookId(),
							hardValidation.categoryOrdinal(),
							row.localAmount(),
							false));
		}

		Map<BucketKey, BucketThreshold> thresholds = buildThresholds(validReferenceRows);
		Map<Long, BigDecimal> accountAmountMap = new HashMap<>(eligibleAccountBookIds.size());
		Map<Long, Long> accountCountMap = new HashMap<>(eligibleAccountBookIds.size());
		for (Long accountBookId : eligibleAccountBookIds) {
			accountAmountMap.put(accountBookId, BigDecimal.ZERO);
			accountCountMap.put(accountBookId, 0L);
		}

		Map<Integer, Accumulator> countryCategoryMap = new HashMap<>();
		Map<Long, Map<Integer, Accumulator>> accountCategoryMap = new HashMap<>();

		BigDecimal countryAmount = BigDecimal.ZERO;
		long countryCount = 0L;
		for (ValidExpenseRow row : validTargetRows) {
			BucketThreshold threshold = thresholds.get(new BucketKey(row.categoryOrdinal()));
			BigDecimal cleanedAmount = row.amount();

			if (threshold != null && threshold.hasBounds()) {
				boolean belowLowerBound = row.amount().compareTo(threshold.lowerBound()) < 0;
				boolean aboveUpperBound = row.amount().compareTo(threshold.upperBound()) > 0;
				if (belowLowerBound || aboveUpperBound) {
					if (threshold.applicable()) {
						cleanedAmount =
								belowLowerBound ? threshold.lowerBound() : threshold.upperBound();
						audits.add(
								audit(
										countryCode,
										targetDate,
										row.expenseId(),
										RULE_TYPE_SOFT,
										threshold.ruleName(),
										row.amount(),
										threshold.lowerBound(),
										threshold.upperBound(),
										belowLowerBound
												? ACTION_WINSORIZE_LOW
												: ACTION_WINSORIZE_HIGH));
					} else if (threshold.flagOnly()) {
						audits.add(
								audit(
										countryCode,
										targetDate,
										row.expenseId(),
										RULE_TYPE_SOFT,
										threshold.ruleName(),
										row.amount(),
										threshold.lowerBound(),
										threshold.upperBound(),
										ACTION_FLAG_ONLY));
					}
				}
			}

			countryAmount = countryAmount.add(cleanedAmount);
			countryCount += 1;
			countryCategoryMap
					.computeIfAbsent(row.categoryOrdinal(), unused -> new Accumulator())
					.add(cleanedAmount, 1L);

			if (row.eligibleAccount()) {
				Long accountBookId = row.accountBookId();
				if (accountAmountMap.containsKey(accountBookId)) {
					accountAmountMap.put(
							accountBookId, accountAmountMap.get(accountBookId).add(cleanedAmount));
					accountCountMap.put(accountBookId, accountCountMap.get(accountBookId) + 1L);
					accountCategoryMap
							.computeIfAbsent(accountBookId, unused -> new HashMap<>())
							.computeIfAbsent(row.categoryOrdinal(), unused -> new Accumulator())
							.add(cleanedAmount, 1L);
				}
			}
		}

		List<AccountAmountCount> accountCleanedRows =
				new ArrayList<>(eligibleAccountBookIds.size());
		for (Long accountBookId : eligibleAccountBookIds) {
			accountCleanedRows.add(
					new AccountAmountCount(
							accountBookId,
							accountAmountMap.getOrDefault(accountBookId, BigDecimal.ZERO),
							accountCountMap.getOrDefault(accountBookId, 0L)));
		}

		List<CategoryAmountCount> countryCategoryRows = new ArrayList<>(countryCategoryMap.size());
		for (Map.Entry<Integer, Accumulator> entry : countryCategoryMap.entrySet()) {
			countryCategoryRows.add(
					new CategoryAmountCount(
							entry.getKey(), entry.getValue().amount, entry.getValue().count));
		}

		List<AccountCategoryAmountCount> accountCategoryRows = new ArrayList<>();
		for (Map.Entry<Long, Map<Integer, Accumulator>> accountEntry :
				accountCategoryMap.entrySet()) {
			Long accountBookId = accountEntry.getKey();
			for (Map.Entry<Integer, Accumulator> categoryEntry :
					accountEntry.getValue().entrySet()) {
				accountCategoryRows.add(
						new AccountCategoryAmountCount(
								accountBookId,
								categoryEntry.getKey(),
								categoryEntry.getValue().amount,
								categoryEntry.getValue().count));
			}
		}

		return new CleanedAggregationResult(
				new AmountCount(countryAmount, countryCount),
				accountCleanedRows,
				countryCategoryRows,
				accountCategoryRows,
				audits);
	}

	private HardValidation validateHard(
			ExpenseRow row,
			CountryCode countryCode,
			LocalDateTime startUtc,
			LocalDateTime endUtc,
			Set<Long> seenExpenseIds,
			boolean checkRange) {
		if (row.expenseId() == null) {
			return HardValidation.invalid("MISSING_EXPENSE_ID");
		}
		if (!seenExpenseIds.add(row.expenseId())) {
			return HardValidation.invalid("DUPLICATE_RECORD");
		}
		if (row.accountBookId() == null) {
			return HardValidation.invalid("MISSING_ACCOUNT_BOOK_ID");
		}
		if (row.localAmount() == null) {
			return HardValidation.invalid("AMOUNT_NULL");
		}
		if (row.localAmount().compareTo(BigDecimal.ZERO) <= 0) {
			return HardValidation.invalid("AMOUNT_NON_POSITIVE");
		}
		if (row.localAmount().compareTo(properties.getCleanedMaxAmount()) > 0) {
			return HardValidation.invalid("AMOUNT_EXCEEDS_DOMAIN_MAX");
		}
		if (row.localCurrencyCode() == null || row.localCurrencyCode().isBlank()) {
			return HardValidation.invalid("LOCAL_CURRENCY_MISSING");
		}
		if (row.localCountryCode() == null || row.localCountryCode().isBlank()) {
			return HardValidation.invalid("LOCAL_COUNTRY_MISSING");
		}
		CountryCode localCountryCode;
		try {
			localCountryCode = CountryCode.valueOf(row.localCountryCode());
		} catch (Exception e) {
			return HardValidation.invalid("LOCAL_COUNTRY_INVALID");
		}
		if (localCountryCode != countryCode) {
			return HardValidation.invalid("LOCAL_COUNTRY_MISMATCH");
		}
		String expectedCurrency = localCountryCode.getCurrencyCode().name();
		if (!expectedCurrency.equals(row.localCurrencyCode())) {
			return HardValidation.invalid("COUNTRY_CURRENCY_MISMATCH");
		}
		if (row.occurredAtUtc() == null) {
			return HardValidation.invalid("OCCURRED_AT_NULL");
		}
		if (checkRange
				&& (row.occurredAtUtc().isBefore(startUtc)
						|| !row.occurredAtUtc().isBefore(endUtc))) {
			return HardValidation.invalid("OCCURRED_AT_OUT_OF_RANGE");
		}
		Integer categoryOrdinal = parseCategoryOrdinal(row.categoryValue());
		if (categoryOrdinal == null) {
			return HardValidation.invalid("CATEGORY_MISSING_OR_INVALID");
		}
		return HardValidation.valid(categoryOrdinal);
	}

	private Map<BucketKey, BucketThreshold> buildThresholds(List<ValidExpenseRow> referenceRows) {
		Map<BucketKey, List<BigDecimal>> bucketAmountMap = new HashMap<>();
		for (ValidExpenseRow row : referenceRows) {
			BucketKey key = new BucketKey(row.categoryOrdinal());
			bucketAmountMap.computeIfAbsent(key, unused -> new ArrayList<>()).add(row.amount());
		}

		Map<BucketKey, BucketThreshold> thresholds = new HashMap<>(bucketAmountMap.size());
		for (Map.Entry<BucketKey, List<BigDecimal>> entry : bucketAmountMap.entrySet()) {
			List<BigDecimal> sorted = new ArrayList<>(entry.getValue());
			Collections.sort(sorted);
			thresholds.put(entry.getKey(), computeThreshold(sorted));
		}
		return thresholds;
	}

	private BucketThreshold computeThreshold(List<BigDecimal> sortedAmounts) {
		if (sortedAmounts.isEmpty()) {
			return BucketThreshold.notApplicable("NO_REFERENCE_SAMPLE");
		}

		ComputedBounds bounds = computeBounds(sortedAmounts);
		if (bounds == null) {
			return BucketThreshold.notApplicable("BOUNDARY_COLLISION");
		}
		if (sortedAmounts.size() < properties.getOutlierMinSampleSize()) {
			return BucketThreshold.flagOnly(
					bounds.ruleName() + "_N_LT_MIN_SAMPLE", bounds.lower(), bounds.upper());
		}
		return BucketThreshold.applicable(bounds.ruleName(), bounds.lower(), bounds.upper());
	}

	private ComputedBounds computeBounds(List<BigDecimal> sortedAmounts) {
		BigDecimal domainMin = properties.getCleanedMinAmount();
		BigDecimal domainMax = properties.getCleanedMaxAmount();
		double lowerTailP = normalizeTailP(properties.getOutlierLowerTailP());
		double upperTailP = normalizeTailP(properties.getOutlierUpperTailP());

		BigDecimal percentileLower = quantile(sortedAmounts, lowerTailP);
		BigDecimal percentileUpper = quantile(sortedAmounts, 1d - upperTailP);

		BigDecimal methodLower;
		BigDecimal methodUpper;
		String ruleName;
		if (properties.getOutlierMethod() == AnalysisBatchProperties.OutlierMethod.MAD) {
			ruleName = "MAD_WINSORIZE";
			BigDecimal median = quantile(sortedAmounts, 0.5d);
			List<BigDecimal> deviations = new ArrayList<>(sortedAmounts.size());
			for (BigDecimal amount : sortedAmounts) {
				deviations.add(amount.subtract(median).abs());
			}
			Collections.sort(deviations);
			BigDecimal mad = quantile(deviations, 0.5d);
			BigDecimal sigma = mad.multiply(MAD_SCALE, MC);
			BigDecimal zThreshold = BigDecimal.valueOf(properties.getOutlierMadZThreshold());
			BigDecimal delta = sigma.multiply(zThreshold, MC);
			methodLower = median.subtract(delta, MC);
			methodUpper = median.add(delta, MC);
		} else {
			ruleName = "IQR_WINSORIZE";
			BigDecimal q1 = quantile(sortedAmounts, 0.25d);
			BigDecimal q3 = quantile(sortedAmounts, 0.75d);
			BigDecimal iqr = q3.subtract(q1, MC);
			BigDecimal iqrMultiplier = BigDecimal.valueOf(properties.getOutlierIqrMultiplier());
			BigDecimal delta = iqr.multiply(iqrMultiplier, MC);
			methodLower = q1.subtract(delta, MC);
			methodUpper = q3.add(delta, MC);
		}

		BigDecimal lower = max(domainMin, percentileLower, methodLower);
		BigDecimal upper = min(domainMax, percentileUpper, methodUpper);
		if (lower.compareTo(upper) > 0) {
			return null;
		}
		return new ComputedBounds(ruleName, lower, upper);
	}

	private Integer parseCategoryOrdinal(Object rawCategory) {
		if (rawCategory == null) {
			return null;
		}
		int ordinal;
		if (rawCategory instanceof Number number) {
			ordinal = number.intValue();
		} else {
			try {
				ordinal = Integer.parseInt(rawCategory.toString());
			} catch (NumberFormatException e) {
				return null;
			}
		}
		if (ordinal < 0 || ordinal >= Category.values().length) {
			return null;
		}
		return ordinal;
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

	private BigDecimal max(BigDecimal a, BigDecimal b, BigDecimal c) {
		return a.max(b).max(c);
	}

	private BigDecimal min(BigDecimal a, BigDecimal b, BigDecimal c) {
		return a.min(b).min(c);
	}

	private AnalysisOutlierAuditEntity audit(
			CountryCode countryCode,
			LocalDate occurredDate,
			Long expenseId,
			String ruleType,
			String ruleName,
			BigDecimal rawValue,
			BigDecimal lowerBound,
			BigDecimal upperBound,
			String action) {
		return AnalysisOutlierAuditEntity.builder()
				.countryCode(countryCode)
				.occurredDate(occurredDate)
				.recordKey(expenseId == null ? "UNKNOWN" : String.valueOf(expenseId))
				.ruleType(ruleType)
				.ruleName(ruleName)
				.rawValue(rawValue)
				.lowerBound(lowerBound)
				.upperBound(upperBound)
				.action(action)
				.build();
	}

	public record CleanedAggregationResult(
			AmountCount countryAmountCount,
			List<AccountAmountCount> accountAmountCounts,
			List<CategoryAmountCount> countryCategoryAmountCounts,
			List<AccountCategoryAmountCount> accountCategoryAmountCounts,
			List<AnalysisOutlierAuditEntity> audits) {}

	public record CategoryAmountCount(
			Integer categoryOrdinal, BigDecimal totalAmount, long expenseCount) {}

	public record AccountCategoryAmountCount(
			Long accountBookId,
			Integer categoryOrdinal,
			BigDecimal totalAmount,
			long expenseCount) {}

	private record BucketKey(int categoryOrdinal) {}

	private record ValidExpenseRow(
			Long expenseId,
			Long accountBookId,
			int categoryOrdinal,
			BigDecimal amount,
			boolean eligibleAccount) {}

	private record HardValidation(boolean valid, Integer categoryOrdinal, String ruleName) {
		static HardValidation valid(int categoryOrdinal) {
			return new HardValidation(true, categoryOrdinal, null);
		}

		static HardValidation invalid(String ruleName) {
			return new HardValidation(false, null, ruleName);
		}
	}

	private record ComputedBounds(String ruleName, BigDecimal lower, BigDecimal upper) {}

	private record BucketThreshold(
			boolean applicable,
			boolean flagOnly,
			String ruleName,
			BigDecimal lowerBound,
			BigDecimal upperBound) {
		boolean hasBounds() {
			return lowerBound != null && upperBound != null;
		}

		static BucketThreshold applicable(
				String ruleName, BigDecimal lowerBound, BigDecimal upperBound) {
			return new BucketThreshold(true, false, ruleName, lowerBound, upperBound);
		}

		static BucketThreshold flagOnly(
				String ruleName, BigDecimal lowerBound, BigDecimal upperBound) {
			return new BucketThreshold(false, true, ruleName, lowerBound, upperBound);
		}

		static BucketThreshold notApplicable(String ruleName) {
			return new BucketThreshold(false, false, ruleName, null, null);
		}
	}

	private static class Accumulator {
		private BigDecimal amount = BigDecimal.ZERO;
		private long count = 0L;

		private void add(BigDecimal deltaAmount, long deltaCount) {
			amount = amount.add(deltaAmount);
			count += deltaCount;
		}
	}
}
