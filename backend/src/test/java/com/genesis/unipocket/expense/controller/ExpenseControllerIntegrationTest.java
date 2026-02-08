package com.genesis.unipocket.expense.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.genesis.unipocket.TestcontainersConfiguration;
import com.genesis.unipocket.expense.common.enums.Category;
import com.genesis.unipocket.expense.persistence.entity.expense.ExpenseEntity;
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
			"category": "FOOD",
			"paymentMethod": "CARD",
			"occurredAt": "2026-02-04T12:30:00",
			"localCurrencyAmount": 10000.0,
			"localCurrencyCode": "KRW",
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

		ExpenseEntity saved = expenseRepository.findAll().get(0);
		assertThat(saved.getMerchantName()).isEqualTo("스타벅스");
		assertThat(saved.getLocalCurrency()).isEqualTo(CurrencyCode.KRW);
		assertThat(saved.getLocalAmount()).isEqualTo(BigDecimal.valueOf(10000.0));
		assertThat(saved.getCategory()).isEqualTo(Category.FOOD);

		assertThat(saved.getAccountBookId()).isEqualTo(accountBookId);
		assertThat(saved.getStandardAmount()).isEqualTo(BigDecimal.valueOf(10)); // TODO 값 확인
	}

	@Test
	@DisplayName("지출내역 단건 조회 - 성공")
	void 지출내역_단건조회_성공() throws Exception {
		// given
		Long accountBookId = 7L;
		// 지출 데이터가 이미 생성되어 있다고 가정 (실제로는 fixture 필요)

		// when & then
		mockMvc.perform(
						get(
								"/api/account-books/{accountBookId}/expenses/{expenseId}",
								accountBookId,
								1L))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.expenseId").exists())
				.andExpect(jsonPath("$.merchantName").exists());
	}

	@Test
	@DisplayName("지출내역 목록 조회 - 성공")
	void 지출내역_목록조회_성공() throws Exception {
		// given
		Long accountBookId = 7L;

		// when & then
		mockMvc.perform(
						get("/api/account-books/{accountBookId}/expenses", accountBookId)
								.param("page", "0")
								.param("size", "20"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.expenses").isArray())
				.andExpect(jsonPath("$.totalCount").exists());
	}

	@Test
	@DisplayName("지출내역 수정 - 성공")
	void 지출내역_수정_성공() throws Exception {
		// given
		Long accountBookId = 7L;
		Long expenseId = 1L; // 이미 존재하는 지출 ID

		String updateBody =
				"""
			{
			"merchantName": "스타벅스 수정",
			"category": "FOOD",
			"paymentMethod": "CASH",
			"occurredAt": "2026-02-04T12:30:00",
			"localCurrencyAmount": 15000.0,
			"localCurrencyCode": "KRW",
			"memo": "수정된 메모",
			"travelId": null
			}
			""";

		// when & then
		mockMvc.perform(
						put(
										"/api/account-books/{accountBookId}/expenses/{expenseId}",
										accountBookId,
										expenseId)
								.contentType(MediaType.APPLICATION_JSON)
								.content(updateBody))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.merchantName").value("스타벅스 수정"));
	}

	@Test
	@DisplayName("지출내역 삭제 - 성공")
	void 지출내역_삭제_성공() throws Exception {
		// given
		Long accountBookId = 7L;
		Long expenseId = 1L;

		// when & then
		mockMvc.perform(
						delete(
								"/api/account-books/{accountBookId}/expenses/{expenseId}",
								accountBookId,
								expenseId))
				.andExpect(status().isNoContent());
	}

	@Test
	@DisplayName("존재하지 않는 지출내역 조회 - 404")
	void 존재하지않는_지출내역_조회_실패() throws Exception {
		// given
		Long accountBookId = 7L;
		Long nonExistentExpenseId = 99999L;

		// when & then
		mockMvc.perform(
						get(
								"/api/account-books/{accountBookId}/expenses/{expenseId}",
								accountBookId,
								nonExistentExpenseId))
				.andExpect(status().isNotFound());
	}
}
