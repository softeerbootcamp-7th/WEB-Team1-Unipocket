package com.genesis.unipocket.expense.command.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.genesis.unipocket.TestcontainersConfiguration;
import com.genesis.unipocket.accountbook.command.persistence.entity.AccountBookCreateArgs;
import com.genesis.unipocket.accountbook.command.persistence.entity.AccountBookEntity;
import com.genesis.unipocket.accountbook.command.persistence.repository.AccountBookCommandRepository;
import com.genesis.unipocket.auth.support.JwtTestHelper;
import com.genesis.unipocket.expense.command.persistence.entity.ExpenseEntity;
import com.genesis.unipocket.expense.command.persistence.repository.ExpenseRepository;
import com.genesis.unipocket.global.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CountryCode;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.user.command.persistence.entity.UserCardEntity;
import com.genesis.unipocket.user.command.persistence.entity.UserEntity;
import com.genesis.unipocket.user.command.persistence.repository.UserCardCommandRepository;
import com.genesis.unipocket.user.command.persistence.repository.UserCommandRepository;
import com.genesis.unipocket.user.common.enums.CardCompany;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
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
	@Autowired private UserCommandRepository userRepository;
	@Autowired private UserCardCommandRepository userCardRepository;

	@Autowired private JwtTestHelper jwtTestHelper;

	private Long accountBookId;
	private UUID userId;

	@BeforeEach
	void setUp() {
		// Create an AccountBook for testing
		UserEntity user =
				userRepository.save(
						UserEntity.builder()
								.email("test-user@unipocket.com")
								.name("test-user")
								.mainBucketId(1L)
								.build());
		userId = user.getId();

		AccountBookCreateArgs args =
				new AccountBookCreateArgs(
						user,
						"Test Account Book",
						CountryCode.KR,
						CountryCode.KR,
						1,
						null,
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
			"category": 2,
			"userCardId": null,
			"occurredAt": "2026-02-04T12:30:00Z",
			"localCurrencyAmount": 10000.0,
			"localCurrencyCode": "KRW",
			"memo": "아메리카노"
			}
			""";

		// when
		mockMvc.perform(
						post("/account-books/{accountBookId}/expenses/manual", accountBookId)
								.with(jwtTestHelper.withJwtAuth(userId))
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
		assertThat(saved.getBaseAmount()).isNull();
		assertThat(saved.getDisplayBaseAmount()).isEqualByComparingTo(BigDecimal.valueOf(10000.00));
	}

	@Test
	@DisplayName("지출내역 단건 조회 - 성공")
	void 지출내역_단건조회_성공() throws Exception {
		// given
		Long expenseId = createTestExpense();

		// when & then
		mockMvc.perform(
						get(
										"/account-books/{accountBookId}/expenses/{expenseId}",
										accountBookId,
										expenseId)
								.with(jwtTestHelper.withJwtAuth(userId)))
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
						get("/account-books/{accountBookId}/expenses", accountBookId)
								.with(jwtTestHelper.withJwtAuth(userId))
								.param("page", "0")
								.param("size", "20")
								.param("sort", "occurredAt,desc"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.expenses").isArray())
				.andExpect(jsonPath("$.totalCount").value(0));
	}

	@Test
	@DisplayName("지출내역 목록 조회 - accountBookId 타입 불일치 시 400")
	void 지출내역_목록조회_accountBookId_타입불일치_실패() throws Exception {
		mockMvc.perform(
						get("/account-books/{accountBookId}/expenses", "undefined")
								.with(jwtTestHelper.withJwtAuth(userId))
								.param("page", "0")
								.param("size", "10"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("400_INVALID_INPUT_VALUE"));
	}

	@Test
	@DisplayName("지출내역 목록 조회 - 2개 조회 성공")
	void 지출내역_목록조회_2개_성공() throws Exception {
		// given - 2개의 지출내역 생성
		Long expenseId1 = createTestExpense();
		Long expenseId2 = createTestExpense();

		// when & then
		mockMvc.perform(
						get("/account-books/{accountBookId}/expenses", accountBookId)
								.with(jwtTestHelper.withJwtAuth(userId))
								.param("page", "0")
								.param("size", "20")
								.param("sort", "occurredAt,desc"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.expenses").isArray())
				.andExpect(jsonPath("$.expenses.length()").value(2))
				.andExpect(jsonPath("$.totalCount").value(2));
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
			"category": 2,
			"userCardId": null,
			"occurredAt": "2026-02-04T12:30:00Z",
			"localCurrencyAmount": 15000.0,
			"localCurrencyCode": "KRW",
			"memo": "수정된 메모",
			"travelId": null
			}
			""";

		// when & then
		mockMvc.perform(
						put(
										"/account-books/{accountBookId}/expenses/{expenseId}",
										accountBookId,
										expenseId)
								.with(jwtTestHelper.withJwtAuth(userId))
								.contentType(MediaType.APPLICATION_JSON)
								.content(updateBody))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.displayMerchantName").value("스타벅스 수정"));
	}

	@Test
	@DisplayName("지출내역 일괄 수정 - 성공")
	void 지출내역_일괄수정_성공() throws Exception {
		Long expenseId1 = createTestExpense();
		Long expenseId2 = createTestExpense();

		String updateBody =
				"""
			{
			"items": [
				{
				"expenseId": %d,
				"merchantName": "일괄수정-1",
				"category": 2,
				"userCardId": null,
				"occurredAt": "2026-02-04T12:30:00Z",
				"localCurrencyAmount": 11111.0,
				"localCurrencyCode": "KRW",
				"memo": "bulk-1",
				"travelId": null
				},
				{
				"expenseId": %d,
				"merchantName": "일괄수정-2",
				"category": 3,
				"userCardId": null,
				"occurredAt": "2026-02-05T12:30:00Z",
				"localCurrencyAmount": 22222.0,
				"localCurrencyCode": "KRW",
				"memo": "bulk-2",
				"travelId": null
				}
			]
			}
			"""
						.formatted(expenseId1, expenseId2);

		mockMvc.perform(
						put("/account-books/{accountBookId}/expenses/bulk", accountBookId)
								.with(jwtTestHelper.withJwtAuth(userId))
								.contentType(MediaType.APPLICATION_JSON)
								.content(updateBody))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalUpdated").value(2))
				.andExpect(jsonPath("$.items").isArray())
				.andExpect(jsonPath("$.items.length()").value(2))
				.andExpect(jsonPath("$.items[0].displayMerchantName").value("일괄수정-1"))
				.andExpect(jsonPath("$.items[1].displayMerchantName").value("일괄수정-2"));
	}

	@Test
	@DisplayName("지출내역 삭제 - 성공")
	void 지출내역_삭제_성공() throws Exception {
		// given
		Long expenseId = createTestExpense();

		// when & then
		mockMvc.perform(
						delete(
										"/account-books/{accountBookId}/expenses/{expenseId}",
										accountBookId,
										expenseId)
								.with(jwtTestHelper.withJwtAuth(userId)))
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
										"/account-books/{accountBookId}/expenses/{expenseId}",
										accountBookId,
										nonExistentExpenseId)
								.with(jwtTestHelper.withJwtAuth(userId)))
				.andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("지출내역 필터링 - 카테고리 필터")
	void 지출내역_카테고리_필터링_성공() throws Exception {
		// given - 다양한 카테고리의 지출내역 생성
		createTestExpenseWithDetails("스타벅스", 2, "2026-02-04T12:30:00", 10000.0, null);
		createTestExpenseWithDetails("택시", 3, "2026-02-04T14:00:00", 15000.0, null);
		createTestExpenseWithDetails("맥도날드", 2, "2026-02-04T18:00:00", 8000.0, null);

		// when & then - FOOD 카테고리만 조회
		mockMvc.perform(
						get("/account-books/{accountBookId}/expenses", accountBookId)
								.with(jwtTestHelper.withJwtAuth(userId))
								.param("page", "0")
								.param("size", "20")
								.param("sort", "occurredAt,desc")
								.param("category", "2"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.expenses").isArray())
				.andExpect(jsonPath("$.expenses.length()").value(2))
				.andExpect(jsonPath("$.totalCount").value(2));
	}

	@Test
	@DisplayName("지출내역 필터링 - 카드 4자리 필터는 입력 카드 기준(OR)으로만 조회")
	void 지출내역_카드4자리_유저카드기준_필터링_성공() throws Exception {
		// given
		UserCardEntity matchedCard = createUserCard("매칭카드", "1234");
		UserCardEntity unmatchedCard = createUserCard("비매칭카드", "5678");

		createTestExpenseWithUserCard(
				"매칭 결제", 2, "2026-02-04T12:00:00", 10000.0, matchedCard.getUserCardId());
		createTestExpenseWithUserCard(
				"비매칭 결제", 2, "2026-02-04T13:00:00", 12000.0, unmatchedCard.getUserCardId());
		createTestExpenseWithDetails("현금 결제", 2, "2026-02-04T14:00:00", 9000.0, null);

		// when & then - user_card.card_number=1234인 내역만 조회
		mockMvc.perform(
						get("/account-books/{accountBookId}/expenses", accountBookId)
								.with(jwtTestHelper.withJwtAuth(userId))
								.param("page", "0")
								.param("size", "20")
								.param("sort", "occurredAt,desc")
								.param("cardFourDigits", "1234"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.expenses").isArray())
				.andExpect(jsonPath("$.expenses.length()").value(1))
				.andExpect(jsonPath("$.totalCount").value(1))
				.andExpect(jsonPath("$.expenses[0].merchantName").value("매칭 결제"))
				.andExpect(jsonPath("$.expenses[0].paymentMethod.card.lastDigits").value("1234"));
	}

	@Test
	@DisplayName("지출내역 필터링 - 거래처명 필터")
	void 지출내역_거래처명_필터링_성공() throws Exception {
		// given
		createTestExpenseWithDetails("스타벅스 강남점", 2, "2026-02-04T12:30:00", 10000.0, null);
		createTestExpenseWithDetails("맥도날드", 2, "2026-02-04T14:00:00", 8000.0, null);
		createTestExpenseWithDetails("스타벅스 역삼점", 2, "2026-02-04T18:00:00", 12000.0, null);

		// when & then - "스타벅스"로 검색
		mockMvc.perform(
						get("/account-books/{accountBookId}/expenses", accountBookId)
								.with(jwtTestHelper.withJwtAuth(userId))
								.param("page", "0")
								.param("size", "20")
								.param("sort", "occurredAt,desc")
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
		createTestExpenseWithDetails("스타벅스", 2, "2026-02-01T12:00:00", 10000.0, null);
		createTestExpenseWithDetails("맥도날드", 2, "2026-02-05T12:00:00", 8000.0, null);
		createTestExpenseWithDetails("택시", 3, "2026-02-10T12:00:00", 15000.0, null);

		// when & then - 2026-02-03 ~ 2026-02-07 범위 조회
		mockMvc.perform(
						get("/account-books/{accountBookId}/expenses", accountBookId)
								.with(jwtTestHelper.withJwtAuth(userId))
								.param("page", "0")
								.param("size", "20")
								.param("sort", "occurredAt,desc")
								.param("startDate", "2026-02-03T00:00:00+09:00")
								.param("endDate", "2026-02-07T23:59:59+09:00"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.expenses").isArray())
				.andExpect(jsonPath("$.expenses.length()").value(1))
				.andExpect(jsonPath("$.totalCount").value(1));
	}

	@Test
	@DisplayName("지출내역 필터링 - 거래처명 다중값(OR) 필터")
	void 지출내역_거래처명_다중값_필터링_성공() throws Exception {
		// given
		createTestExpenseWithDetails("스타벅스", 2, "2026-02-04T12:00:00", 10000.0, null);
		createTestExpenseWithDetails("맥도날드", 2, "2026-02-04T13:00:00", 15000.0, null);
		createTestExpenseWithDetails("버거킹", 2, "2026-02-04T14:00:00", 50000.0, null);

		// when & then - 스타벅스 또는 맥도날드만 조회
		mockMvc.perform(
						get("/account-books/{accountBookId}/expenses", accountBookId)
								.with(jwtTestHelper.withJwtAuth(userId))
								.param("page", "0")
								.param("size", "20")
								.param("sort", "occurredAt,desc")
								.param("merchantName", "스타벅스", "맥도날드"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.expenses").isArray())
				.andExpect(jsonPath("$.expenses.length()").value(2))
				.andExpect(jsonPath("$.totalCount").value(2));
	}

	@Test
	@DisplayName("지출내역 정렬 - 금액순 정렬 시 지출은 음수, 수입은 양수로 판정")
	void 지출내역_금액정렬시_소비수입_분리_성공() throws Exception {
		// given
		createTestExpenseWithDetails("소비-중간", 2, "2026-02-04T12:00:00", 30000.0, null);
		createTestExpenseWithDetails("소비-작음", 2, "2026-02-04T13:00:00", 10000.0, null);
		createTestExpenseWithDetails("수입-큼", 9, "2026-02-04T14:00:00", 50000.0, null);

		// when & then
		mockMvc.perform(
						get("/account-books/{accountBookId}/expenses", accountBookId)
								.with(jwtTestHelper.withJwtAuth(userId))
								.param("page", "0")
								.param("size", "20")
								.param("sort", "baseCurrencyAmount,desc"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.expenses").isArray())
				.andExpect(jsonPath("$.expenses.length()").value(3))
				.andExpect(jsonPath("$.expenses[0].category").value(9))
				.andExpect(jsonPath("$.expenses[0].merchantName").value("수입-큼"))
				.andExpect(jsonPath("$.expenses[1].category").value(2))
				.andExpect(jsonPath("$.expenses[1].merchantName").value("소비-작음"))
				.andExpect(jsonPath("$.expenses[2].category").value(2))
				.andExpect(jsonPath("$.expenses[2].merchantName").value("소비-중간"));
	}

	@Test
	@DisplayName("지출내역 정렬 - 금액 오름차순 정렬 시 지출 우선 후 수입")
	void 지출내역_금액오름차순_정렬_성공() throws Exception {
		// given
		createTestExpenseWithDetails("소비-중간", 2, "2026-02-04T12:00:00", 30000.0, null);
		createTestExpenseWithDetails("소비-작음", 2, "2026-02-04T13:00:00", 10000.0, null);
		createTestExpenseWithDetails("수입-큼", 9, "2026-02-04T14:00:00", 50000.0, null);

		// when & then
		mockMvc.perform(
						get("/account-books/{accountBookId}/expenses", accountBookId)
								.with(jwtTestHelper.withJwtAuth(userId))
								.param("page", "0")
								.param("size", "20")
								.param("sort", "baseCurrencyAmount,asc"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.expenses").isArray())
				.andExpect(jsonPath("$.expenses.length()").value(3))
				.andExpect(jsonPath("$.expenses[0].category").value(2))
				.andExpect(jsonPath("$.expenses[0].merchantName").value("소비-중간"))
				.andExpect(jsonPath("$.expenses[1].category").value(2))
				.andExpect(jsonPath("$.expenses[1].merchantName").value("소비-작음"))
				.andExpect(jsonPath("$.expenses[2].category").value(9))
				.andExpect(jsonPath("$.expenses[2].merchantName").value("수입-큼"));
	}

	@Test
	@DisplayName("지출내역 수기 작성 - base only 입력 시 base는 비우고 calculated로 표시")
	void 수기지출_base_only_입력시_정합성_성공() throws Exception {
		String body =
				"""
			{
			"merchantName": "기내결제",
			"category": 2,
			"userCardId": null,
			"occurredAt": "2026-02-04T12:30:00Z",
			"baseCurrencyAmount": 20000.0,
			"memo": "base only"
			}
			""";

		mockMvc.perform(
						post("/account-books/{accountBookId}/expenses/manual", accountBookId)
								.with(jwtTestHelper.withJwtAuth(userId))
								.contentType(MediaType.APPLICATION_JSON)
								.content(body))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.baseCurrencyAmount").value("20000.00"));

		ExpenseEntity saved = expenseRepository.findAll().get(0);
		assertThat(saved.getBaseAmount()).isNull();
		assertThat(saved.getDisplayBaseAmount()).isEqualByComparingTo("20000.00");
		assertThat(saved.getLocalAmount()).isEqualByComparingTo("20000.00");
	}

	@Test
	@DisplayName("지출내역 수기 작성 - local+base 입력 시 base 우선 표시")
	void 수기지출_local_base_동시입력시_base_우선표시_성공() throws Exception {
		String body =
				"""
			{
			"merchantName": "해외결제",
			"category": 2,
			"userCardId": null,
			"occurredAt": "2026-02-04T12:30:00Z",
			"localCurrencyAmount": 10000.0,
			"baseCurrencyAmount": 10000.0,
			"localCurrencyCode": "KRW",
			"memo": "local+base"
			}
			""";

		mockMvc.perform(
						post("/account-books/{accountBookId}/expenses/manual", accountBookId)
								.with(jwtTestHelper.withJwtAuth(userId))
								.contentType(MediaType.APPLICATION_JSON)
								.content(body))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.localCurrencyAmount").value("10000.00"))
				.andExpect(jsonPath("$.baseCurrencyAmount").value("10000.00"));

		ExpenseEntity saved = expenseRepository.findAll().get(0);
		assertThat(saved.getBaseAmount()).isEqualByComparingTo("10000.00");
		assertThat(saved.getDisplayBaseAmount()).isEqualByComparingTo("10000.00");
		assertThat(saved.getExchangeInfo().getCalculatedBaseCurrencyAmount()).isNotNull();
		assertThat(saved.getExchangeInfo().getCalculatedBaseCurrencyAmount().signum())
				.isGreaterThan(0);
	}

	@Test
	@DisplayName("지출내역 수기 작성 - localAmount 0 입력 시 실패")
	void 수기지출_local_0_입력시_실패() throws Exception {
		String body =
				"""
			{
			"merchantName": "잘못된지출",
			"category": 2,
			"userCardId": null,
			"occurredAt": "2026-02-04T12:30:00Z",
			"localCurrencyAmount": 0.0,
			"localCurrencyCode": "KRW",
			"memo": "invalid"
			}
			""";

		mockMvc.perform(
						post("/account-books/{accountBookId}/expenses/manual", accountBookId)
								.with(jwtTestHelper.withJwtAuth(userId))
								.contentType(MediaType.APPLICATION_JSON)
								.content(body))
				.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("지출내역 수기 작성 - baseAmount 0 입력 시 실패")
	void 수기지출_base_0_입력시_실패() throws Exception {
		String body =
				"""
			{
			"merchantName": "잘못된지출",
			"category": 2,
			"userCardId": null,
			"occurredAt": "2026-02-04T12:30:00Z",
			"baseCurrencyAmount": 0.0,
			"memo": "invalid"
			}
			""";

		mockMvc.perform(
						post("/account-books/{accountBookId}/expenses/manual", accountBookId)
								.with(jwtTestHelper.withJwtAuth(userId))
								.contentType(MediaType.APPLICATION_JSON)
								.content(body))
				.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("지출내역 수기 작성 - merchantName 공백 입력 시 실패")
	void 수기지출_merchant_공백_입력시_실패() throws Exception {
		String body =
				"""
			{
			"merchantName": "   ",
			"category": 2,
			"userCardId": null,
			"occurredAt": "2026-02-04T12:30:00Z",
			"localCurrencyAmount": 10000.0,
			"localCurrencyCode": "KRW",
			"memo": "invalid"
			}
			""";

		mockMvc.perform(
						post("/account-books/{accountBookId}/expenses/manual", accountBookId)
								.with(jwtTestHelper.withJwtAuth(userId))
								.contentType(MediaType.APPLICATION_JSON)
								.content(body))
				.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("지출내역 수기 작성 - merchantName 40자 초과 입력 시 실패")
	void 수기지출_merchant_40자초과_입력시_실패() throws Exception {
		String body =
				"""
			{
			"merchantName": "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
			"category": 2,
			"userCardId": null,
			"occurredAt": "2026-02-04T12:30:00Z",
			"localCurrencyAmount": 10000.0,
			"localCurrencyCode": "KRW",
			"memo": "invalid"
			}
			""";

		mockMvc.perform(
						post("/account-books/{accountBookId}/expenses/manual", accountBookId)
								.with(jwtTestHelper.withJwtAuth(userId))
								.contentType(MediaType.APPLICATION_JSON)
								.content(body))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("400_INVALID_INPUT_VALUE"));
	}

	@Test
	@DisplayName("지출내역 필터링 - 복합 조건 필터")
	void 지출내역_복합조건_필터링_성공() throws Exception {
		// given - 다양한 조건의 지출내역 생성
		createTestExpenseWithDetails("스타벅스", 2, "2026-02-04T12:00:00", 10000.0, null);
		createTestExpenseWithDetails("스타벅스", 2, "2026-02-04T13:00:00", 25000.0, null);
		createTestExpenseWithDetails("맥도날드", 2, "2026-02-04T14:00:00", 8000.0, null);
		createTestExpenseWithDetails("택시", 3, "2026-02-04T15:00:00", 15000.0, null);

		// when & then - 카테고리=FOOD, 거래처명=스타벅스, 12:30~14:00(UTC) 범위
		mockMvc.perform(
						get("/account-books/{accountBookId}/expenses", accountBookId)
								.with(jwtTestHelper.withJwtAuth(userId))
								.param("page", "0")
								.param("size", "20")
								.param("sort", "occurredAt,desc")
								.param("category", "2")
								.param("merchantName", "스타벅스")
								.param("startDate", "2026-02-04T12:30:00Z")
								.param("endDate", "2026-02-04T14:00:00Z"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.expenses").isArray())
				.andExpect(jsonPath("$.expenses.length()").value(1))
				.andExpect(jsonPath("$.totalCount").value(1))
				.andExpect(jsonPath("$.expenses[0].merchantName").value("스타벅스"));
	}

	@Test
	@DisplayName("지출내역 필터링 - travelId 필터")
	void 지출내역_travelId_필터링_성공() throws Exception {
		// given - travelId가 있는 것과 없는 지출내역 생성
		createTestExpenseWithDetails("여행 중 식사", 2, "2026-02-04T12:00:00", 10000.0, 100L);
		createTestExpenseWithDetails("여행 중 택시", 3, "2026-02-04T13:00:00", 15000.0, 100L);
		createTestExpenseWithDetails("일상 식사", 2, "2026-02-04T14:00:00", 8000.0, null);

		// when & then - travelId=100인 지출내역만 조회
		mockMvc.perform(
						get("/account-books/{accountBookId}/expenses", accountBookId)
								.with(jwtTestHelper.withJwtAuth(userId))
								.param("page", "0")
								.param("size", "20")
								.param("sort", "occurredAt,desc")
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
			"category": 2,
			"userCardId": null,
			"occurredAt": "2026-02-04T12:30:00Z",
			"localCurrencyAmount": 10000.0,
			"localCurrencyCode": "KRW",
			"memo": "아메리카노"
			}
			""";

		mockMvc.perform(
						post("/account-books/{accountBookId}/expenses/manual", accountBookId)
								.with(jwtTestHelper.withJwtAuth(userId))
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
			String merchantName, Integer category, String occurredAt, Double amount, Long travelId)
			throws Exception {
		String travelIdJson = travelId != null ? String.valueOf(travelId) : "null";
		String body =
				String.format(
						"""
			{
			"merchantName": "%s",
			"category": %d,
			"userCardId": null,
			"occurredAt": "%sZ",
			"localCurrencyAmount": %.1f,
			"localCurrencyCode": "KRW",
			"memo": "테스트",
			"travelId": %s
			}
			""",
						merchantName, category, occurredAt, amount, travelIdJson);

		mockMvc.perform(
						post("/account-books/{accountBookId}/expenses/manual", accountBookId)
								.with(jwtTestHelper.withJwtAuth(userId))
								.contentType(MediaType.APPLICATION_JSON)
								.content(body))
				.andExpect(status().isCreated());

		return expenseRepository
				.findAll()
				.get(expenseRepository.findAll().size() - 1)
				.getExpenseId();
	}

	private Long createTestExpenseWithUserCard(
			String merchantName,
			Integer category,
			String occurredAt,
			Double amount,
			Long userCardId)
			throws Exception {
		String userCardIdJson = userCardId != null ? String.valueOf(userCardId) : "null";
		String body =
				String.format(
						"""
			{
			"merchantName": "%s",
			"category": %d,
			"userCardId": %s,
			"occurredAt": "%sZ",
			"localCurrencyAmount": %.1f,
			"localCurrencyCode": "KRW",
			"memo": "테스트"
			}
			""",
						merchantName, category, userCardIdJson, occurredAt, amount);

		mockMvc.perform(
						post("/account-books/{accountBookId}/expenses/manual", accountBookId)
								.with(jwtTestHelper.withJwtAuth(userId))
								.contentType(MediaType.APPLICATION_JSON)
								.content(body))
				.andExpect(status().isCreated());

		return expenseRepository
				.findAll()
				.get(expenseRepository.findAll().size() - 1)
				.getExpenseId();
	}

	private UserCardEntity createUserCard(String nickName, String cardNumber) {
		UserEntity user = userRepository.findById(userId).orElseThrow();
		return userCardRepository.save(
				UserCardEntity.builder()
						.user(user)
						.nickName(nickName)
						.cardNumber(cardNumber)
						.cardCompany(CardCompany.HYUNDAI)
						.build());
	}
}
