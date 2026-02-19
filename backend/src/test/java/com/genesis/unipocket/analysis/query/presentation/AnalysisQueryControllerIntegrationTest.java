package com.genesis.unipocket.analysis.query.presentation;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.genesis.unipocket.TestcontainersConfiguration;
import com.genesis.unipocket.accountbook.command.persistence.entity.AccountBookEntity;
import com.genesis.unipocket.accountbook.command.persistence.repository.AccountBookCommandRepository;
import com.genesis.unipocket.analysis.command.persistence.entity.AnalysisMetricType;
import com.genesis.unipocket.analysis.command.persistence.repository.PairMonthlyAggregateRepository;
import com.genesis.unipocket.analysis.command.persistence.repository.PairMonthlyCategoryAggregateRepository;
import com.genesis.unipocket.analysis.common.enums.CurrencyType;
import com.genesis.unipocket.analysis.support.AnalysisFixtureFactory;
import com.genesis.unipocket.auth.support.JwtTestHelper;
import com.genesis.unipocket.expense.command.persistence.repository.ExpenseRepository;
import com.genesis.unipocket.global.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CountryCode;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.user.command.persistence.entity.UserEntity;
import com.genesis.unipocket.user.command.persistence.repository.UserCommandRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@AutoConfigureMockMvc
@SpringBootTest
@ActiveProfiles("test-it")
@Import(TestcontainersConfiguration.class)
@Transactional
@Tag("integration")
class AnalysisQueryControllerIntegrationTest {

	@Autowired private MockMvc mockMvc;
	@Autowired private JwtTestHelper jwtTestHelper;
	@Autowired private UserCommandRepository userRepository;
	@Autowired private AccountBookCommandRepository accountBookRepository;
	@Autowired private ExpenseRepository expenseRepository;
	@Autowired private PairMonthlyAggregateRepository pairMonthlyAggregateRepository;

	@Autowired
	private PairMonthlyCategoryAggregateRepository pairMonthlyCategoryAggregateRepository;

	private UUID userId;
	private Long accountBookId;

	@BeforeEach
	void setUp() {
		UserEntity user = AnalysisFixtureFactory.saveUser(userRepository, "analysis-controller");
		userId = user.getId();

		AccountBookEntity accountBook =
				AnalysisFixtureFactory.saveAccountBook(
						accountBookRepository,
						user,
						CountryCode.US,
						CountryCode.KR,
						"analysis-controller");
		accountBookId = accountBook.getId();
	}

