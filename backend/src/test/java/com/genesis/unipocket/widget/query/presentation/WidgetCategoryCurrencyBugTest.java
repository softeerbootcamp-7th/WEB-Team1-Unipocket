package com.genesis.unipocket.widget.query.presentation;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
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

/**
 * CATEGORY 위젯 BASE 통화 버그 재현 테스트.
 *
 * 가게부: local=FR(EUR), base=KR(KRW)
 * - 쇼핑 1000 EUR → calculatedBase 1,500,000 KRW (정상)
 * - 식비 450 EUR → baseCurrencyAmount = 450 (환율 미적용, EUR 그대로)
 */
@AutoConfigureMockMvc
@SpringBootTest
@ActiveProfiles("test-it")
@Import(TestcontainersConfiguration.class)
@Transactional
@Tag("integration")
class WidgetCategoryCurrencyBugTest {

	@Autowired private MockMvc mockMvc;
	@Autowired private JwtTestHelper jwtTestHelper;
	@Autowired private UserCommandRepository userRepository;
	@Autowired private AccountBookCommandRepository accountBookRepository;
	@Autowired private ExpenseRepository expenseRepository;

	private UUID userId;
	private Long accountBookId;

	@BeforeEach
	void setUp() {
		UserEntity user =
				userRepository.save(
						UserEntity.builder()
								.email("category-bug@unipocket.com")
								.name("category-bug")
								.mainBucketId(1L)
								.build());
		userId = user.getId();

		// 가계부: local=FR(EUR), base=KR(KRW)
		AccountBookEntity accountBook =
				accountBookRepository.save(
						AccountBookEntity.create(
								new AccountBookCreateArgs(
										user,
										"Category Bug Book",
										CountryCode.FR, // local = FR (EUR)
										CountryCode.KR, // base = KR (KR → KRW)
										1,
										null,
										LocalDate.of(2026, 1, 1),
										LocalDate.of(2026, 12, 31))));
		accountBookId = accountBook.getId();

		// 정상 케이스: 쇼핑 1000 EUR → calculated 1,500,000 KRW
		expenseRepository.save(
				ExpenseEntity.manual(
						new ExpenseManualCreateArgs(
								accountBookId,
								"정상 쇼핑",
								Category.SHOPPING,
								null,
								OffsetDateTime.of(2026, 2, 16, 10, 0, 0, 0, ZoneOffset.UTC),
								new BigDecimal("1000"), // local = 1000 EUR
								CurrencyCode.EUR,
								null, // base = null
								CurrencyCode.KRW,
								new BigDecimal("1500000"), // calculated = 1,500,000 KRW
								CurrencyCode.KRW,
								"",
								null,
								new BigDecimal("1500"))));

		// 버그 시나리오: 식비 450 EUR, base=450 (EUR 금액 그대로, 환율 미적용)
		// COALESCE(450, null) = 450 → KRW 총합에 EUR 숫자 450이 추가됨
		expenseRepository.save(
				ExpenseEntity.manual(
						new ExpenseManualCreateArgs(
								accountBookId,
								"식비-환율미적용",
								Category.FOOD,
								null,
								OffsetDateTime.of(2026, 2, 16, 11, 0, 0, 0, ZoneOffset.UTC),
								new BigDecimal("450"), // local = 450 EUR
								CurrencyCode.EUR,
								new BigDecimal("450"), // base = 450 (EUR 값 그대로!)
								CurrencyCode.KRW,
								null,
								null,
								"",
								null,
								BigDecimal.ONE)));
	}

	@Test
	@DisplayName("CATEGORY 위젯 LOCAL(EUR) 뷰 - 모든 카테고리 표시")
	void categoryWidget_localView_showsAllCategories() throws Exception {
		mockMvc.perform(
						get("/account-books/{id}/widget", accountBookId)
								.with(jwtTestHelper.withJwtAuth(userId))
								.queryParam("widgetType", "CATEGORY")
								.queryParam("currencyType", "LOCAL"))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalAmount").exists())
				.andExpect(jsonPath("$.items.length()").value(2));
	}

	@Test
	@DisplayName("CATEGORY 위젯 BASE(KRW) 뷰 - 식비 금액 확인")
	void categoryWidget_baseView_foodAmountCheck() throws Exception {
		// 정상이라면 식비 = 450 * 1500 = 675,000 KRW 여야 하지만
		// 실제로는 450이 그대로 나올 것 (버그)
		mockMvc.perform(
						get("/account-books/{id}/widget", accountBookId)
								.with(jwtTestHelper.withJwtAuth(userId))
								.queryParam("widgetType", "CATEGORY")
								.queryParam("currencyType", "BASE"))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalAmount").exists())
				.andExpect(jsonPath("$.items").isArray());
	}
}
