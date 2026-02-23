package com.genesis.unipocket.analysis.support;

import com.genesis.unipocket.accountbook.command.persistence.entity.AccountBookCreateArgs;
import com.genesis.unipocket.accountbook.command.persistence.entity.AccountBookEntity;
import com.genesis.unipocket.accountbook.command.persistence.repository.AccountBookCommandRepository;
import com.genesis.unipocket.analysis.command.persistence.entity.AnalysisBatchJobStatus;
import com.genesis.unipocket.analysis.command.persistence.entity.AnalysisMetricType;
import com.genesis.unipocket.analysis.command.persistence.entity.AnalysisMonthlyDirtyEntity;
import com.genesis.unipocket.analysis.command.persistence.entity.AnalysisQualityType;
import com.genesis.unipocket.analysis.command.persistence.entity.PairMonthlyAggregateEntity;
import com.genesis.unipocket.analysis.command.persistence.entity.PairMonthlyCategoryAggregateEntity;
import com.genesis.unipocket.analysis.command.persistence.repository.AnalysisMonthlyDirtyRepository;
import com.genesis.unipocket.analysis.command.persistence.repository.PairMonthlyAggregateRepository;
import com.genesis.unipocket.analysis.command.persistence.repository.PairMonthlyCategoryAggregateRepository;
import com.genesis.unipocket.analysis.common.enums.CurrencyType;
import com.genesis.unipocket.expense.command.persistence.entity.ExpenseEntity;
import com.genesis.unipocket.expense.command.persistence.entity.dto.ExpenseManualCreateArgs;
import com.genesis.unipocket.expense.command.persistence.repository.ExpenseRepository;
import com.genesis.unipocket.global.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CountryCode;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.user.command.persistence.entity.UserEntity;
import com.genesis.unipocket.user.command.persistence.repository.UserCommandRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

public final class AnalysisFixtureFactory {

	private AnalysisFixtureFactory() {}

	public static UserEntity saveUser(UserCommandRepository userRepository, String emailPrefix) {
		String unique = UUID.randomUUID().toString().substring(0, 8);
		return userRepository.save(
				UserEntity.builder()
						.email(emailPrefix + "+" + unique + "@unipocket.com")
						.name("analysis-user-" + unique)
						.mainBucketId(1L)
						.build());
	}

	public static AccountBookEntity saveAccountBook(
			AccountBookCommandRepository accountBookRepository,
			UserEntity user,
			CountryCode localCountryCode,
			CountryCode baseCountryCode,
			String title) {
		return accountBookRepository.save(
				AccountBookEntity.create(
						new AccountBookCreateArgs(
								user,
								title,
								localCountryCode,
								baseCountryCode,
								1,
								null,
								LocalDate.of(2026, 1, 1),
								LocalDate.of(2026, 12, 31))));
	}

	public static ExpenseEntity saveExpense(
			ExpenseRepository expenseRepository,
			Long accountBookId,
			Category category,
			OffsetDateTime occurredAt,
			BigDecimal localAmount,
			BigDecimal baseAmount,
			BigDecimal calculatedBaseAmount,
			CurrencyCode localCurrencyCode,
			CurrencyCode baseCurrencyCode,
			String merchantName) {
		ExpenseManualCreateArgs args =
				new ExpenseManualCreateArgs(
						accountBookId,
						merchantName,
						category,
						null,
						occurredAt,
						localAmount,
						localCurrencyCode,
						baseAmount,
						baseCurrencyCode,
						calculatedBaseAmount,
						baseCurrencyCode,
						"analysis-fixture",
						null,
						BigDecimal.ONE);
		return expenseRepository.save(ExpenseEntity.manual(args));
	}

	public static AnalysisMonthlyDirtyEntity savePendingDirty(
			AnalysisMonthlyDirtyRepository dirtyRepository,
			CountryCode countryCode,
			Long accountBookId,
			LocalDate monthStart) {
		return savePendingDirtyWithEventAt(
				dirtyRepository,
				countryCode,
				accountBookId,
				monthStart,
				LocalDateTime.of(2026, 1, 15, 0, 0));
	}

	public static AnalysisMonthlyDirtyEntity savePendingDirtyWithEventAt(
			AnalysisMonthlyDirtyRepository dirtyRepository,
			CountryCode countryCode,
			Long accountBookId,
			LocalDate monthStart,
			LocalDateTime lastEventAtUtc) {
		AnalysisMonthlyDirtyEntity entity =
				AnalysisMonthlyDirtyEntity.create(
						countryCode, accountBookId, monthStart, lastEventAtUtc);
		entity.markPendingFromEvent(lastEventAtUtc);
		return dirtyRepository.save(entity);
	}

	public static AnalysisMonthlyDirtyEntity saveRetryDirty(
			AnalysisMonthlyDirtyRepository dirtyRepository,
			CountryCode countryCode,
			Long accountBookId,
			LocalDate monthStart,
			LocalDateTime nextRetryAtUtc) {
		AnalysisMonthlyDirtyEntity entity =
				AnalysisMonthlyDirtyEntity.create(
						countryCode,
						accountBookId,
						monthStart,
						LocalDateTime.of(2026, 1, 15, 0, 0));
		entity.markRetry(nextRetryAtUtc, "TEST", "retry");
		return dirtyRepository.save(entity);
	}

	public static PairMonthlyAggregateEntity savePairMonthlyAggregate(
			PairMonthlyAggregateRepository repository,
			CountryCode localCountryCode,
			CountryCode baseCountryCode,
			LocalDate monthStart,
			AnalysisMetricType metricType,
			long includedAccountCount,
			BigDecimal totalMetricSum,
			BigDecimal averageMetricValue,
			BigDecimal lower,
			BigDecimal upper) {
		return repository.save(
				PairMonthlyAggregateEntity.of(
						localCountryCode,
						baseCountryCode,
						monthStart,
						AnalysisQualityType.CLEANED,
						metricType,
						includedAccountCount,
						totalMetricSum,
						averageMetricValue,
						lower,
						upper));
	}

	public static PairMonthlyCategoryAggregateEntity savePairMonthlyCategoryAggregate(
			PairMonthlyCategoryAggregateRepository repository,
			CountryCode localCountryCode,
			CountryCode baseCountryCode,
			LocalDate monthStart,
			CurrencyType currencyType,
			Category category,
			long includedAccountCount,
			BigDecimal totalAmount,
			BigDecimal averageAmount) {
		return repository.save(
				PairMonthlyCategoryAggregateEntity.of(
						localCountryCode,
						baseCountryCode,
						monthStart,
						AnalysisQualityType.CLEANED,
						currencyType,
						category,
						includedAccountCount,
						totalAmount,
						averageAmount));
	}

	public static OffsetDateTime utcDateTime(int year, int month, int day, int hour, int minute) {
		return OffsetDateTime.of(year, month, day, hour, minute, 0, 0, ZoneOffset.UTC);
	}

	public static AnalysisBatchJobStatus successStatus() {
		return AnalysisBatchJobStatus.SUCCESS;
	}
}
