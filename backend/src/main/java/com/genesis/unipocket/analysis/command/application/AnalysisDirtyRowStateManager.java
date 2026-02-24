package com.genesis.unipocket.analysis.command.application;

import com.genesis.unipocket.analysis.command.config.AnalysisBatchProperties;
import com.genesis.unipocket.analysis.command.persistence.repository.AnalysisMonthlyDirtyRepository;
import com.genesis.unipocket.analysis.common.enums.AnalysisBatchJobStatus;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AnalysisDirtyRowStateManager {

	private final AnalysisMonthlyDirtyRepository monthlyDirtyRepository;
	private final AnalysisBatchProperties properties;

	public void finalizeDirtyRun(long dirtyId, LocalDateTime claimTime) {
		int repending =
				monthlyDirtyRepository.markPendingIfNewEventDuringRun(
						dirtyId,
						AnalysisBatchJobStatus.RUNNING,
						AnalysisBatchJobStatus.PENDING,
						claimTime);
		if (repending > 0) {
			return;
		}
		monthlyDirtyRepository.markSuccessIfNoNewEventDuringRun(
				dirtyId,
				AnalysisBatchJobStatus.RUNNING,
				AnalysisBatchJobStatus.SUCCESS,
				claimTime,
				LocalDateTime.now(ZoneOffset.UTC));
	}

	public void markFailure(long dirtyId, Exception exception) {
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
}
