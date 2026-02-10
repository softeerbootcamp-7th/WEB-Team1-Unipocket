package com.genesis.unipocket.expense.command.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.genesis.unipocket.TestcontainersConfiguration;
import com.genesis.unipocket.accountbook.command.persistence.entity.AccountBookCreateArgs;
import com.genesis.unipocket.accountbook.command.persistence.entity.AccountBookEntity;
import com.genesis.unipocket.accountbook.command.persistence.repository.AccountBookCommandRepository;
import com.genesis.unipocket.expense.command.persistence.entity.expense.ExpenseEntity;
import com.genesis.unipocket.expense.command.persistence.repository.ExpenseRepository;
import com.genesis.unipocket.expense.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CountryCode;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.support.JwtTestHelper;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
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

	@Autowired private AccountBookCommandRepository accountBookRepository;

	@Autowired private JwtTestHelper jwtTestHelper;

	private Long accountBookId;

	@BeforeEach
	void setUp() {
		// Create an AccountBook for testing
		AccountBookCreateArgs args =
				new AccountBookCreateArgs(
						"00000000-0000-0000-0000-000000000001",
						"Test Account Book",
						CountryCode.KR,
						CountryCode.KR,
						LocalDate.of(2026, 1, 1),
						LocalDate.of(2026, 12, 31));
		AccountBookEntity accountBook = AccountBookEntity.create(args);
		accountBook = accountBookRepository.save(accountBook);
		accountBookId = accountBook.getId();
	}

	@Test
	@DisplayName("지출내역 수기 작성 - 성공 테스트")
	void 수기지출_POST시_DB에_저장되어야한다() throws Exception {
		// given

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
								.with(jwtTestHelper.withJwtAuth())
								.contentType(MediaType.APPLICATION_JSON)
								.content(body))
				.andExpect(status().isCreated());

		// then
		assertThat(expenseRepository.findAll().size()).isEqualTo(1);

		ExpenseEntity saved = expenseRepository.findAll().get(0);
		assertThat(saved.getMerchantName()).isEqualTo("스타벅스");
		assertThat(saved.getLocalCurrency()).isEqualTo(CurrencyCode.KRW);
		assertThat(saved.getLocalAmount()).isEqualByComparingTo(BigDecimal.valueOf(10000.0));
		assertThat(saved.getCategory()).isEqualTo(Category.FOOD);

		assertThat(saved.getAccountBookId()).isEqualTo(accountBookId);
		assertThat(saved.getStandardAmount()).isEqualByComparingTo(BigDecimal.valueOf(10000.00));
	}

	@Test
	@DisplayName("지출내역 단건 조회 - 성공")
	void 지출내역_단건조회_성공() throws Exception {
		// given
		Long expenseId = createTestExpense();

		// when & then
		mockMvc.perform(
						get(
										"/api/account-books/{accountBookId}/expenses/{expenseId}",
										accountBookId,
										expenseId)
								.with(jwtTestHelper.withJwtAuth()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.expenseId").exists())
				.andExpect(jsonPath("$.merchantName").exists());
	}

	@Test
	@DisplayName("지출내역 목록 조회 - 성공")
	void 지출내역_목록조회_성공() throws Exception {
		// given - no expenses created, expecting empty list
		System.out.println("Test accountBookId: " + accountBookId);

		// when & then
		mockMvc.perform(
						get("/api/account-books/{accountBookId}/expenses", accountBookId)
								.with(jwtTestHelper.withJwtAuth())
								.param("page", "0")
								.param("size", "20")
								.param("sort", "expenseId,desc"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.expenses").isArray())
				.andExpect(jsonPath("$.totalCount").value(0));
	}

	@Test
	@DisplayName("지출내역 목록 조회 - 2개 조회 성공")
	void 지출내역_목록조회_2개_성공() throws Exception {
		// given - 2개의 지출내역 생성
		Long expenseId1 = createTestExpense();
		Long expenseId2 = createTestExpense();

		// when & then
		mockMvc.perform(
						get("/api/account-books/{accountBookId}/expenses", accountBookId)
								.with(jwtTestHelper.withJwtAuth())
								.param("page", "0")
								.param("size", "20")
								.param("sort", "expenseId,desc"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.expenses").isArray())
				.andExpect(jsonPath("$.expenses.length()").value(2))
				.andExpect(jsonPath("$.totalCount").value(2))
				.andExpect(jsonPath("$.expenses[0].expenseId").value(expenseId2))
				.andExpect(jsonPath("$.expenses[1].expenseId").value(expenseId1));
	}

	@Test
	@DisplayName("지출내역 수정 - 성공")
	void 지출내역_수정_성공() throws Exception {
		// given
		Long expenseId = createTestExpense();

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
								.with(jwtTestHelper.withJwtAuth())
								.contentType(MediaType.APPLICATION_JSON)
								.content(updateBody))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.displayMerchantName").value("스타벅스 수정"));
	}

	@Test
	@DisplayName("지출내역 삭제 - 성공")
	void 지출내역_삭제_성공() throws Exception {
		// given
		Long expenseId = createTestExpense();

		// when & then
		mockMvc.perform(
						delete(
										"/api/account-books/{accountBookId}/expenses/{expenseId}",
										accountBookId,
										expenseId)
								.with(jwtTestHelper.withJwtAuth()))
				.andExpect(status().isNoContent());
	}

	@Test
	@DisplayName("존재하지 않는 지출내역 조회 - 404")
	void 존재하지않는_지출내역_조회_실패() throws Exception {
		// given
		Long nonExistentExpenseId = 99999L;

		// when & then
		mockMvc.perform(
						get(
										"/api/account-books/{accountBookId}/expenses/{expenseId}",
										accountBookId,
										nonExistentExpenseId)
								.with(jwtTestHelper.withJwtAuth()))
				.andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("지출내역 필터링 - 카테고리 필터")
	void 지출내역_카테고리_필터링_성공() throws Exception {
		// given - 다양한 카테고리의 지출내역 생성
		createTestExpenseWithDetails("스타벅스", "FOOD", "2026-02-04T12:30:00", 10000.0, null);
		createTestExpenseWithDetails("택시", "TRANSPORT", "2026-02-04T14:00:00", 15000.0, null);
		createTestExpenseWithDetails("맥도날드", "FOOD", "2026-02-04T18:00:00", 8000.0, null);

		// when & then - FOOD 카테고리만 조회
		mockMvc.perform(
						get("/api/account-books/{accountBookId}/expenses", accountBookId)
								.with(jwtTestHelper.withJwtAuth())
								.param("page", "0")
								.param("size", "20")
								.param("sort", "expenseId,desc")
								.param("category", "FOOD"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.expenses").isArray())
				.andExpect(jsonPath("$.expenses.length()").value(2))
				.andExpect(jsonPath("$.totalCount").value(2));
	}

	@Test
	@DisplayName("지출내역 필터링 - 거래처명 필터")
	void 지출내역_거래처명_필터링_성공() throws Exception {
		// given
		createTestExpenseWithDetails("스타벅스 강남점", "FOOD", "2026-02-04T12:30:00", 10000.0, null);
		createTestExpenseWithDetails("맥도날드", "FOOD", "2026-02-04T14:00:00", 8000.0, null);
		createTestExpenseWithDetails("스타벅스 역삼점", "FOOD", "2026-02-04T18:00:00", 12000.0, null);

		// when & then - "스타벅스"로 검색
		mockMvc.perform(
						get("/api/account-books/{accountBookId}/expenses", accountBookId)
								.with(jwtTestHelper.withJwtAuth())
								.param("page", "0")
								.param("size", "20")
								.param("sort", "expenseId,desc")
								.param("merchantName", "스타벅스"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.expenses").isArray())
				.andExpect(jsonPath("$.expenses.length()").value(2))
				.andExpect(jsonPath("$.totalCount").value(2));
	}

	@Test
	@DisplayName("지출내역 필터링 - 날짜 범위 필터")
	void 지출내역_날짜범위_필터링_성공() throws Exception {
		// given - 다양한 날짜의 지출내역 생성
		createTestExpenseWithDetails("스타벅스", "FOOD", "2026-02-01T12:00:00", 10000.0, null);
		createTestExpenseWithDetails("맥도날드", "FOOD", "2026-02-05T12:00:00", 8000.0, null);
		createTestExpenseWithDetails("택시", "TRANSPORT", "2026-02-10T12:00:00", 15000.0, null);

		// when & then - 2026-02-03 ~ 2026-02-07 범위 조회
		mockMvc.perform(
						get("/api/account-books/{accountBookId}/expenses", accountBookId)
								.with(jwtTestHelper.withJwtAuth())
								.param("page", "0")
								.param("size", "20")
								.param("sort", "expenseId,desc")
								.param("startDate", "2026-02-03T00:00:00")
								.param("endDate", "2026-02-07T23:59:59"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.expenses").isArray())
				.andExpect(jsonPath("$.expenses.length()").value(1))
				.andExpect(jsonPath("$.totalCount").value(1));
	}

	@Test
	@DisplayName("지출내역 필터링 - 금액 범위 필터")
	void 지출내역_금액범위_필터링_성공() throws Exception {
		// given - 다양한 금액의 지출내역 생성
		createTestExpenseWithDetails("저렴한 식당", "FOOD", "2026-02-04T12:00:00", 5000.0, null);
		createTestExpenseWithDetails("중간 식당", "FOOD", "2026-02-04T13:00:00", 15000.0, null);
		createTestExpenseWithDetails("고급 식당", "FOOD", "2026-02-04T14:00:00", 50000.0, null);

		// when & then - 10000 ~ 30000 범위 조회
		mockMvc.perform(
						get("/api/account-books/{accountBookId}/expenses", accountBookId)
								.with(jwtTestHelper.withJwtAuth())
								.param("page", "0")
								.param("size", "20")
								.param("sort", "expenseId,desc")
								.param("minAmount", "10000")
								.param("maxAmount", "30000"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.expenses").isArray())
				.andExpect(jsonPath("$.expenses.length()").value(1))
				.andExpect(jsonPath("$.totalCount").value(1));
	}

	@Test
	@DisplayName("지출내역 필터링 - 복합 조건 필터")
	void 지출내역_복합조건_필터링_성공() throws Exception {
		// given - 다양한 조건의 지출내역 생성
		createTestExpenseWithDetails("스타벅스", "FOOD", "2026-02-04T12:00:00", 10000.0, null);
		createTestExpenseWithDetails("스타벅스", "FOOD", "2026-02-04T13:00:00", 25000.0, null);
		createTestExpenseWithDetails("맥도날드", "FOOD", "2026-02-04T14:00:00", 8000.0, null);
		createTestExpenseWithDetails("택시", "TRANSPORT", "2026-02-04T15:00:00", 15000.0, null);

		// when & then - 카테고리=FOOD, 거래처명=스타벅스, 금액 8000~15000
		mockMvc.perform(
						get("/api/account-books/{accountBookId}/expenses", accountBookId)
								.with(jwtTestHelper.withJwtAuth())
								.param("page", "0")
								.param("size", "20")
								.param("sort", "expenseId,desc")
								.param("category", "FOOD")
								.param("merchantName", "스타벅스")
								.param("minAmount", "8000")
								.param("maxAmount", "15000"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.expenses").isArray())
				.andExpect(jsonPath("$.expenses.length()").value(1))
				.andExpect(jsonPath("$.totalCount").value(1))
				.andExpect(jsonPath("$.expenses[0].displayMerchantName").value("스타벅스"));
	}

	@Test
	@DisplayName("지출내역 필터링 - travelId 필터")
	void 지출내역_travelId_필터링_성공() throws Exception {
		// given - travelId가 있는 것과 없는 지출내역 생성
		createTestExpenseWithDetails("여행 중 식사", "FOOD", "2026-02-04T12:00:00", 10000.0, 100L);
		createTestExpenseWithDetails("여행 중 택시", "TRANSPORT", "2026-02-04T13:00:00", 15000.0, 100L);
		createTestExpenseWithDetails("일상 식사", "FOOD", "2026-02-04T14:00:00", 8000.0, null);

		// when & then - travelId=100인 지출내역만 조회
		mockMvc.perform(
						get("/api/account-books/{accountBookId}/expenses", accountBookId)
								.with(jwtTestHelper.withJwtAuth())
								.param("page", "0")
								.param("size", "20")
								.param("sort", "expenseId,desc")
								.param("travelId", "100"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.expenses").isArray())
				.andExpect(jsonPath("$.expenses.length()").value(2))
				.andExpect(jsonPath("$.totalCount").value(2));
	}

	private Long createTestExpense() throws Exception {
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

		mockMvc.perform(
						post("/api/account-books/{accountBookId}/expenses/manual", accountBookId)
								.with(jwtTestHelper.withJwtAuth())
								.contentType(MediaType.APPLICATION_JSON)
								.content(body))
				.andExpect(status().isCreated());

		// Return the ID of the created expense
		return expenseRepository
				.findAll()
				.get(expenseRepository.findAll().size() - 1)
				.getExpenseId();
	}

	private Long createTestExpenseWithDetails(
			String merchantName, String category, String occurredAt, Double amount, Long travelId)
			throws Exception {
		String travelIdJson = travelId != null ? String.valueOf(travelId) : "null";
		String body =
				String.format(
						"""
			{
			"merchantName": "%s",
			"category": "%s",
			"paymentMethod": "CARD",
			"occurredAt": "%s",
			"localCurrencyAmount": %.1f,
			"localCurrencyCode": "KRW",
			"memo": "테스트",
			"travelId": %s
			}
			""",
						merchantName, category, occurredAt, amount, travelIdJson);

		mockMvc.perform(
						post("/api/account-books/{accountBookId}/expenses/manual", accountBookId)
								.with(jwtTestHelper.withJwtAuth())
								.contentType(MediaType.APPLICATION_JSON)
								.content(body))
				.andExpect(status().isCreated());

		return expenseRepository
				.findAll()
				.get(expenseRepository.findAll().size() - 1)
				.getExpenseId();
	}
}
