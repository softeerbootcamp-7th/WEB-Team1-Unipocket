package com.genesis.unipocket.analysis.command.application;

import com.genesis.unipocket.analysis.command.config.AnalysisBatchProperties;
import com.genesis.unipocket.analysis.command.persistence.entity.AccountDailyAggregateEntity;
import com.genesis.unipocket.analysis.command.persistence.entity.AnalysisBatchJobStatus;
import com.genesis.unipocket.analysis.command.persistence.entity.AnalysisMetricType;
import com.genesis.unipocket.analysis.command.persistence.entity.AnalysisQualityType;
import com.genesis.unipocket.analysis.command.persistence.entity.CountryBatchScheduleEntity;
import com.genesis.unipocket.analysis.command.persistence.entity.CountryDailyAnalysisJobEntity;
import com.genesis.unipocket.analysis.command.persistence.entity.CountryDailyAnalysisResultEntity;
import com.genesis.unipocket.analysis.command.persistence.repository.AccountDailyAggregateRepository;
import com.genesis.unipocket.analysis.command.persistence.repository.CountryBatchScheduleRepository;
import com.genesis.unipocket.analysis.command.persistence.repository.CountryDailyAnalysisJobRepository;
import com.genesis.unipocket.analysis.command.persistence.repository.CountryDailyAnalysisResultRepository;
import com.genesis.unipocket.analysis.command.persistence.repository.support.AnalysisBatchAggregationRepository;
import com.genesis.unipocket.analysis.command.persistence.repository.support.AnalysisBatchAggregationRepository.AccountAmountCount;
import com.genesis.unipocket.analysis.command.persistence.repository.support.AnalysisBatchAggregationRepository.AmountCount;
import com.genesis.unipocket.global.util.CountryCodeTimezoneMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
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
	private final AnalysisResultCacheService cacheService;
	private final AnalysisBatchAggregationRepository aggregationRepository;
	private final AnalysisBatchProperties properties;
	private final PlatformTransactionManager transactionManager;

	public void processClaimedJob(Long jobId) {
		TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
		try {
			txTemplate.executeWithoutResult(status -> executeJob(jobId));
		} catch (Exception e) {
			log.error("Failed to process analysis job. jobId={}", jobId, e);
			txTemplate.executeWithoutResult(status -> markFailure(jobId, e));
		}
	}

	private void executeJob(Long jobId) {
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
			return;
		}

		ZoneId zoneId = CountryCodeTimezoneMapper.getZoneId(job.getCountryCode());
		for (LocalDate targetDate = job.getRangeStartLocalDate();
				!targetDate.isAfter(job.getRangeEndLocalDate());
				targetDate = targetDate.plusDays(1)) {
			LocalDateTime startUtc = toUtc(targetDate, zoneId);
			LocalDateTime endUtc = toUtc(targetDate.plusDays(1), zoneId);

			AmountCount countryRaw =
					aggregationRepository.aggregateCountryDailyRaw(
							job.getCountryCode(), startUtc, endUtc);
			AmountCount countryCleaned =
					aggregationRepository.aggregateCountryDailyCleaned(
							job.getCountryCode(),
							startUtc,
							endUtc,
							properties.getCleanedMinAmount(),
							properties.getCleanedMaxAmount());

			upsertCountryMetrics(targetDate, job, countryRaw, AnalysisQualityType.RAW);
			upsertCountryMetrics(targetDate, job, countryCleaned, AnalysisQualityType.CLEANED);

			List<AccountAmountCount> accountRaw =
					aggregationRepository.aggregateAccountDailyRaw(
							job.getCountryCode(), startUtc, endUtc);
			List<AccountAmountCount> accountCleaned =
					aggregationRepository.aggregateAccountDailyCleaned(
							job.getCountryCode(),
							startUtc,
							endUtc,
							properties.getCleanedMinAmount(),
							properties.getCleanedMaxAmount());

			upsertAccountMetrics(targetDate, accountRaw, AnalysisQualityType.RAW);
			upsertAccountMetrics(targetDate, accountCleaned, AnalysisQualityType.CLEANED);
		}

		LocalDateTime nowUtc = LocalDateTime.now(ZoneOffset.UTC);
		job.markSuccess(nowUtc);
		CountryBatchScheduleEntity schedule =
				scheduleRepository
						.findById(job.getCountryCode())
						.orElseThrow(
								() ->
										new IllegalStateException(
												"Country schedule not found: "
														+ job.getCountryCode()));
		schedule.markSuccess(job.getRunLocalDate());
		schedule.moveNextRun(properties.getRunHour(), properties.getRunMinute(), nowUtc);
	}

	private void upsertCountryMetrics(
			LocalDate targetDate,
			CountryDailyAnalysisJobEntity job,
			AmountCount amountCount,
			AnalysisQualityType qualityType) {
		upsertCountryMetric(
				job,
				targetDate,
				AnalysisMetricType.TOTAL_LOCAL_AMOUNT,
				qualityType,
				amountCount.totalAmount());
		upsertCountryMetric(
				job,
				targetDate,
				AnalysisMetricType.EXPENSE_COUNT,
				qualityType,
				BigDecimal.valueOf(amountCount.expenseCount()));
	}

	private void upsertCountryMetric(
			CountryDailyAnalysisJobEntity job,
			LocalDate targetDate,
			AnalysisMetricType metricType,
			AnalysisQualityType qualityType,
			BigDecimal value) {
		resultRepository
				.findByCountryCodeAndTargetLocalDateAndMetricTypeAndQualityType(
						job.getCountryCode(), targetDate, metricType, qualityType)
				.ifPresentOrElse(
						existing -> existing.updateMetricValue(value),
						() ->
								resultRepository.save(
										CountryDailyAnalysisResultEntity.of(
												job.getCountryCode(),
												targetDate,
												metricType,
												qualityType,
												value)));
		cacheService.cacheCountryMetric(
				job.getCountryCode(), targetDate, metricType, qualityType, value);
	}

	private void upsertAccountMetrics(
			LocalDate targetDate,
			List<AccountAmountCount> accountRows,
			AnalysisQualityType qualityType) {
		for (AccountAmountCount row : accountRows) {
			upsertAccountMetric(
					row.accountBookId(),
					targetDate,
					AnalysisMetricType.TOTAL_LOCAL_AMOUNT,
					qualityType,
					row.totalAmount());
			upsertAccountMetric(
					row.accountBookId(),
					targetDate,
					AnalysisMetricType.EXPENSE_COUNT,
					qualityType,
					BigDecimal.valueOf(row.expenseCount()));
		}
	}

	private void upsertAccountMetric(
			Long accountBookId,
			LocalDate targetDate,
			AnalysisMetricType metricType,
			AnalysisQualityType qualityType,
			BigDecimal value) {
		accountAggregateRepository
				.findByAccountBookIdAndTargetLocalDateAndMetricTypeAndQualityType(
						accountBookId, targetDate, metricType, qualityType)
				.ifPresentOrElse(
						existing -> existing.updateMetricValue(value),
						() ->
								accountAggregateRepository.save(
										AccountDailyAggregateEntity.of(
												accountBookId,
												targetDate,
												metricType,
												qualityType,
												value)));
		cacheService.cacheAccountMetric(accountBookId, targetDate, metricType, qualityType, value);
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
}
