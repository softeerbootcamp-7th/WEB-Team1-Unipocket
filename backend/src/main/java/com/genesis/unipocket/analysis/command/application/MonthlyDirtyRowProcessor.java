package com.genesis.unipocket.analysis.command.application;

import com.genesis.unipocket.accountbook.command.persistence.entity.AccountBookEntity;
import com.genesis.unipocket.accountbook.command.persistence.repository.AccountBookCommandRepository;
import com.genesis.unipocket.analysis.command.persistence.entity.AnalysisMonthlyDirtyEntity;
import com.genesis.unipocket.analysis.command.persistence.repository.AnalysisBatchAggregationRepository;
import com.genesis.unipocket.analysis.command.persistence.repository.AnalysisBatchAggregationRepository.AmountPairCount;
import com.genesis.unipocket.analysis.command.persistence.repository.AnalysisBatchAggregationRepository.CategoryAmountPairCount;
import com.genesis.unipocket.analysis.command.persistence.repository.AnalysisBatchAggregationRepository.CategoryLocalCurrencyGroupRow;
import com.genesis.unipocket.analysis.command.persistence.repository.AnalysisBatchAggregationRepository.LocalCurrencyGroupRow;
import com.genesis.unipocket.analysis.command.persistence.repository.AnalysisMonthlyDirtyRepository;
import com.genesis.unipocket.analysis.common.enums.AnalysisBatchJobStatus;
import com.genesis.unipocket.analysis.common.enums.AnalysisQualityType;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.global.util.CountryCodeTimezoneMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MonthlyDirtyRowProcessor {

	private final AnalysisMonthlyDirtyRepository monthlyDirtyRepository;
	private final AccountBookCommandRepository accountBookRepository;
	private final AnalysisBatchAggregationRepository aggregationRepository;
	private final MonthlyAmountCorrectionCalculator correctionCalculator;
	private final AccountMonthlyAggregateWriter aggregateWriter;
	private final AnalysisDirtyRowStateManager stateManager;

	public PairMonthKey process(long dirtyId, LocalDateTime claimTime) {
		AnalysisMonthlyDirtyEntity dirty =
				monthlyDirtyRepository
						.findById(dirtyId)
						.orElseThrow(
								() ->
										new IllegalStateException(
												"Monthly dirty row not found: " + dirtyId));
		if (dirty.getStatus() != AnalysisBatchJobStatus.RUNNING) {
			return null;
		}

		AccountBookEntity accountBook =
				accountBookRepository
						.findById(Objects.requireNonNull(dirty.getAccountBookId()))
						.orElseThrow(
								() ->
										new IllegalStateException(
												"Account book not found while processing monthly"
														+ " dirty: "
														+ dirty.getAccountBookId()));

		if (accountBook.getLocalCountryCode() != dirty.getCountryCode()) {
			stateManager.finalizeDirtyRun(dirtyId, claimTime);
			return null;
		}

		LocalDate monthStart = dirty.getTargetYearMonth().withDayOfMonth(1);
		LocalDate nextMonthStart = monthStart.plusMonths(1);
		ZoneId zoneId = CountryCodeTimezoneMapper.getZoneId(dirty.getCountryCode());
		LocalDateTime startUtc = toUtc(monthStart, zoneId);
		LocalDateTime endUtc = toUtc(nextMonthStart, zoneId);

		AmountPairCount rawAmountCount =
				aggregationRepository.aggregateAccountBookMonthlyRaw(
						dirty.getAccountBookId(), startUtc, endUtc);
		List<CategoryAmountPairCount> rawCategoryRows =
				aggregationRepository.aggregateAccountBookMonthlyRawByCategory(
						dirty.getAccountBookId(), startUtc, endUtc);

		CurrencyCode accountBookLocalCurrency = accountBook.getLocalCountryCode().getCurrencyCode();
		OffsetDateTime refDateTime = monthStart.atStartOfDay().atOffset(ZoneOffset.UTC);

		List<LocalCurrencyGroupRow> localCurrencyGroups =
				aggregationRepository.aggregateLocalAmountGroupedByCurrency(
						dirty.getAccountBookId(), startUtc, endUtc);
		BigDecimal correctedLocalTotal =
				correctionCalculator.computeCorrectedLocalAmount(
						localCurrencyGroups, accountBookLocalCurrency, refDateTime);

		List<CategoryLocalCurrencyGroupRow> categoryLocalCurrencyGroups =
				aggregationRepository.aggregateLocalAmountGroupedByCurrencyAndCategory(
						dirty.getAccountBookId(), startUtc, endUtc);
		List<CategoryAmountPairCount> correctedCategoryRows =
				correctionCalculator.computeCorrectedCategoryRows(
						rawCategoryRows,
						categoryLocalCurrencyGroups,
						accountBookLocalCurrency,
						refDateTime);

		AmountPairCount correctedAmountCount =
				new AmountPairCount(
						correctedLocalTotal,
						rawAmountCount.totalBaseAmount(),
						rawAmountCount.expenseCount());

		aggregateWriter.upsertMonthlyMetrics(
				dirty.getAccountBookId(),
				dirty.getCountryCode(),
				monthStart,
				correctedAmountCount,
				AnalysisQualityType.CLEANED);
		aggregateWriter.upsertMonthlyCategoryMetrics(
				dirty.getAccountBookId(),
				dirty.getCountryCode(),
				monthStart,
				correctedCategoryRows,
				AnalysisQualityType.CLEANED);

		stateManager.finalizeDirtyRun(dirtyId, claimTime);
		return new PairMonthKey(
				accountBook.getLocalCountryCode(), accountBook.getBaseCountryCode(), monthStart);
	}

	private LocalDateTime toUtc(LocalDate localDate, ZoneId zoneId) {
		return localDate.atStartOfDay(zoneId).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
	}
}
