package com.genesis.unipocket.analysis.command.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.genesis.unipocket.analysis.command.config.AnalysisBatchProperties;
import com.genesis.unipocket.analysis.command.persistence.entity.CountryBatchScheduleEntity;
import com.genesis.unipocket.analysis.command.persistence.repository.CountryBatchScheduleRepository;
import com.genesis.unipocket.global.common.enums.CountryCode;
import com.genesis.unipocket.global.util.CountryCodeTimezoneMapper;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CountryBatchScheduleProcessorTest {

	@Mock private CountryBatchScheduleRepository scheduleRepository;
	@Mock private CountryMonthlyDirtyAggregationService monthlyDirtyAggregationService;

	private AnalysisBatchProperties properties;
	private CountryBatchScheduleProcessor processor;

	@BeforeEach
	void setUp() {
		properties = new AnalysisBatchProperties();
		properties.setRunHour(3);
		properties.setRunMinute(0);
		processor =
				new CountryBatchScheduleProcessor(
						scheduleRepository, monthlyDirtyAggregationService, properties);
	}

	@Test
	void processDueCountries_missingSchedules_initializesAllCountrySchedules() {
		when(scheduleRepository.findAll()).thenReturn(List.of());
		when(scheduleRepository.findDueSchedules(any(LocalDateTime.class))).thenReturn(List.of());

		processor.processDueCountries();

		verify(scheduleRepository, times(CountryCode.values().length)).save(any());
	}

	@Test
	void processDueCountries_dueScheduleExists_runsAggregationAndMovesNextRun() {
		List<CountryBatchScheduleEntity> existingSchedules = buildSchedulesForAllCountries();
		CountryBatchScheduleEntity dueSchedule = existingSchedules.get(0);
		LocalDateTime before = dueSchedule.getNextRunAtUtc();

		when(scheduleRepository.findAll()).thenReturn(existingSchedules);
		when(scheduleRepository.findDueSchedules(any(LocalDateTime.class)))
				.thenReturn(List.of(dueSchedule));

		processor.processDueCountries();

		verify(monthlyDirtyAggregationService, times(1))
				.processCountryDirtyRows(eq(dueSchedule.getCountryCode()));
		assertThat(dueSchedule.getLastSuccessLocalDate()).isNotNull();
		assertThat(dueSchedule.getLastSuccessAtUtc()).isNotNull();
		assertThat(dueSchedule.getNextRunAtUtc()).isAfter(before);
	}

	@Test
	void processDueCountries_aggregationThrows_keepsScheduleUnmodified() {
		List<CountryBatchScheduleEntity> existingSchedules = buildSchedulesForAllCountries();
		CountryBatchScheduleEntity dueSchedule = existingSchedules.get(0);
		LocalDateTime before = dueSchedule.getNextRunAtUtc();

		doThrow(new RuntimeException("boom"))
				.when(monthlyDirtyAggregationService)
				.processCountryDirtyRows(dueSchedule.getCountryCode());
		when(scheduleRepository.findAll()).thenReturn(existingSchedules);
		when(scheduleRepository.findDueSchedules(any(LocalDateTime.class)))
				.thenReturn(List.of(dueSchedule));

		processor.processDueCountries();

		assertThat(dueSchedule.getLastSuccessLocalDate()).isNull();
		assertThat(dueSchedule.getLastSuccessAtUtc()).isNull();
		assertThat(dueSchedule.getNextRunAtUtc()).isEqualTo(before);
	}

	private List<CountryBatchScheduleEntity> buildSchedulesForAllCountries() {
		return Arrays.stream(CountryCode.values())
				.map(
						countryCode ->
								CountryBatchScheduleEntity.create(
										countryCode,
										CountryCodeTimezoneMapper.getZoneId(countryCode),
										properties.getRunHour(),
										properties.getRunMinute(),
										LocalDateTime.of(2026, 1, 1, 0, 0)))
				.toList();
	}
}
