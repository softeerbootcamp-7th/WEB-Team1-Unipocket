package com.genesis.unipocket.analysis.command.persistence.repository.support;

import static org.assertj.core.api.Assertions.assertThat;

import com.genesis.unipocket.TestcontainersConfiguration;
import com.genesis.unipocket.accountbook.command.persistence.entity.AccountBookEntity;
import com.genesis.unipocket.accountbook.command.persistence.repository.AccountBookCommandRepository;
import com.genesis.unipocket.analysis.command.persistence.repository.support.AnalysisBatchAggregationRepository.AmountPairCount;
import com.genesis.unipocket.analysis.command.persistence.repository.support.AnalysisBatchAggregationRepository.CategoryAmountPairCount;
import com.genesis.unipocket.analysis.command.persistence.repository.support.AnalysisBatchAggregationRepository.ExpenseRow;
import com.genesis.unipocket.analysis.support.AnalysisFixtureFactory;
import com.genesis.unipocket.expense.command.persistence.repository.ExpenseRepository;
import com.genesis.unipocket.global.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CountryCode;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.user.command.persistence.entity.UserEntity;
import com.genesis.unipocket.user.command.persistence.repository.UserCommandRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
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
class AnalysisBatchAggregationRepositoryIntegrationTest {

	@Autowired private AnalysisBatchAggregationRepository repository;
	@Autowired private UserCommandRepository userRepository;
	@Autowired private AccountBookCommandRepository accountBookRepository;
	@Autowired private ExpenseRepository expenseRepository;

	private Long accountBookId;

	@BeforeEach
	void setUp() {
		UserEntity user = AnalysisFixtureFactory.saveUser(userRepository, "analysis-native");
		AccountBookEntity accountBook =
				AnalysisFixtureFactory.saveAccountBook(
						accountBookRepository,
						user,
						CountryCode.US,
						CountryCode.KR,
						"analysis-native-query");
		accountBookId = accountBook.getId();
	}

	@Test
	void aggregateAccountBookMonthlyRaw_givenExpenses_returnsLocalAndBaseTotals() {
		AnalysisFixtureFactory.saveExpense(
				expenseRepository,
				accountBookId,
				Category.FOOD,
				AnalysisFixtureFactory.utcDateTime(2025, 12, 10, 10, 0),
				new BigDecimal("100.00"),
				new BigDecimal("70.00"),
				null,
				CurrencyCode.USD,
				CurrencyCode.KRW,
				"a");
		AnalysisFixtureFactory.saveExpense(
				expenseRepository,
				accountBookId,
				Category.LIVING,
				AnalysisFixtureFactory.utcDateTime(2025, 12, 11, 10, 0),
				new BigDecimal("60.00"),
				null,
				new BigDecimal("45.00"),
				CurrencyCode.USD,
				CurrencyCode.KRW,
				"b");
		AnalysisFixtureFactory.saveExpense(
				expenseRepository,
				accountBookId,
				Category.INCOME,
				AnalysisFixtureFactory.utcDateTime(2025, 12, 12, 10, 0),
				new BigDecimal("300.00"),
				new BigDecimal("210.00"),
				null,
				CurrencyCode.USD,
				CurrencyCode.KRW,
				"income");

		AmountPairCount result =
				repository.aggregateAccountBookMonthlyRaw(
						accountBookId,
						LocalDateTime.of(2025, 12, 1, 0, 0),
						LocalDateTime.of(2026, 1, 1, 0, 0));

		assertThat(result.totalLocalAmount()).isEqualByComparingTo("160.00");
		assertThat(result.totalBaseAmount()).isEqualByComparingTo("115.00");
		assertThat(result.expenseCount()).isEqualTo(2L);
	}

