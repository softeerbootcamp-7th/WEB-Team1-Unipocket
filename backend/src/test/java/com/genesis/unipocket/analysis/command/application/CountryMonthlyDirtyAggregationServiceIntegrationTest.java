package com.genesis.unipocket.analysis.command.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.genesis.unipocket.TestcontainersConfiguration;
import com.genesis.unipocket.accountbook.command.persistence.entity.AccountBookEntity;
import com.genesis.unipocket.accountbook.command.persistence.repository.AccountBookCommandRepository;
import com.genesis.unipocket.analysis.command.persistence.entity.AccountMonthlyAggregateEntity;
import com.genesis.unipocket.analysis.command.persistence.entity.AnalysisMetricType;
import com.genesis.unipocket.analysis.command.persistence.entity.AnalysisQualityType;
import com.genesis.unipocket.analysis.command.persistence.entity.PairMonthlyAggregateEntity;
import com.genesis.unipocket.analysis.command.persistence.entity.PairMonthlyCategoryAggregateEntity;
import com.genesis.unipocket.analysis.command.persistence.repository.AccountMonthlyAggregateRepository;
import com.genesis.unipocket.analysis.command.persistence.repository.AnalysisMonthlyDirtyRepository;
import com.genesis.unipocket.analysis.command.persistence.repository.PairMonthlyAggregateRepository;
import com.genesis.unipocket.analysis.command.persistence.repository.PairMonthlyCategoryAggregateRepository;
import com.genesis.unipocket.analysis.common.enums.CurrencyType;
import com.genesis.unipocket.analysis.support.AnalysisFixtureFactory;
import com.genesis.unipocket.expense.command.persistence.repository.ExpenseRepository;
import com.genesis.unipocket.global.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CountryCode;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.user.command.persistence.entity.UserEntity;
import com.genesis.unipocket.user.command.persistence.repository.UserCommandRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test-it")
@Import(TestcontainersConfiguration.class)
@Transactional
@Tag("integration")
class CountryMonthlyDirtyAggregationServiceIntegrationTest {

	@Autowired private CountryMonthlyDirtyAggregationService service;
	@Autowired private UserCommandRepository userRepository;
	@Autowired private AccountBookCommandRepository accountBookRepository;
	@Autowired private ExpenseRepository expenseRepository;
	@Autowired private AnalysisMonthlyDirtyRepository dirtyRepository;
	@Autowired private AccountMonthlyAggregateRepository accountMonthlyAggregateRepository;
	@Autowired private PairMonthlyAggregateRepository pairMonthlyAggregateRepository;

	@Autowired
	private PairMonthlyCategoryAggregateRepository pairMonthlyCategoryAggregateRepository;

