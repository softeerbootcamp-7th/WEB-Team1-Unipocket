package com.genesis.unipocket.analysis.command.application;

import com.genesis.unipocket.analysis.command.config.AnalysisBatchProperties;
import com.genesis.unipocket.analysis.command.persistence.entity.AnalysisBatchJobStatus;
import com.genesis.unipocket.analysis.command.persistence.repository.CountryDailyAnalysisJobRepository;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class CountryDailyAnalysisDispatcher {

	private static final List<AnalysisBatchJobStatus> CLAIMABLE_STATUSES =
			List.of(AnalysisBatchJobStatus.PENDING, AnalysisBatchJobStatus.RETRY);

	private final CountryDailyAnalysisJobRepository jobRepository;
	private final CountryDailyAnalysisWorker worker;
	private final AnalysisBatchProperties properties;
	private final PlatformTransactionManager transactionManager;

	@Qualifier("analysisBatchExecutor") private final Executor analysisBatchExecutor;

	private final String workerId = resolveWorkerId();

	public void dispatch() {
		LocalDateTime nowUtc = LocalDateTime.now(ZoneOffset.UTC);
		List<Long> candidateIds =
				jobRepository.findDispatchableJobIds(
						CLAIMABLE_STATUSES,
						nowUtc,
						PageRequest.of(0, properties.getDispatchBatchSize()));

		if (candidateIds.isEmpty()) {
			return;
		}

		TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
		for (Long jobId : candidateIds) {
			Boolean claimed =
					txTemplate.execute(
							status -> {
								LocalDateTime claimTime = LocalDateTime.now(ZoneOffset.UTC);
								int updated =
										jobRepository.claimJob(
												jobId,
												AnalysisBatchJobStatus.RUNNING,
												CLAIMABLE_STATUSES,
												workerId,
												claimTime,
												claimTime.plusMinutes(
														properties.getLeaseMinutes()));
								return updated == 1;
							});

			if (Boolean.TRUE.equals(claimed)) {
				analysisBatchExecutor.execute(() -> worker.processClaimedJob(jobId));
			}
		}
	}

	private String resolveWorkerId() {
		try {
			return InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			return "analysis-worker-" + UUID.randomUUID();
		}
	}
}
