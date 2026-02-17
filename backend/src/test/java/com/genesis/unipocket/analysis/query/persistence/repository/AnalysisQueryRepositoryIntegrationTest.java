package com.genesis.unipocket.analysis.query.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.genesis.unipocket.TestcontainersConfiguration;
import com.genesis.unipocket.accountbook.command.persistence.entity.AccountBookEntity;
import com.genesis.unipocket.accountbook.command.persistence.repository.AccountBookCommandRepository;
import com.genesis.unipocket.analysis.common.enums.CurrencyType;
import com.genesis.unipocket.analysis.support.AnalysisFixtureFactory;
import com.genesis.unipocket.expense.command.persistence.repository.ExpenseRepository;
import com.genesis.unipocket.global.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CountryCode;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.user.command.persistence.entity.UserEntity;
import com.genesis.unipocket.user.command.persistence.repository.UserCommandRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
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
class AnalysisQueryRepositoryIntegrationTest {

	@Autowired private AnalysisQueryRepository analysisQueryRepository;
	@Autowired private UserCommandRepository userRepository;
	@Autowired private AccountBookCommandRepository accountBookRepository;
	@Autowired private ExpenseRepository expenseRepository;

	private Long accountBookId;

	@BeforeEach
	void setUp() {
		UserEntity user = AnalysisFixtureFactory.saveUser(userRepository, "analysis-repo");
		AccountBookEntity accountBook =
				AnalysisFixtureFactory.saveAccountBook(
						accountBookRepository,
						user,
						CountryCode.US,
						CountryCode.KR,
						"analysis-query-repository");
		accountBookId = accountBook.getId();
	}

	@Test
	void getAccountBookCountryCodes_existingAccountBook_returnsCountryCodes() {
		Object[] row = analysisQueryRepository.getAccountBookCountryCodes(accountBookId);

		assertThat(row).hasSize(2);
		assertThat(row[0]).isEqualTo(CountryCode.US);
		assertThat(row[1]).isEqualTo(CountryCode.KR);
	}

	@Test
	void getMySpendEvents_incomeExists_excludesIncomeAndUsesCalculatedBaseAmountFallback() {
		AnalysisFixtureFactory.saveExpense(
				expenseRepository,
				accountBookId,
				Category.FOOD,
				AnalysisFixtureFactory.utcDateTime(2025, 12, 2, 10, 0),
				new BigDecimal("100.00"),
				null,
				new BigDecimal("75.00"),
				CurrencyCode.USD,
				CurrencyCode.KRW,
				"first");
		AnalysisFixtureFactory.saveExpense(
				expenseRepository,
				accountBookId,
				Category.LIVING,
				AnalysisFixtureFactory.utcDateTime(2025, 12, 3, 10, 0),
				new BigDecimal("200.00"),
				new BigDecimal("150.00"),
				null,
				CurrencyCode.USD,
				CurrencyCode.KRW,
				"second");
		AnalysisFixtureFactory.saveExpense(
				expenseRepository,
				accountBookId,
				Category.INCOME,
				AnalysisFixtureFactory.utcDateTime(2025, 12, 4, 10, 0),
				new BigDecimal("999.00"),
				new BigDecimal("888.00"),
				null,
				CurrencyCode.USD,
				CurrencyCode.KRW,
				"income");

		List<Object[]> baseRows =
				analysisQueryRepository.getMySpendEvents(
						accountBookId,
						AnalysisFixtureFactory.utcDateTime(2025, 12, 1, 0, 0),
						AnalysisFixtureFactory.utcDateTime(2025, 12, 31, 23, 59),
						CurrencyType.BASE);
		List<Object[]> localRows =
				analysisQueryRepository.getMySpendEvents(
						accountBookId,
						AnalysisFixtureFactory.utcDateTime(2025, 12, 1, 0, 0),
						AnalysisFixtureFactory.utcDateTime(2025, 12, 31, 23, 59),
						CurrencyType.LOCAL);

		assertThat(baseRows).hasSize(2);
		assertThat((BigDecimal) baseRows.get(0)[1]).isEqualByComparingTo(new BigDecimal("75.00"));
		assertThat((BigDecimal) baseRows.get(1)[1]).isEqualByComparingTo(new BigDecimal("150.00"));
		assertThat(localRows).hasSize(2);
		assertThat((BigDecimal) localRows.get(0)[1]).isEqualByComparingTo(new BigDecimal("100.00"));
		assertThat((BigDecimal) localRows.get(1)[1]).isEqualByComparingTo(new BigDecimal("200.00"));
	}

	@Test
	void getMyCategorySpent_incomeIncludedInRawData_excludesIncomeFromAggregation() {
		AnalysisFixtureFactory.saveExpense(
				expenseRepository,
				accountBookId,
				Category.FOOD,
				AnalysisFixtureFactory.utcDateTime(2025, 12, 2, 10, 0),
				new BigDecimal("10.00"),
				new BigDecimal("10.00"),
				null,
				CurrencyCode.USD,
				CurrencyCode.KRW,
				"food");
		AnalysisFixtureFactory.saveExpense(
				expenseRepository,
				accountBookId,
				Category.LIVING,
				AnalysisFixtureFactory.utcDateTime(2025, 12, 3, 10, 0),
				new BigDecimal("25.00"),
				new BigDecimal("25.00"),
				null,
				CurrencyCode.USD,
				CurrencyCode.KRW,
				"living");
		AnalysisFixtureFactory.saveExpense(
				expenseRepository,
				accountBookId,
				Category.INCOME,
				AnalysisFixtureFactory.utcDateTime(2025, 12, 4, 10, 0),
				new BigDecimal("999.00"),
				new BigDecimal("999.00"),
				null,
				CurrencyCode.USD,
				CurrencyCode.KRW,
				"income");

		List<Object[]> rows =
				analysisQueryRepository.getMyCategorySpent(
						accountBookId,
						AnalysisFixtureFactory.utcDateTime(2025, 12, 1, 0, 0),
						AnalysisFixtureFactory.utcDateTime(2025, 12, 31, 23, 59),
						CurrencyType.BASE);

		Map<Category, BigDecimal> result =
				rows.stream()
						.collect(
								java.util.stream.Collectors.toMap(
										r -> (Category) r[0], r -> (BigDecimal) r[1]));
		assertThat(result).containsEntry(Category.FOOD, new BigDecimal("10.00"));
		assertThat(result).containsEntry(Category.LIVING, new BigDecimal("25.00"));
		assertThat(result).doesNotContainKey(Category.INCOME);
	}
}