	@Test
	void processCountryDirtyRows_pendingDirtyRowsExist_updatesAccountAndPairAggregates() {
		LocalDate monthStart = LocalDate.of(2026, 1, 1);

		UserEntity user = AnalysisFixtureFactory.saveUser(userRepository, "analysis-batch");
		AccountBookEntity firstAccountBook =
				AnalysisFixtureFactory.saveAccountBook(
						accountBookRepository, user, CountryCode.US, CountryCode.KR, "batch-first");
		AccountBookEntity secondAccountBook =
				AnalysisFixtureFactory.saveAccountBook(
						accountBookRepository,
						user,
						CountryCode.US,
						CountryCode.KR,
						"batch-second");

		AnalysisFixtureFactory.saveExpense(
				expenseRepository,
				firstAccountBook.getId(),
				Category.FOOD,
				AnalysisFixtureFactory.utcDateTime(2026, 1, 10, 10, 0),
				new BigDecimal("100.00"),
				new BigDecimal("70.00"),
				null,
				CurrencyCode.USD,
				CurrencyCode.KRW,
				"first-food");
		AnalysisFixtureFactory.saveExpense(
				expenseRepository,
				firstAccountBook.getId(),
				Category.LIVING,
				AnalysisFixtureFactory.utcDateTime(2026, 1, 11, 10, 0),
				new BigDecimal("50.00"),
				new BigDecimal("35.00"),
				null,
				CurrencyCode.USD,
				CurrencyCode.KRW,
				"first-living");
		AnalysisFixtureFactory.saveExpense(
				expenseRepository,
				secondAccountBook.getId(),
				Category.FOOD,
				AnalysisFixtureFactory.utcDateTime(2026, 1, 12, 10, 0),
				new BigDecimal("80.00"),
				new BigDecimal("56.00"),
				null,
				CurrencyCode.USD,
				CurrencyCode.KRW,
				"second-food");

		AnalysisFixtureFactory.savePendingDirty(
				dirtyRepository, CountryCode.US, firstAccountBook.getId(), monthStart);
		AnalysisFixtureFactory.savePendingDirty(
				dirtyRepository, CountryCode.US, secondAccountBook.getId(), monthStart);

		service.processCountryDirtyRows(CountryCode.US);

		assertThat(dirtyRepository.findAll())
				.allMatch(dirty -> dirty.getStatus() == AnalysisFixtureFactory.successStatus());

		assertMonthlyAggregate(
				firstAccountBook.getId(),
				monthStart,
				AnalysisMetricType.TOTAL_LOCAL_AMOUNT,
				AnalysisQualityType.CLEANED,
				"150.0000");
		assertMonthlyAggregate(
				firstAccountBook.getId(),
				monthStart,
				AnalysisMetricType.TOTAL_BASE_AMOUNT,
				AnalysisQualityType.CLEANED,
				"105.0000");
		assertMonthlyAggregate(
				secondAccountBook.getId(),
				monthStart,
				AnalysisMetricType.TOTAL_BASE_AMOUNT,
				AnalysisQualityType.CLEANED,
				"56.0000");

		PairMonthlyAggregateEntity pairBase =
				pairMonthlyAggregateRepository
						.findByLocalCountryCodeAndBaseCountryCodeAndTargetYearMonthAndQualityTypeAndMetricType(
								CountryCode.US,
								CountryCode.KR,
								monthStart,
								AnalysisQualityType.CLEANED,
								AnalysisMetricType.TOTAL_BASE_AMOUNT)
						.orElseThrow();
		assertThat(pairBase.getIncludedAccountCount()).isEqualTo(2L);
		assertThat(pairBase.getTotalMetricSum()).isEqualByComparingTo("161.0000");
		assertThat(pairBase.getAverageMetricValue()).isEqualByComparingTo("80.5000");

		PairMonthlyAggregateEntity pairLocal =
				pairMonthlyAggregateRepository
						.findByLocalCountryCodeAndBaseCountryCodeAndTargetYearMonthAndQualityTypeAndMetricType(
								CountryCode.US,
								CountryCode.KR,
								monthStart,
								AnalysisQualityType.CLEANED,
								AnalysisMetricType.TOTAL_LOCAL_AMOUNT)
						.orElseThrow();
		assertThat(pairLocal.getTotalMetricSum()).isEqualByComparingTo("230.0000");
		assertThat(pairLocal.getAverageMetricValue()).isEqualByComparingTo("115.0000");

		PairMonthlyCategoryAggregateEntity foodBase =
				findPairCategory(monthStart, CurrencyType.BASE, Category.FOOD).orElseThrow();
		PairMonthlyCategoryAggregateEntity livingBase =
				findPairCategory(monthStart, CurrencyType.BASE, Category.LIVING).orElseThrow();
		PairMonthlyCategoryAggregateEntity transportBase =
				findPairCategory(monthStart, CurrencyType.BASE, Category.TRANSPORT).orElseThrow();

		assertThat(foodBase.getTotalAmount()).isEqualByComparingTo("126.0000");
		assertThat(foodBase.getAverageAmount()).isEqualByComparingTo("63.0000");
		assertThat(livingBase.getTotalAmount()).isEqualByComparingTo("35.0000");
		assertThat(livingBase.getAverageAmount()).isEqualByComparingTo("17.5000");
		assertThat(transportBase.getTotalAmount()).isEqualByComparingTo("0.0000");
	}

	private void assertMonthlyAggregate(
			Long accountBookId,
			LocalDate monthStart,
			AnalysisMetricType metricType,
			AnalysisQualityType qualityType,
			String expectedValue) {
		AccountMonthlyAggregateEntity entity =
				accountMonthlyAggregateRepository
						.findByAccountBookIdAndTargetYearMonthAndMetricTypeAndQualityType(
								accountBookId, monthStart, metricType, qualityType)
						.orElseThrow();
		assertThat(entity.getMetricValue()).isEqualByComparingTo(expectedValue);
	}

	private Optional<PairMonthlyCategoryAggregateEntity> findPairCategory(
			LocalDate monthStart, CurrencyType currencyType, Category category) {
		return pairMonthlyCategoryAggregateRepository
				.findAllByLocalCountryCodeAndBaseCountryCodeAndTargetYearMonthAndQualityTypeAndCurrencyType(
						CountryCode.US,
						CountryCode.KR,
						monthStart,
						AnalysisQualityType.CLEANED,
						currencyType)
				.stream()
				.filter(row -> row.getCategory() == category)
				.findFirst();
	}
}