	@Test
	void getAnalysisOverview_peerAndCategoryAggregatesExist_returnsMergedOverview()
			throws Exception {
		AnalysisFixtureFactory.saveExpense(
				expenseRepository,
				accountBookId,
				Category.FOOD,
				AnalysisFixtureFactory.utcDateTime(2025, 12, 2, 11, 0),
				new BigDecimal("240.00"),
				new BigDecimal("200.00"),
				null,
				CurrencyCode.USD,
				CurrencyCode.KRW,
				"food");
		AnalysisFixtureFactory.saveExpense(
				expenseRepository,
				accountBookId,
				Category.LIVING,
				AnalysisFixtureFactory.utcDateTime(2025, 12, 4, 11, 0),
				new BigDecimal("360.00"),
				new BigDecimal("300.00"),
				null,
				CurrencyCode.USD,
				CurrencyCode.KRW,
				"living");
		AnalysisFixtureFactory.saveExpense(
				expenseRepository,
				accountBookId,
				Category.FOOD,
				AnalysisFixtureFactory.utcDateTime(2025, 11, 10, 11, 0),
				new BigDecimal("60.00"),
				new BigDecimal("60.00"),
				null,
				CurrencyCode.USD,
				CurrencyCode.KRW,
				"prev");
		AnalysisFixtureFactory.saveExpense(
				expenseRepository,
				accountBookId,
				Category.LIVING,
				AnalysisFixtureFactory.utcDateTime(2025, 11, 20, 11, 0),
				new BigDecimal("40.00"),
				new BigDecimal("40.00"),
				null,
				CurrencyCode.USD,
				CurrencyCode.KRW,
				"prev-living");

		AnalysisFixtureFactory.savePairMonthlyAggregate(
				pairMonthlyAggregateRepository,
				CountryCode.US,
				CountryCode.KR,
				LocalDate.of(2025, 12, 1),
				AnalysisMetricType.TOTAL_BASE_AMOUNT,
				5L,
				new BigDecimal("2000.00"),
				new BigDecimal("400.00"),
				new BigDecimal("0.00"),
				new BigDecimal("999999.00"));
		AnalysisFixtureFactory.savePairMonthlyCategoryAggregate(
				pairMonthlyCategoryAggregateRepository,
				CountryCode.US,
				CountryCode.KR,
				LocalDate.of(2025, 12, 1),
				CurrencyType.BASE,
				Category.FOOD,
				5L,
				new BigDecimal("1000.00"),
				new BigDecimal("200.00"));
		AnalysisFixtureFactory.savePairMonthlyCategoryAggregate(
				pairMonthlyCategoryAggregateRepository,
				CountryCode.US,
				CountryCode.KR,
				LocalDate.of(2025, 12, 1),
				CurrencyType.BASE,
				Category.LIVING,
				5L,
				new BigDecimal("500.00"),
				new BigDecimal("100.00"));

		mockMvc.perform(
						get("/account-books/{accountBookId}/analysis", accountBookId)
								.with(jwtTestHelper.withJwtAuth(userId))
								.queryParam("year", "2025")
								.queryParam("month", "12")
								.queryParam("currencyType", "BASE"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.countryCode").value("KR"))
				.andExpect(jsonPath("$.compareWithAverage.month").value(12))
				.andExpect(jsonPath("$.compareWithAverage.mySpentAmount").value("500"))
				.andExpect(jsonPath("$.compareWithAverage.averageSpentAmount").value("400"))
				.andExpect(jsonPath("$.compareWithAverage.spentAmountDiff").value("100"))
				.andExpect(jsonPath("$.compareWithLastMonth.diff").value("400"))
				.andExpect(
						jsonPath("$.compareWithLastMonth.totalSpent.thisMonthToDate").value("500"))
				.andExpect(
						jsonPath("$.compareWithLastMonth.totalSpent.lastMonthTotal").value("100"))
				.andExpect(jsonPath("$.compareWithLastMonth.thisMonthItem.length()").value(31))
				.andExpect(jsonPath("$.compareWithLastMonth.prevMonthItem.length()").value(30))
				.andExpect(
						jsonPath("$.compareByCategory.maxDiffCategoryIndex")
								.value(Category.LIVING.ordinal()))
				.andExpect(jsonPath("$.compareByCategory.isOverSpent").value(true))
				.andExpect(jsonPath("$.compareByCategory.maxLabel").value("360"))
				.andExpect(
						jsonPath("$.compareByCategory.items.length()")
								.value(Category.values().length - 2))
				.andExpect(
						jsonPath("$.compareByCategory.items[?(@.categoryIndex == 2)].mySpentAmount")
								.value("200"))
				.andExpect(
						jsonPath(
										"$.compareByCategory.items[?(@.categoryIndex =="
												+ " 2)].averageSpentAmount")
								.value("200"))
				.andExpect(
						jsonPath("$.compareByCategory.items[?(@.categoryIndex == 4)].mySpentAmount")
								.value("300"))
				.andExpect(
						jsonPath(
										"$.compareByCategory.items[?(@.categoryIndex =="
												+ " 4)].averageSpentAmount")
								.value("100"));
	}

	@Test
	void getAnalysisOverview_afterNewExpenseInserted_returnsUpdatedMySpentAmount()
			throws Exception {
		AnalysisFixtureFactory.saveExpense(
				expenseRepository,
				accountBookId,
				Category.FOOD,
				AnalysisFixtureFactory.utcDateTime(2025, 12, 1, 10, 0),
				new BigDecimal("20.00"),
				new BigDecimal("20.00"),
				null,
				CurrencyCode.USD,
				CurrencyCode.KRW,
				"first");

		mockMvc.perform(
						get("/account-books/{accountBookId}/analysis", accountBookId)
								.with(jwtTestHelper.withJwtAuth(userId))
								.queryParam("year", "2025")
								.queryParam("month", "12")
								.queryParam("currencyType", "BASE"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.compareWithAverage.mySpentAmount").value("20"));

		AnalysisFixtureFactory.saveExpense(
				expenseRepository,
				accountBookId,
				Category.LIVING,
				AnalysisFixtureFactory.utcDateTime(2025, 12, 2, 10, 0),
				new BigDecimal("30.00"),
				new BigDecimal("30.00"),
				null,
				CurrencyCode.USD,
				CurrencyCode.KRW,
				"second");

		mockMvc.perform(
						get("/account-books/{accountBookId}/analysis", accountBookId)
								.with(jwtTestHelper.withJwtAuth(userId))
								.queryParam("year", "2025")
								.queryParam("month", "12")
								.queryParam("currencyType", "BASE"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.compareWithAverage.mySpentAmount").value("50"));
	}

	@Test
	void getAnalysisOverview_singleAccountBookMonthlyMockData_returnsOk() throws Exception {
		for (int day = 1; day <= 31; day++) {
			AnalysisFixtureFactory.saveExpense(
					expenseRepository,
					accountBookId,
					Category.FOOD,
					AnalysisFixtureFactory.utcDateTime(2025, 12, day, 10, 0),
					new BigDecimal("10.00"),
					new BigDecimal("10.00"),
					null,
					CurrencyCode.USD,
					CurrencyCode.KRW,
					"monthly-mock-" + day);
		}

		MvcResult result =
				mockMvc.perform(
								get("/account-books/{accountBookId}/analysis", accountBookId)
										.with(jwtTestHelper.withJwtAuth(userId))
										.queryParam("year", "2025")
										.queryParam("month", "12")
										.queryParam("currencyType", "BASE"))
						.andExpect(status().isOk())
						.andExpect(jsonPath("$.countryCode").value("KR"))
						.andExpect(jsonPath("$.compareWithAverage.mySpentAmount").value("310"))
						.andExpect(jsonPath("$.compareWithAverage.averageSpentAmount").value("0"))
						.andExpect(
								jsonPath("$.compareWithLastMonth.totalSpent.thisMonthToDate")
										.value("310"))
						.andExpect(
								jsonPath("$.compareWithLastMonth.thisMonthItem.length()").value(31))
						.andExpect(
								jsonPath(
												"$.compareByCategory.items[?(@.categoryIndex =="
														+ " 2)].mySpentAmount")
										.value("310"))
						.andReturn();

		System.out.println("analysis response body: " + result.getResponse().getContentAsString());
	}

	@Test
	void getAnalysisOverview_invalidMonthParam_returnsBadRequest() throws Exception {
		mockMvc.perform(
						get("/account-books/{accountBookId}/analysis", accountBookId)
								.with(jwtTestHelper.withJwtAuth(userId))
								.queryParam("year", "2025")
								.queryParam("month", "13")
								.queryParam("currencyType", "BASE"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value(ErrorCode.INVALID_INPUT_VALUE.getCode()));
	}

	@Test
	void getAnalysisOverview_futureMonthParam_returnsBadRequest() throws Exception {
		YearMonth nextMonth = YearMonth.now(ZoneId.of("America/New_York")).plusMonths(1);

		mockMvc.perform(
						get("/account-books/{accountBookId}/analysis", accountBookId)
								.with(jwtTestHelper.withJwtAuth(userId))
								.queryParam("year", String.valueOf(nextMonth.getYear()))
								.queryParam("month", String.valueOf(nextMonth.getMonthValue()))
								.queryParam("currencyType", "BASE"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value(ErrorCode.INVALID_INPUT_VALUE.getCode()));
	}

	@Test
	void getAnalysisOverview_peerDataUnavailable_returnsOverviewWithoutPeerComparison()
			throws Exception {
		AnalysisFixtureFactory.saveExpense(
				expenseRepository,
				accountBookId,
				Category.FOOD,
				AnalysisFixtureFactory.utcDateTime(2025, 12, 1, 10, 0),
				new BigDecimal("100.00"),
				new BigDecimal("100.00"),
				new BigDecimal("100.00"),
				CurrencyCode.USD,
				CurrencyCode.KRW,
				"food");

		mockMvc.perform(
						get("/account-books/{accountBookId}/analysis", accountBookId)
								.with(jwtTestHelper.withJwtAuth(userId))
								.queryParam("year", "2025")
								.queryParam("month", "12")
								.queryParam("currencyType", "BASE"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.compareWithAverage.averageSpentAmount").value("0"))
				.andExpect(jsonPath("$.compareWithAverage.spentAmountDiff").value("0"))
				.andExpect(
						jsonPath("$.compareByCategory.items.length()")
								.value(8)) // UNCLASSIFIED/INCOME 제외
				.andExpect(
						jsonPath("$.compareByCategory.items[?(@.categoryIndex == 2)].mySpentAmount")
								.value("100"))
				.andExpect(
						jsonPath(
										"$.compareByCategory.items[?(@.categoryIndex =="
												+ " 2)].averageSpentAmount")
								.value("0"));
	}

	@Test
	void getAnalysisOverview_localCurrency_returnsLocalCurrencyFormatted() throws Exception {
		AnalysisFixtureFactory.saveExpense(
				expenseRepository,
				accountBookId,
				Category.FOOD,
				AnalysisFixtureFactory.utcDateTime(2025, 12, 1, 10, 0),
				new BigDecimal("100.00"), // Local amount
				new BigDecimal("120.00"), // Base amount
				new BigDecimal("120.00"), // Calculated Base amount
				CurrencyCode.USD,
				CurrencyCode.KRW,
				"food");

		mockMvc.perform(
						get("/account-books/{accountBookId}/analysis", accountBookId)
								.with(jwtTestHelper.withJwtAuth(userId))
								.queryParam("year", "2025")
								.queryParam("month", "12")
								.queryParam("currencyType", "LOCAL"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.countryCode").value("US"))
				.andExpect(jsonPath("$.compareWithAverage.mySpentAmount").value("100"));
	}

	@Test
	void getAnalysisOverview_missingJwtCookie_returnsUnauthorized() throws Exception {
		mockMvc.perform(
						get("/account-books/{accountBookId}/analysis", accountBookId)
								.queryParam("year", "2025")
								.queryParam("month", "12")
								.queryParam("currencyType", "BASE"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value(ErrorCode.TOKEN_REQUIRED.getCode()));
	}
}
