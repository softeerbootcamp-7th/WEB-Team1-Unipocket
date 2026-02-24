package com.genesis.unipocket.analysis.command.application;

import com.genesis.unipocket.analysis.command.config.AnalysisBatchProperties;
import com.genesis.unipocket.analysis.command.persistence.repository.AnalysisMonthlyDirtyRepository;
import com.genesis.unipocket.analysis.common.enums.AnalysisBatchJobStatus;
import com.genesis.unipocket.global.common.enums.CountryCode;
import com.genesis.unipocket.global.util.CountryCodeTimezoneMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class CountryMonthlyDirtyAggregationService {

	private static final Collection<AnalysisBatchJobStatus> CLAIMABLE_STATUSES =
			List.of(AnalysisBatchJobStatus.PENDING, AnalysisBatchJobStatus.RETRY);

	private final AnalysisMonthlyDirtyRepository monthlyDirtyRepository;
	private final AnalysisBatchProperties properties;
	private final PlatformTransactionManager transactionManager;
	private final MonthlyDirtyRowProcessor dirtyRowProcessor;
	private final PairMonthlyAggregateRefresher pairMonthlyAggregateRefresher;
	private final AnalysisDirtyRowStateManager dirtyRowStateManager;

	public void processCountryDirtyRows(CountryCode countryCode) {
		ZoneId countryZone = CountryCodeTimezoneMapper.getZoneId(countryCode);
		LocalDateTime batchStartUtc =
				LocalDate.now(countryZone)
						.atTime(properties.getRunHour(), properties.getRunMinute())
						.atZone(countryZone)
						.withZoneSameInstant(ZoneOffset.UTC)
						.toLocalDateTime();
		TransactionTemplate txTemplate =
				new TransactionTemplate(Objects.requireNonNull(transactionManager));
		Set<PairMonthKey> affectedPairMonths = new LinkedHashSet<>();

		int maxLoops = properties.getMaxLoopsPerBatch();
		int loopCount = 0;
		while (loopCount++ < maxLoops) {
			LocalDateTime nowUtc = LocalDateTime.now(ZoneOffset.UTC);
			List<Long> candidateIds =
					monthlyDirtyRepository.findDispatchableIdsByCountryCode(
							countryCode,
							CLAIMABLE_STATUSES,
							nowUtc,
							batchStartUtc,
							PageRequest.of(0, properties.getDispatchBatchSize()));
			if (candidateIds.isEmpty()) {
				break;
			}

			for (Long dirtyId : candidateIds) {
				LocalDateTime claimTime = LocalDateTime.now(ZoneOffset.UTC);
				Boolean claimed =
						txTemplate.execute(
								status -> {
									int updated =
											monthlyDirtyRepository.claimDirty(
													dirtyId,
													AnalysisBatchJobStatus.RUNNING,
													CLAIMABLE_STATUSES,
													claimTime,
													claimTime.plusMinutes(
															properties.getLeaseMinutes()));
									return updated == 1;
								});

				if (!Boolean.TRUE.equals(claimed)) {
					continue;
				}

				try {
					PairMonthKey affectedKey =
							txTemplate.execute(
									status -> dirtyRowProcessor.process(dirtyId, claimTime));
					if (affectedKey != null) {
						affectedPairMonths.add(affectedKey);
					}
				} catch (Exception e) {
					log.error("Failed to process monthly dirty row. dirtyId={}", dirtyId, e);
					txTemplate.executeWithoutResult(
							status -> dirtyRowStateManager.markFailure(dirtyId, e));
				}
			}
		}

		for (PairMonthKey pairMonthKey : affectedPairMonths) {
			txTemplate.executeWithoutResult(
					status -> pairMonthlyAggregateRefresher.refresh(pairMonthKey));
		}
	}
}
