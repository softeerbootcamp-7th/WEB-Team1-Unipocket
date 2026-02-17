package com.genesis.unipocket.analysis.command.application;

import com.genesis.unipocket.analysis.command.config.AnalysisBatchProperties;
import com.genesis.unipocket.analysis.command.persistence.entity.CountryBatchScheduleEntity;
import com.genesis.unipocket.analysis.command.persistence.repository.CountryBatchScheduleRepository;
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
	private final CountryMonthlyDirtyAggregationService monthlyDirtyAggregationService;
	private final AnalysisBatchProperties properties;

	@Transactional
	public void processDueCountries() {
		LocalDateTime nowUtc = LocalDateTime.now(ZoneOffset.UTC);
		initializeSchedules(nowUtc);

		for (CountryBatchScheduleEntity schedule : scheduleRepository.findDueSchedules(nowUtc)) {
			ZoneId zoneId = ZoneId.of(schedule.getTimezone());
			LocalDate runLocalDate =
					schedule.getNextRunAtUtc()
							.atZone(ZoneOffset.UTC)
							.withZoneSameInstant(zoneId)
							.toLocalDate();
			try {
				monthlyDirtyAggregationService.processCountryDirtyRows(schedule.getCountryCode());
				LocalDateTime successAtUtc = LocalDateTime.now(ZoneOffset.UTC);
				schedule.markSuccess(runLocalDate, successAtUtc);
				schedule.moveNextRun(properties.getRunHour(), properties.getRunMinute());
			} catch (Exception e) {
				log.error(
						"Failed monthly dirty aggregation. country={}, runLocalDate={}",
						schedule.getCountryCode(),
						runLocalDate,
						e);
			}
		}
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
