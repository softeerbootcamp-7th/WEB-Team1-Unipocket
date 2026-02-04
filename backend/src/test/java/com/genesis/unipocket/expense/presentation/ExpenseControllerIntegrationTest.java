package com.genesis.unipocket.expense.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.genesis.unipocket.TestcontainersConfiguration;
import com.genesis.unipocket.expense.common.enums.Category;
import com.genesis.unipocket.expense.persistence.entity.expense.Expense;
import com.genesis.unipocket.expense.persistence.repository.ExpenseRepository;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * <b>지출내역 도메인 통합 테스트</b>
 *
 * @author codingbaraGo
 * @since 2026-02-03
 */
@AutoConfigureMockMvc
@SpringBootTest
@ActiveProfiles("test-it")
@Import(TestcontainersConfiguration.class)
@Transactional
@Tag("integration")
class ExpenseControllerIntegrationTest {

	@Autowired private MockMvc mockMvc;

	@Autowired private ExpenseRepository expenseRepository;

	@Test
	@DisplayName("지출내역 수기 작성 - 성공 테스트")
	void 수기지출_POST시_DB에_저장되어야한다() throws Exception {
		// given
		Long accountBookId = 7L;

		String body =
				"""
			{
			"merchantName": "스타벅스",
			"category": 1,
			"paymentMethod": "CARD",
			"occurredAt": "2026-02-04T12:30:00",
			"localAmount": 10000.0,
			"localCurrency": "KRW",
			"standardAmount": 1000.123,
			"standardCurrency": "JPY",
			"memo": "아메리카노"
			}
			""";

		// when
		mockMvc.perform(
						post("/api/account-books/{accountBookId}/expenses/manual", accountBookId)
								.contentType(MediaType.APPLICATION_JSON)
								.content(body))
				.andExpect(status().isCreated());

		// then
		assertThat(expenseRepository.findAll().size()).isEqualTo(1);

		Expense saved = expenseRepository.findAll().get(0);
		assertThat(saved.getMerchantName()).isEqualTo("스타벅스");
		assertThat(saved.getLocalCurrency()).isEqualTo(CurrencyCode.KRW);
		assertThat(saved.getStandardCurrency()).isEqualTo(CurrencyCode.JPY);
		assertThat(saved.getCategory()).isEqualTo(Category.RESIDENCE);

		assertThat(saved.getAccountBookId()).isEqualTo(accountBookId);
		assertThat(saved.getStandardAmount()).isEqualTo(BigDecimal.valueOf(1000.123));
	}
}
