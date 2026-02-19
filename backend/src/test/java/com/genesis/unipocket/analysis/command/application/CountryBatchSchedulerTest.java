package com.genesis.unipocket.analysis.command.application;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.genesis.unipocket.analysis.command.config.AnalysisBatchProperties;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class CountryBatchSchedulerTest {

	private final CountryBatchScheduleProcessor planner =
			Mockito.mock(CountryBatchScheduleProcessor.class);
	private final AnalysisBatchProperties properties = new AnalysisBatchProperties();
	private final CountryBatchScheduler scheduler = new CountryBatchScheduler(planner, properties);

	@Test
	void scheduleJobs_batchDisabled_skipsPlannerExecution() {
		properties.setEnabled(false);

		scheduler.scheduleJobs();

		verifyNoInteractions(planner);
	}

	@Test
	void scheduleJobs_batchEnabled_executesPlannerOnce() {
		properties.setEnabled(true);

		scheduler.scheduleJobs();

		verify(planner, times(1)).processDueCountries();
	}

	@Test
	void scheduleJobs_plannerThrows_resetsPlanningFlagForNextRun() {
		properties.setEnabled(true);
		doThrow(new RuntimeException("boom")).doNothing().when(planner).processDueCountries();

		scheduler.scheduleJobs();
		scheduler.scheduleJobs();

		verify(planner, times(2)).processDueCountries();
	}
}
