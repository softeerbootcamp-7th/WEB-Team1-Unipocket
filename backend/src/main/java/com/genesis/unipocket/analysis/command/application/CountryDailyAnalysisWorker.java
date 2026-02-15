package com.genesis.unipocket.analysis.command.application;

import com.genesis.unipocket.analysis.command.config.AnalysisBatchProperties;
import com.genesis.unipocket.analysis.command.persistence.entity.AccountDailyAggregateEntity;
import com.genesis.unipocket.analysis.command.persistence.entity.AccountDailyCategoryAggregateEntity;
import com.genesis.unipocket.analysis.command.persistence.entity.AnalysisBatchJobStatus;
import com.genesis.unipocket.analysis.command.persistence.entity.AnalysisMetricType;
import com.genesis.unipocket.analysis.command.persistence.entity.AnalysisQualityType;
import com.genesis.unipocket.analysis.command.persistence.entity.CountryBatchScheduleEntity;
import com.genesis.unipocket.analysis.command.persistence.entity.CountryDailyAnalysisJobEntity;
import com.genesis.unipocket.analysis.command.persistence.entity.CountryDailyAnalysisResultEntity;
import com.genesis.unipocket.analysis.command.persistence.entity.CountryDailyCategoryAggregateEntity;
import com.genesis.unipocket.analysis.command.persistence.repository.AccountDailyAggregateRepository;
import com.genesis.unipocket.analysis.command.persistence.repository.AccountDailyCategoryAggregateRepository;
import com.genesis.unipocket.analysis.command.persistence.repository.AnalysisOutlierAuditRepository;
import com.genesis.unipocket.analysis.command.persistence.repository.CountryBatchScheduleRepository;
import com.genesis.unipocket.analysis.command.persistence.repository.CountryDailyAnalysisJobRepository;
import com.genesis.unipocket.analysis.command.persistence.repository.CountryDailyAnalysisResultRepository;
import com.genesis.unipocket.analysis.command.persistence.repository.CountryDailyCategoryAggregateRepository;
import com.genesis.unipocket.analysis.command.persistence.repository.support.AnalysisBatchAggregationRepository;
import com.genesis.unipocket.analysis.command.persistence.repository.support.AnalysisBatchAggregationRepository.AccountAmountCount;
import com.genesis.unipocket.analysis.command.persistence.repository.support.AnalysisBatchAggregationRepository.AccountCategoryAmountCount;
import com.genesis.unipocket.analysis.command.persistence.repository.support.AnalysisBatchAggregationRepository.AmountCount;
import com.genesis.unipocket.analysis.command.persistence.repository.support.AnalysisBatchAggregationRepository.CategoryAmountCount;
import com.genesis.unipocket.global.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CountryCode;
import com.genesis.unipocket.global.util.CountryCodeTimezoneMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class CountryDailyAnalysisWorker {

	private final CountryDailyAnalysisJobRepository jobRepository;
	private final CountryBatchScheduleRepository scheduleRepository;
	private final CountryDailyAnalysisResultRepository resultRepository;
	private final AccountDailyAggregateRepository accountAggregateRepository;
	private final CountryDailyCategoryAggregateRepository countryCategoryAggregateRepository;
	private final AccountDailyCategoryAggregateRepository accountCategoryAggregateRepository;
	private final AnalysisOutlierAuditRepository outlierAuditRepository;
	private final AnalysisBatchAggregationRepository aggregationRepository;
	private final CountryDailyOutlierAggregationService outlierAggregationService;
	private final AnalysisBatchProperties properties;
	private final PlatformTransactionManager transactionManager;

	public void processClaimedJob(Long jobId) {
		TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
		try {
			JobExecutionContext context =
					txTemplate.execute(status -> loadJobExecutionContext(jobId));
			if (context == null) {
				return;
			}

			for (LocalDate targetDate = context.rangeStartLocalDate();
					!targetDate.isAfter(context.rangeEndLocalDate());
					targetDate = targetDate.plusDays(1)) {
				LocalDate processingDate = targetDate;
				txTemplate.executeWithoutResult(
						status ->
								processDailyAggregation(
										context.countryCode(), context.zoneId(), processingDate));
			}

			txTemplate.executeWithoutResult(
					status -> markSuccess(jobId, context.countryCode(), context.runLocalDate()));
		} catch (Exception e) {
			log.error("Failed to process analysis job. jobId={}", jobId, e);
			txTemplate.executeWithoutResult(status -> markFailure(jobId, e));
		}
	}

	private JobExecutionContext loadJobExecutionContext(Long jobId) {
		CountryDailyAnalysisJobEntity job =
				jobRepository
						.findById(jobId)
						.orElseThrow(
								() ->
										new IllegalStateException(
												"Country analysis job not found: " + jobId));

		if (job.getStatus() != AnalysisBatchJobStatus.RUNNING) {
			log.debug(
					"Skip processing non-running job. jobId={}, status={}", jobId, job.getStatus());
			return null;
		}

		ZoneId zoneId = CountryCodeTimezoneMapper.getZoneId(job.getCountryCode());
		return new JobExecutionContext(
				job.getCountryCode(),
				job.getRunLocalDate(),
				job.getRangeStartLocalDate(),
				job.getRangeEndLocalDate(),
				zoneId);
	}

	private void processDailyAggregation(
			CountryCode countryCode, ZoneId zoneId, LocalDate targetDate) {
		LocalDateTime startUtc = toUtc(targetDate, zoneId);
		LocalDateTime endUtc = toUtc(targetDate.plusDays(1), zoneId);
		LocalDateTime referenceStartUtc =
				toUtc(targetDate.minusDays(properties.getOutlierReferenceDays()), zoneId);
		List<Long> eligibleAccountBookIds =
				aggregationRepository.findAccountBookIdsByLocalEqualsBaseCountry(countryCode);

		AmountCount countryRaw =
				aggregationRepository.aggregateComparableCountryDailyRaw(
						countryCode, startUtc, endUtc);
		upsertCountryMetrics(targetDate, countryCode, countryRaw, AnalysisQualityType.RAW);

		List<AccountAmountCount> accountRaw =
				aggregationRepository.aggregateAccountDailyRaw(countryCode, startUtc, endUtc);
		upsertAccountMetrics(targetDate, accountRaw, AnalysisQualityType.RAW);

		upsertCountryCategoryMetrics(
				targetDate,
				countryCode,
				aggregationRepository.aggregateComparableCountryDailyRawByCategory(
						countryCode, startUtc, endUtc),
				AnalysisQualityType.RAW);
		upsertAccountCategoryMetrics(
				targetDate,
				aggregationRepository.aggregateComparableAccountDailyRawByCategory(
						countryCode, startUtc, endUtc),
				AnalysisQualityType.RAW,
				eligibleAccountBookIds);

		CountryDailyOutlierAggregationService.CleanedAggregationResult cleaned =
				outlierAggregationService.calculate(
						countryCode,
						targetDate,
						startUtc,
						endUtc,
						aggregationRepository.findComparableExpenseRows(
								countryCode, startUtc, endUtc),
						aggregationRepository.findComparableExpenseRows(
								countryCode, referenceStartUtc, startUtc),
						eligibleAccountBookIds);

		upsertCountryMetrics(
				targetDate, countryCode, cleaned.countryAmountCount(), AnalysisQualityType.CLEANED);
		upsertAccountMetrics(
				targetDate, cleaned.accountAmountCounts(), AnalysisQualityType.CLEANED);
		upsertCountryCategoryMetrics(
				targetDate,
				countryCode,
				toCategoryAmountCounts(cleaned.countryCategoryAmountCounts()),
				AnalysisQualityType.CLEANED);
		upsertAccountCategoryMetrics(
				targetDate,
				toAccountCategoryAmountCounts(cleaned.accountCategoryAmountCounts()),
				AnalysisQualityType.CLEANED,
				eligibleAccountBookIds);

		outlierAuditRepository.deleteByCountryCodeAndOccurredDate(countryCode, targetDate);
		if (!cleaned.audits().isEmpty()) {
			outlierAuditRepository.saveAll(cleaned.audits());
		}
	}

	private void markSuccess(Long jobId, CountryCode countryCode, LocalDate runLocalDate) {
		CountryDailyAnalysisJobEntity job =
				jobRepository
						.findById(jobId)
						.orElseThrow(
								() ->
										new IllegalStateException(
												"Country analysis job not found while marking"
														+ " success: "
														+ jobId));

		if (job.getStatus() != AnalysisBatchJobStatus.RUNNING) {
			log.warn(
					"Skip marking success due to unexpected status. jobId={}, status={}",
					jobId,
					job.getStatus());
			return;
		}

		LocalDateTime nowUtc = LocalDateTime.now(ZoneOffset.UTC);
		job.markSuccess(nowUtc);
		CountryBatchScheduleEntity schedule =
				scheduleRepository
						.findById(countryCode)
						.orElseThrow(
								() ->
										new IllegalStateException(
												"Country schedule not found: " + countryCode));
		schedule.markSuccess(runLocalDate, nowUtc);
		schedule.moveNextRun(properties.getRunHour(), properties.getRunMinute());
	}

	private void upsertCountryMetrics(
			LocalDate targetDate,
			CountryCode countryCode,
			AmountCount amountCount,
			AnalysisQualityType qualityType) {
		upsertCountryMetric(
				countryCode,
				targetDate,
				AnalysisMetricType.TOTAL_LOCAL_AMOUNT,
				qualityType,
				amountCount.totalAmount());
		upsertCountryMetric(
				countryCode,
				targetDate,
				AnalysisMetricType.EXPENSE_COUNT,
				qualityType,
				BigDecimal.valueOf(amountCount.expenseCount()));
	}

	private void upsertCountryMetric(
			CountryCode countryCode,
			LocalDate targetDate,
			AnalysisMetricType metricType,
			AnalysisQualityType qualityType,
			BigDecimal value) {
		resultRepository
				.findByCountryCodeAndTargetLocalDateAndMetricTypeAndQualityType(
						countryCode, targetDate, metricType, qualityType)
				.ifPresentOrElse(
						existing -> existing.updateMetricValue(value),
						() ->
								resultRepository.save(
										CountryDailyAnalysisResultEntity.of(
												countryCode,
												targetDate,
												metricType,
												qualityType,
												value)));
	}

	private void upsertAccountMetrics(
			LocalDate targetDate,
			List<AccountAmountCount> accountRows,
			AnalysisQualityType qualityType) {
		if (accountRows.isEmpty()) {
			return;
		}

		upsertAccountMetricBatch(
				targetDate,
				accountRows,
				qualityType,
				AnalysisMetricType.TOTAL_LOCAL_AMOUNT,
				AccountAmountCount::totalAmount);
		upsertAccountMetricBatch(
				targetDate,
				accountRows,
				qualityType,
				AnalysisMetricType.EXPENSE_COUNT,
				row -> BigDecimal.valueOf(row.expenseCount()));
	}

	private void upsertAccountMetricBatch(
			LocalDate targetDate,
			List<AccountAmountCount> accountRows,
			AnalysisQualityType qualityType,
			AnalysisMetricType metricType,
			Function<AccountAmountCount, BigDecimal> valueExtractor) {
		List<Long> accountBookIds =
				accountRows.stream().map(AccountAmountCount::accountBookId).distinct().toList();

		Map<Long, AccountDailyAggregateEntity> existingMap =
				accountAggregateRepository
						.findAllByAccountBookIdInAndTargetLocalDateAndMetricTypeAndQualityType(
								accountBookIds, targetDate, metricType, qualityType)
						.stream()
						.collect(
								Collectors.toMap(
										AccountDailyAggregateEntity::getAccountBookId,
										Function.identity(),
										(left, right) -> left));

		List<AccountDailyAggregateEntity> toSave = new ArrayList<>(accountRows.size());
		for (AccountAmountCount row : accountRows) {
			Long accountBookId = row.accountBookId();
			BigDecimal value = valueExtractor.apply(row);

			AccountDailyAggregateEntity existing = existingMap.get(accountBookId);
			if (existing != null) {
				existing.updateMetricValue(value);
				toSave.add(existing);
			} else {
				toSave.add(
						AccountDailyAggregateEntity.of(
								accountBookId, targetDate, metricType, qualityType, value));
			}
		}

		if (!toSave.isEmpty()) {
			accountAggregateRepository.saveAll(toSave);
		}
	}

	private List<CategoryAmountCount> toCategoryAmountCounts(
			List<CountryDailyOutlierAggregationService.CategoryAmountCount> rows) {
		return rows.stream()
				.map(
						row ->
								new CategoryAmountCount(
										row.categoryOrdinal(),
										row.totalAmount(),
										row.expenseCount()))
				.toList();
	}

	private List<AccountCategoryAmountCount> toAccountCategoryAmountCounts(
			List<CountryDailyOutlierAggregationService.AccountCategoryAmountCount> rows) {
		return rows.stream()
				.map(
						row ->
								new AccountCategoryAmountCount(
										row.accountBookId(),
										row.categoryOrdinal(),
										row.totalAmount(),
										row.expenseCount()))
				.toList();
	}

	private void upsertCountryCategoryMetrics(
			LocalDate targetDate,
			CountryCode countryCode,
			List<CategoryAmountCount> rows,
			AnalysisQualityType qualityType) {
		countryCategoryAggregateRepository.deleteByCountryCodeAndTargetLocalDateAndQualityType(
				countryCode, targetDate, qualityType);
		if (rows.isEmpty()) {
			return;
		}
		List<CountryDailyCategoryAggregateEntity> toSave =
				rows.stream()
						.map(
								row ->
										CountryDailyCategoryAggregateEntity.of(
												countryCode,
												targetDate,
												toCategory(row.categoryOrdinal()),
												qualityType,
												row.totalAmount(),
												row.expenseCount()))
						.toList();
		countryCategoryAggregateRepository.saveAll(toSave);
	}

	private void upsertAccountCategoryMetrics(
			LocalDate targetDate,
			List<AccountCategoryAmountCount> rows,
			AnalysisQualityType qualityType,
			List<Long> scopedAccountBookIds) {
		if (!scopedAccountBookIds.isEmpty()) {
			accountCategoryAggregateRepository
					.deleteByAccountBookIdInAndTargetLocalDateAndQualityType(
							scopedAccountBookIds, targetDate, qualityType);
		}
		if (rows.isEmpty()) {
			return;
		}

		List<AccountDailyCategoryAggregateEntity> toSave =
				rows.stream()
						.map(
								row ->
										AccountDailyCategoryAggregateEntity.of(
												row.accountBookId(),
												targetDate,
												toCategory(row.categoryOrdinal()),
												qualityType,
												row.totalAmount(),
												row.expenseCount()))
						.toList();
		accountCategoryAggregateRepository.saveAll(toSave);
	}

	private Category toCategory(Integer categoryOrdinal) {
		if (categoryOrdinal == null
				|| categoryOrdinal < 0
				|| categoryOrdinal >= Category.values().length) {
			throw new IllegalArgumentException("Invalid category ordinal: " + categoryOrdinal);
		}
		return Category.values()[categoryOrdinal];
	}

	private void markFailure(Long jobId, Exception exception) {
		jobRepository
				.findById(jobId)
				.ifPresent(
						job -> {
							if (job.getStatus() != AnalysisBatchJobStatus.RUNNING) {
								return;
							}

							int currentAttempt = job.getAttemptCount();
							if (currentAttempt >= properties.getMaxRetry()) {
								job.markDead(
										LocalDateTime.now(ZoneOffset.UTC),
										exception.getClass().getSimpleName(),
										exception.getMessage());
								return;
							}

							long multiplier = 1L << Math.max(0, currentAttempt - 1);
							long delayMinutes =
									Math.max(1L, properties.getRetryBaseMinutes() * multiplier);
							job.markRetry(
									LocalDateTime.now(ZoneOffset.UTC).plusMinutes(delayMinutes),
									exception.getClass().getSimpleName(),
									exception.getMessage());
						});
	}

	private LocalDateTime toUtc(LocalDate localDate, ZoneId zoneId) {
		ZonedDateTime zoned = localDate.atStartOfDay(zoneId);
		return zoned.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
	}

	private record JobExecutionContext(
			CountryCode countryCode,
			LocalDate runLocalDate,
			LocalDate rangeStartLocalDate,
			LocalDate rangeEndLocalDate,
			ZoneId zoneId) {}
}
