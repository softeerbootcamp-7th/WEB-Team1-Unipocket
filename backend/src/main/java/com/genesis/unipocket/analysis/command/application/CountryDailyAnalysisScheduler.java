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
	private final CountryDailyAnalysisDispatcher dispatcher;
	private final AnalysisBatchProperties properties;

	private final AtomicBoolean planning = new AtomicBoolean(false);
	private final AtomicBoolean dispatching = new AtomicBoolean(false);

	@Scheduled(fixedDelayString = "${analysis.batch.scheduler.fixed-delay-ms:300000}")
	public void scheduleJobs() {
		if (!planning.compareAndSet(false, true)) {
			return;
		}
		try {
			planner.createDueJobs();
		} catch (Exception e) {
			log.error("Failed to create due country analysis jobs", e);
		} finally {
			planning.set(false);
		}
	}

	@Scheduled(fixedDelayString = "${analysis.batch.dispatcher.fixed-delay-ms:10000}")
	public void dispatchJobs() {
		if (!properties.isEnabled()) {
			return;
		}
		if (!dispatching.compareAndSet(false, true)) {
			return;
		}
		try {
			dispatcher.dispatch();
		} catch (Exception e) {
			log.error("Failed to dispatch country analysis jobs", e);
		} finally {
			dispatching.set(false);
		}
	}
}
