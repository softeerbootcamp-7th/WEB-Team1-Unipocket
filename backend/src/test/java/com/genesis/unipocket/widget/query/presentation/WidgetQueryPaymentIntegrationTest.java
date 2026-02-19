package com.genesis.unipocket.widget.query.presentation;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.genesis.unipocket.TestcontainersConfiguration;
import com.genesis.unipocket.accountbook.command.persistence.entity.AccountBookCreateArgs;
import com.genesis.unipocket.accountbook.command.persistence.entity.AccountBookEntity;
import com.genesis.unipocket.accountbook.command.persistence.repository.AccountBookCommandRepository;
import com.genesis.unipocket.auth.support.JwtTestHelper;
import com.genesis.unipocket.expense.command.persistence.entity.ExpenseEntity;
import com.genesis.unipocket.expense.command.persistence.entity.dto.ExpenseManualCreateArgs;
import com.genesis.unipocket.expense.command.persistence.repository.ExpenseRepository;
import com.genesis.unipocket.global.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CountryCode;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.travel.command.persistence.entity.Travel;
import com.genesis.unipocket.travel.command.persistence.repository.TravelCommandRepository;
import com.genesis.unipocket.user.command.persistence.entity.UserEntity;
import com.genesis.unipocket.user.command.persistence.repository.UserCommandRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@AutoConfigureMockMvc
@SpringBootTest
@ActiveProfiles("test-it")
@Import(TestcontainersConfiguration.class)
@Transactional
@Tag("integration")
class WidgetQueryPaymentIntegrationTest {

	@Autowired private MockMvc mockMvc;
	@Autowired private JwtTestHelper jwtTestHelper;
	@Autowired private UserCommandRepository userRepository;
	@Autowired private AccountBookCommandRepository accountBookRepository;
	@Autowired private ExpenseRepository expenseRepository;
	@Autowired private TravelCommandRepository travelCommandRepository;

	private UUID userId;
	private Long accountBookId;

	@BeforeEach
	void setUp() {
		UserEntity user =
				userRepository.save(
						UserEntity.builder()
								.email("widget-payment@unipocket.com")
								.name("widget-payment")
								.mainBucketId(1L)
								.build());
		userId = user.getId();

		AccountBookEntity accountBook =
				accountBookRepository.save(
						AccountBookEntity.create(
								new AccountBookCreateArgs(
										user,
										"Widget Payment Book",
										CountryCode.KR,
										CountryCode.KR,
										1,
										null,
										LocalDate.of(2026, 1, 1),
										LocalDate.of(2026, 12, 31))));
		accountBookId = accountBook.getId();

		expenseRepository.save(
				ExpenseEntity.manual(
						new ExpenseManualCreateArgs(
								accountBookId,
								"무카드 결제",
								Category.FOOD,
								null,
								OffsetDateTime.of(2026, 2, 16, 10, 0, 0, 0, ZoneOffset.UTC),
								new BigDecimal("1000"),
								CurrencyCode.KRW,
								new BigDecimal("1000"),
								CurrencyCode.KRW,
								null,
								null,
								"",
								null,
								BigDecimal.ONE)));

		expenseRepository.save(
				ExpenseEntity.manual(
						new ExpenseManualCreateArgs(
								accountBookId,
								"카드 결제",
								Category.FOOD,
								1L,
								OffsetDateTime.of(2026, 2, 16, 11, 0, 0, 0, ZoneOffset.UTC),
								new BigDecimal("2000"),
								CurrencyCode.KRW,
								new BigDecimal("2000"),
								CurrencyCode.KRW,
								null,
								null,
								"",
								null,
								BigDecimal.ONE)));

		// base가 null이고 calculated만 있는 케이스
		expenseRepository.save(
				ExpenseEntity.manual(
						new ExpenseManualCreateArgs(
								accountBookId,
								"계산기준 결제",
								Category.FOOD,
								2L,
								OffsetDateTime.of(2026, 2, 16, 12, 0, 0, 0, ZoneOffset.UTC),
								new BigDecimal("1500"),
								CurrencyCode.KRW,
								null,
								CurrencyCode.KRW,
								new BigDecimal("1500"),
								CurrencyCode.KRW,
								"",
								null,
								BigDecimal.ONE)));

		// 소비 집계 제외 대상(INCOME)
		expenseRepository.save(
				ExpenseEntity.manual(
						new ExpenseManualCreateArgs(
								accountBookId,
								"수입",
								Category.INCOME,
								3L,
								OffsetDateTime.of(2026, 2, 16, 13, 0, 0, 0, ZoneOffset.UTC),
								new BigDecimal("9999"),
								CurrencyCode.KRW,
								new BigDecimal("9999"),
								CurrencyCode.KRW,
								null,
								null,
								"",
								null,
								BigDecimal.ONE)));
	}

