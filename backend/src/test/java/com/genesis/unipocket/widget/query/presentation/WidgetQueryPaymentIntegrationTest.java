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
}
