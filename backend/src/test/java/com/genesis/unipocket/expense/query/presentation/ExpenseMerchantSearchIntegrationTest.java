package com.genesis.unipocket.expense.query.presentation;

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
class ExpenseMerchantSearchIntegrationTest {

	@Autowired private MockMvc mockMvc;
	@Autowired private JwtTestHelper jwtTestHelper;
	@Autowired private AccountBookCommandRepository accountBookRepository;
	@Autowired private UserCommandRepository userRepository;
	@Autowired private ExpenseRepository expenseRepository;

	private Long accountBookId;
	private UUID userId;

	@BeforeEach
	void setUp() {
		UserEntity user =
				userRepository.save(
						UserEntity.builder()
								.email("expense-search@unipocket.com")
								.name("expense-search")
								.mainBucketId(1L)
								.build());
		userId = user.getId();

		AccountBookEntity accountBook =
				accountBookRepository.save(
						AccountBookEntity.create(
								new AccountBookCreateArgs(
										user,
										"Expense Search Book",
										CountryCode.KR,
										CountryCode.KR,
										1,
										null,
										LocalDate.of(2026, 1, 1),
										LocalDate.of(2026, 12, 31))));
		accountBookId = accountBook.getId();

		ExpenseEntity expense =
				ExpenseEntity.manual(
						new ExpenseManualCreateArgs(
								accountBookId,
								"스타벅스 코리아",
								Category.FOOD,
								null,
								OffsetDateTime.of(2026, 2, 16, 10, 0, 0, 0, ZoneOffset.UTC),
								new BigDecimal("5500"),
								CurrencyCode.KRW,
								new BigDecimal("5500"),
								CurrencyCode.KRW,
								null,
								null,
								"",
								null,
								BigDecimal.ONE));
		expenseRepository.save(expense);
	}

	@Test
	@DisplayName("거래처명 prefix 검색 결과를 반환한다")
	void searchMerchantNames_returnsSuggestions() throws Exception {
		mockMvc.perform(
						get("/account-books/{accountBookId}/expenses/merchant-names", accountBookId)
								.with(jwtTestHelper.withJwtAuth(userId))
								.queryParam("q", "스타")
								.queryParam("limit", "10"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.merchantNames.length()").value(1))
				.andExpect(jsonPath("$.merchantNames[0]").value("스타벅스 코리아"));
	}

	@Test
	@DisplayName("거래처명 검색 - q가 빈값이면 전체 목록을 반환한다")
	void searchMerchantNames_blankQuery_returnsAll() throws Exception {
		mockMvc.perform(
						get("/account-books/{accountBookId}/expenses/merchant-names", accountBookId)
								.with(jwtTestHelper.withJwtAuth(userId))
								.queryParam("q", "")
								.queryParam("limit", "10"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.merchantNames.length()").value(1))
				.andExpect(jsonPath("$.merchantNames[0]").value("스타벅스 코리아"));
	}

	@Test
	@DisplayName("거래처명 검색 - q가 없으면 전체 목록을 반환한다")
	void searchMerchantNames_nullQuery_returnsAll() throws Exception {
		mockMvc.perform(
						get("/account-books/{accountBookId}/expenses/merchant-names", accountBookId)
								.with(jwtTestHelper.withJwtAuth(userId))
								.queryParam("limit", "10"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.merchantNames.length()").value(1))
				.andExpect(jsonPath("$.merchantNames[0]").value("스타벅스 코리아"));
	}
}
