package com.genesis.unipocket.analysis.command.application;

import com.genesis.unipocket.analysis.command.config.AnalysisBatchProperties;
import com.genesis.unipocket.analysis.command.persistence.entity.CountryBatchScheduleEntity;
import com.genesis.unipocket.analysis.command.persistence.entity.CountryDailyAnalysisJobEntity;
import com.genesis.unipocket.analysis.command.persistence.repository.CountryBatchScheduleRepository;
import com.genesis.unipocket.analysis.command.persistence.repository.CountryDailyAnalysisJobRepository;
import com.genesis.unipocket.analysis.command.persistence.repository.support.AnalysisBatchAggregationRepository;
import com.genesis.unipocket.global.common.enums.CountryCode;
import com.genesis.unipocket.global.util.CountryCodeTimezoneMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.EnumSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CountryDailyAnalysisPlanner {

	private final CountryBatchScheduleRepository scheduleRepository;
	private final CountryDailyAnalysisJobRepository jobRepository;
	private final AnalysisBatchAggregationRepository aggregationRepository;
	private final AnalysisBatchProperties properties;

	@Transactional
	public void createDueJobs() {
		LocalDateTime nowUtc = LocalDateTime.now(ZoneOffset.UTC);
		initializeSchedules(nowUtc);

		for (CountryBatchScheduleEntity schedule : scheduleRepository.findDueSchedules(nowUtc)) {
			ZoneId zoneId = ZoneId.of(schedule.getTimezone());
			LocalDate runLocalDate =
					schedule.getNextRunAtUtc()
							.atZone(ZoneOffset.UTC)
							.withZoneSameInstant(zoneId)
							.toLocalDate();

			LocalDate rangeEnd = runLocalDate.minusDays(1);
			LocalDate rangeStart = calculateRangeStart(schedule, zoneId, rangeEnd);
			if (rangeStart.isAfter(rangeEnd)) {
				log.debug(
						"Skip analysis job creation due to invalid range. country={}, runDate={},"
								+ " rangeStart={}, rangeEnd={}",
						schedule.getCountryCode(),
						runLocalDate,
						rangeStart,
						rangeEnd);
				continue;
			}

			jobRepository
					.findByCountryCodeAndRunLocalDate(schedule.getCountryCode(), runLocalDate)
					.orElseGet(
							() ->
									jobRepository.save(
											CountryDailyAnalysisJobEntity.create(
													schedule.getCountryCode(),
													runLocalDate,
													rangeStart,
													rangeEnd)));
		}
	}

	private LocalDate calculateRangeStart(
			CountryBatchScheduleEntity schedule, ZoneId zoneId, LocalDate rangeEnd) {
		LocalDate minAllowedStart =
				rangeEnd.minusMonths(properties.getLookbackMonths()).plusDays(1);

		LocalDate fallbackStart;
		if (schedule.getLastSuccessAtUtc() == null) {
			fallbackStart = minAllowedStart;
		} else {
			fallbackStart = rangeEnd;
		}

		LocalDate detectedStart =
				schedule.getLastSuccessAtUtc() == null
						? fallbackStart
						: aggregationRepository
								.findEarliestOccurredAtUtcUpdatedAfter(
										schedule.getCountryCode(), schedule.getLastSuccessAtUtc())
								.map(occurredAtUtc -> toLocalDateInZone(occurredAtUtc, zoneId))
								.orElse(fallbackStart);

		if (detectedStart.isBefore(minAllowedStart)) {
			return minAllowedStart;
		}
		if (detectedStart.isAfter(rangeEnd)) {
			return rangeEnd;
		}
		return detectedStart;
	}

	private LocalDate toLocalDateInZone(LocalDateTime utcDateTime, ZoneId zoneId) {
		return utcDateTime.atZone(ZoneOffset.UTC).withZoneSameInstant(zoneId).toLocalDate();
	}

	private void initializeSchedules(LocalDateTime nowUtc) {
		Set<CountryCode> existingCodes =
				scheduleRepository.findAll().stream()
						.map(CountryBatchScheduleEntity::getCountryCode)
						.collect(java.util.stream.Collectors.toSet());

		for (CountryCode countryCode : EnumSet.allOf(CountryCode.class)) {
			if (existingCodes.contains(countryCode)) {
				continue;
			}

			ZoneId zoneId = CountryCodeTimezoneMapper.getZoneId(countryCode);
			scheduleRepository.save(
					CountryBatchScheduleEntity.create(
							countryCode,
							zoneId,
							properties.getRunHour(),
							properties.getRunMinute(),
							nowUtc));
		}
	}
}