	@Test
	@DisplayName("PAYMENT 위젯 조회 시 500 없이 결제수단 통계를 반환한다")
	void paymentWidgetQuery_success() throws Exception {
		mockMvc.perform(
						get("/account-books/{accountBookId}/widget", accountBookId)
								.with(jwtTestHelper.withJwtAuth(userId))
								.queryParam("widgetType", "PAYMENT")
								.queryParam("currencyType", "BASE")
								.queryParam("period", "ALL"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.paymentMethodCount").value(2))
				.andExpect(jsonPath("$.items.length()").value(2));
	}

	@Test
	@DisplayName("BUDGET BASE 집계는 base가 null이면 calculated를 fallback으로 사용한다")
	void budgetWidgetQuery_baseFallbackToCalculated_success() throws Exception {
		mockMvc.perform(
						get("/account-books/{accountBookId}/widget", accountBookId)
								.with(jwtTestHelper.withJwtAuth(userId))
								.queryParam("widgetType", "BUDGET")
								.queryParam("currencyType", "BASE")
								.queryParam("period", "ALL"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.baseSpentAmount").value("4500"));
	}

	@Test
	@DisplayName("소비 집계 위젯은 INCOME 카테고리를 제외한다")
	void budgetWidgetQuery_excludesIncome_success() throws Exception {
		mockMvc.perform(
						get("/account-books/{accountBookId}/widget", accountBookId)
								.with(jwtTestHelper.withJwtAuth(userId))
								.queryParam("widgetType", "BUDGET")
								.queryParam("currencyType", "BASE")
								.queryParam("period", "ALL"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.baseSpentAmount").value("4500"))
				.andExpect(jsonPath("$.localSpentAmount").value("4500"));
	}

	@Test
	@DisplayName("CATEGORY BASE 집계는 calculated fallback을 사용하고 INCOME을 제외한다")
	void categoryWidgetQuery_baseFallbackAndExcludeIncome_success() throws Exception {
		mockMvc.perform(
						get("/account-books/{accountBookId}/widget", accountBookId)
								.with(jwtTestHelper.withJwtAuth(userId))
								.queryParam("widgetType", "CATEGORY")
								.queryParam("currencyType", "BASE")
								.queryParam("period", "ALL"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalAmount").value("4500"))
				.andExpect(jsonPath("$.items.length()").value(1));
	}

	@Test
	@DisplayName("여행 위젯 조회 시 travel/accountBook 소속이 다르면 404를 반환한다")
	void travelWidgetQuery_scopeMismatch_returnsNotFound() throws Exception {
		UserEntity otherUser =
				userRepository.save(
						UserEntity.builder()
								.email("widget-payment-other@unipocket.com")
								.name("widget-payment-other")
								.mainBucketId(1L)
								.build());

		AccountBookEntity otherAccountBook =
				accountBookRepository.save(
						AccountBookEntity.create(
								new AccountBookCreateArgs(
										otherUser,
										"Other Payment Book",
										CountryCode.KR,
										CountryCode.KR,
										1,
										null,
										LocalDate.of(2026, 1, 1),
										LocalDate.of(2026, 12, 31))));

		Travel otherTravel =
				travelCommandRepository.save(
						Travel.builder()
								.accountBookId(otherAccountBook.getId())
								.travelPlaceName("Other Travel")
								.startDate(LocalDate.of(2026, 2, 1))
								.endDate(LocalDate.of(2026, 2, 3))
								.build());

		mockMvc.perform(
						get(
										"/account-books/{accountBookId}/travels/{travelId}/widget",
										accountBookId,
										otherTravel.getId())
								.with(jwtTestHelper.withJwtAuth(userId))
								.queryParam("widgetType", "BUDGET")
								.queryParam("currencyType", "BASE")
								.queryParam("period", "ALL"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("404_TRAVEL_NOT_FOUND"));
	}
}
