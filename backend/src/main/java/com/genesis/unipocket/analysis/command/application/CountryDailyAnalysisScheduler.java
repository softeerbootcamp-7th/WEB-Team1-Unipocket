package com.genesis.unipocket.analysis.command.application;

import com.genesis.unipocket.analysis.command.config.AnalysisBatchProperties;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
		prefix = "analysis.batch",
		name = "enabled",
		havingValue = "true",
		matchIfMissing = true)
public class CountryDailyAnalysisScheduler {

	private final CountryDailyAnalysisPlanner planner;
	private final AnalysisBatchProperties properties;

	private final AtomicBoolean planning = new AtomicBoolean(false);

	@Scheduled(fixedDelayString = "${analysis.batch.scheduler.fixed-delay-ms:300000}")
	public void scheduleJobs() {
		if (!properties.isEnabled()) {
			return;
		}
		if (!planning.compareAndSet(false, true)) {
			return;
		}
		try {
			planner.processDueCountries();
		} catch (Exception e) {
			log.error("Failed to process due country schedules", e);
		} finally {
			planning.set(false);
		}
	}
}