	@Test
	void findExpenseRows_givenExpenses_returnsMappedRowsWithCountryCodes() {
		AnalysisFixtureFactory.saveExpense(
				expenseRepository,
				accountBookId,
				Category.TRANSPORT,
				AnalysisFixtureFactory.utcDateTime(2025, 12, 20, 12, 0),
				new BigDecimal("12.00"),
				new BigDecimal("9.00"),
				null,
				CurrencyCode.USD,
				CurrencyCode.KRW,
				"transport");

		List<ExpenseRow> rows =
				repository.findExpenseRowsByAccountBook(
						accountBookId,
						LocalDateTime.of(2025, 12, 1, 0, 0),
						LocalDateTime.of(2026, 1, 1, 0, 0));

		assertThat(rows).hasSize(1);
		ExpenseRow row = rows.get(0);
		assertThat(row.accountBookId()).isEqualTo(accountBookId);
		assertThat(((Number) row.categoryValue()).intValue())
				.isEqualTo(Category.TRANSPORT.ordinal());
		assertThat(row.localAmount()).isEqualByComparingTo("12.00");
		assertThat(row.baseAmount()).isEqualByComparingTo("9.00");
		assertThat(row.localCountryCode()).isEqualTo(CountryCode.US.name());
		assertThat(row.baseCountryCode()).isEqualTo(CountryCode.KR.name());
	}

	@Test
	void aggregateAccountBookMonthlyRawByCategory_givenExpenses_returnsCategoryGroupedTotals() {
		AnalysisFixtureFactory.saveExpense(
				expenseRepository,
				accountBookId,
				Category.FOOD,
				AnalysisFixtureFactory.utcDateTime(2025, 12, 10, 10, 0),
				new BigDecimal("10.00"),
				new BigDecimal("7.00"),
				null,
				CurrencyCode.USD,
				CurrencyCode.KRW,
				"food-1");
		AnalysisFixtureFactory.saveExpense(
				expenseRepository,
				accountBookId,
				Category.FOOD,
				AnalysisFixtureFactory.utcDateTime(2025, 12, 11, 10, 0),
				new BigDecimal("20.00"),
				new BigDecimal("14.00"),
				null,
				CurrencyCode.USD,
				CurrencyCode.KRW,
				"food-2");
		AnalysisFixtureFactory.saveExpense(
				expenseRepository,
				accountBookId,
				Category.LIVING,
				AnalysisFixtureFactory.utcDateTime(2025, 12, 12, 10, 0),
				new BigDecimal("30.00"),
				new BigDecimal("21.00"),
				null,
				CurrencyCode.USD,
				CurrencyCode.KRW,
				"living");
		AnalysisFixtureFactory.saveExpense(
				expenseRepository,
				accountBookId,
				Category.INCOME,
				AnalysisFixtureFactory.utcDateTime(2025, 12, 13, 10, 0),
				new BigDecimal("200.00"),
				new BigDecimal("140.00"),
				null,
				CurrencyCode.USD,
				CurrencyCode.KRW,
				"income");

		List<CategoryAmountPairCount> rows =
				repository.aggregateAccountBookMonthlyRawByCategory(
						accountBookId,
						LocalDateTime.of(2025, 12, 1, 0, 0),
						LocalDateTime.of(2026, 1, 1, 0, 0));

		CategoryAmountPairCount food =
				rows.stream()
						.filter(
								row ->
										row.categoryOrdinal() != null
												&& row.categoryOrdinal() == Category.FOOD.ordinal())
						.findFirst()
						.orElseThrow();
		CategoryAmountPairCount living =
				rows.stream()
						.filter(
								row ->
										row.categoryOrdinal() != null
												&& row.categoryOrdinal()
														== Category.LIVING.ordinal())
						.findFirst()
						.orElseThrow();

		assertThat(food.totalLocalAmount()).isEqualByComparingTo("30.00");
		assertThat(food.totalBaseAmount()).isEqualByComparingTo("21.00");
		assertThat(food.expenseCount()).isEqualTo(2L);
		assertThat(living.totalLocalAmount()).isEqualByComparingTo("30.00");
		assertThat(living.totalBaseAmount()).isEqualByComparingTo("21.00");
		assertThat(living.expenseCount()).isEqualTo(1L);
		assertThat(rows)
				.noneMatch(
						row ->
								row.categoryOrdinal() != null
										&& row.categoryOrdinal() == Category.INCOME.ordinal());
	}
}
