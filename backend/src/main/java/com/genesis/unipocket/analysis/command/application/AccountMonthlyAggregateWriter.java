package com.genesis.unipocket.analysis.command.application;

import com.genesis.unipocket.analysis.command.persistence.entity.AccountMonthlyAggregateEntity;
import com.genesis.unipocket.analysis.command.persistence.entity.AccountMonthlyCategoryAggregateEntity;
import com.genesis.unipocket.analysis.command.persistence.repository.AccountMonthlyAggregateRepository;
import com.genesis.unipocket.analysis.command.persistence.repository.AccountMonthlyCategoryAggregateRepository;
import com.genesis.unipocket.analysis.command.persistence.repository.AnalysisBatchAggregationRepository.AmountPairCount;
import com.genesis.unipocket.analysis.command.persistence.repository.AnalysisBatchAggregationRepository.CategoryAmountPairCount;
import com.genesis.unipocket.analysis.common.enums.AnalysisMetricType;
import com.genesis.unipocket.analysis.common.enums.AnalysisQualityType;
import com.genesis.unipocket.analysis.common.enums.CurrencyType;
import com.genesis.unipocket.global.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CountryCode;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountMonthlyAggregateWriter {

	private final AccountMonthlyAggregateRepository accountMonthlyAggregateRepository;
	private final AccountMonthlyCategoryAggregateRepository
			accountMonthlyCategoryAggregateRepository;

	public void upsertMonthlyMetrics(
			Long accountBookId,
			CountryCode countryCode,
			LocalDate monthStart,
			AmountPairCount amountCount,
			AnalysisQualityType qualityType) {
		upsertMonthlyMetric(
				accountBookId,
				countryCode,
				monthStart,
				AnalysisMetricType.TOTAL_LOCAL_AMOUNT,
				qualityType,
				amountCount.totalLocalAmount());
		upsertMonthlyMetric(
				accountBookId,
				countryCode,
				monthStart,
				AnalysisMetricType.TOTAL_BASE_AMOUNT,
				qualityType,
				amountCount.totalBaseAmount());
	}

	private void upsertMonthlyMetric(
			Long accountBookId,
			CountryCode countryCode,
			LocalDate monthStart,
			AnalysisMetricType metricType,
			AnalysisQualityType qualityType,
			BigDecimal value) {
		var existing =
				accountMonthlyAggregateRepository
						.findByAccountBookIdAndTargetYearMonthAndMetricTypeAndQualityType(
								accountBookId, monthStart, metricType, qualityType);
		if (existing.isPresent()) {
			existing.get().updateMetricValue(value);
		} else {
			accountMonthlyAggregateRepository.save(
					Objects.requireNonNull(
							AccountMonthlyAggregateEntity.of(
									accountBookId,
									countryCode,
									monthStart,
									metricType,
									qualityType,
									value)));
		}
	}

	public void upsertMonthlyCategoryMetrics(
			Long accountBookId,
			CountryCode countryCode,
			LocalDate monthStart,
			List<CategoryAmountPairCount> rows,
			AnalysisQualityType qualityType) {
		accountMonthlyCategoryAggregateRepository
				.deleteByAccountBookIdAndTargetYearMonthAndQualityType(
						accountBookId, monthStart, qualityType);
		accountMonthlyCategoryAggregateRepository.flush();
		if (rows.isEmpty()) {
			return;
		}

		List<AccountMonthlyCategoryAggregateEntity> toSave = new ArrayList<>(rows.size() * 2);
		for (CategoryAmountPairCount row : rows) {
			Category category = toCategory(row.categoryOrdinal());
			toSave.add(
					AccountMonthlyCategoryAggregateEntity.of(
							accountBookId,
							countryCode,
							monthStart,
							category,
							qualityType,
							CurrencyType.LOCAL,
							row.totalLocalAmount(),
							row.expenseCount()));
			toSave.add(
					AccountMonthlyCategoryAggregateEntity.of(
							accountBookId,
							countryCode,
							monthStart,
							category,
							qualityType,
							CurrencyType.BASE,
							row.totalBaseAmount(),
							row.expenseCount()));
		}
		accountMonthlyCategoryAggregateRepository.saveAll(toSave);
	}

	private Category toCategory(Integer ordinal) {
		if (ordinal == null || ordinal < 0 || ordinal >= Category.values().length) {
			throw new IllegalArgumentException("Invalid category ordinal: " + ordinal);
		}
		return Category.values()[ordinal];
	}
}
