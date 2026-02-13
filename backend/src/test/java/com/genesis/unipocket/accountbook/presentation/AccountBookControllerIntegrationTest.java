package com.genesis.unipocket.accountbook.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.genesis.unipocket.TestcontainersConfiguration;
import com.genesis.unipocket.accountbook.command.persistence.entity.AccountBookEntity;
import com.genesis.unipocket.accountbook.command.persistence.repository.AccountBookCommandRepository;
import com.genesis.unipocket.auth.support.JwtTestHelper;
import com.genesis.unipocket.exchange.command.persistence.entity.ExchangeRate;
import com.genesis.unipocket.exchange.command.persistence.repository.ExchangeRateRepository;
import com.genesis.unipocket.global.common.enums.CountryCode;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.user.command.persistence.entity.UserEntity;
import com.genesis.unipocket.user.command.persistence.repository.UserCommandRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

/**
 * <b>가계부 도메인 통합 테스트</b>
 *
 * <p>Command(생성/수정/삭제) + Query(목록조회/단건조회/환율조회) 7개 엔드포인트에 대한 통합테스트입니다.
 */
@AutoConfigureMockMvc
@SpringBootTest
@ActiveProfiles("test-it")
@Import(TestcontainersConfiguration.class)
@Transactional
@Tag("integration")
class AccountBookControllerIntegrationTest {

	@Autowired private MockMvc mockMvc;
	@Autowired private AccountBookCommandRepository accountBookRepository;
	@Autowired private UserCommandRepository userRepository;
	@Autowired private ExchangeRateRepository exchangeRateRepository;
	@Autowired private JwtTestHelper jwtTestHelper;

	private UUID userId;

	@BeforeEach
	void setUp() {
		UserEntity user =
				userRepository.save(
						UserEntity.builder()
								.email("test-user@unipocket.com")
								.name("test-user")
								.build());
		userId = user.getId();

		LocalDateTime today = LocalDate.now().atStartOfDay();
		exchangeRateRepository.save(
				ExchangeRate.builder()
						.currencyCode(CurrencyCode.KRW)
						.recordedAt(today)
						.rate(new BigDecimal("1300.00"))
						.build());
		exchangeRateRepository.save(
				ExchangeRate.builder()
						.currencyCode(CurrencyCode.JPY)
						.recordedAt(today)
						.rate(new BigDecimal("150.00"))
						.build());
	}

	// ========== POST /account-books (가계부 생성) ==========

	@Test
	@DisplayName("가계부 생성 - 201 Created, Location 헤더, DB 저장 확인")
	void 가계부_생성_성공() throws Exception {
		String body =
				"""
			{
				"localCountryCode": "JP",
				"startDate": "2026-03-01",
				"endDate": "2026-03-31"
			}
			""";

		MvcResult result =
				mockMvc.perform(
								post("/account-books")
										.with(jwtTestHelper.withJwtAuth(userId))
										.contentType(MediaType.APPLICATION_JSON)
										.content(body))
						.andExpect(status().isCreated())
						.andExpect(header().exists("Location"))
						.andReturn();

		List<AccountBookEntity> all = accountBookRepository.findAll();
		assertThat(all).hasSize(1);
		assertThat(result.getResponse().getHeader("Location"))
				.isEqualTo("/account-books/" + all.get(0).getId());
	}

	@Test
	@DisplayName("가계부 생성 시 자동 제목 생성 - 'test-user의 가계부1'")
	void 가계부_생성시_자동_제목_생성() throws Exception {
		createAccountBook();

		AccountBookEntity saved = accountBookRepository.findAll().get(0);
		assertThat(saved.getTitle()).isEqualTo("test-user의 가계부1");
	}

	@Test
	@DisplayName("가계부 중복 생성 시 제목 번호 증가 - '가계부2'")
	void 가계부_중복생성시_제목_번호_증가() throws Exception {
		createAccountBook();
		createAccountBook();

		List<AccountBookEntity> all = accountBookRepository.findAll();
		assertThat(all).hasSize(2);
		assertThat(all)
				.extracting(AccountBookEntity::getTitle)
				.containsExactlyInAnyOrder("test-user의 가계부1", "test-user의 가계부2");
	}

	@Test
	@DisplayName("첫 가계부 생성 시 mainBucketId 자동 설정")
	void 첫_가계부_생성시_mainBucketId_자동설정() throws Exception {
		assertThat(userRepository.findById(userId).orElseThrow().hasMainBucket()).isFalse();

		createAccountBook();

		UserEntity updatedUser = userRepository.findById(userId).orElseThrow();
		Long accountBookId = accountBookRepository.findAll().get(0).getId();
		assertThat(updatedUser.getMainBucketId()).isEqualTo(accountBookId);
	}

	@Test
	@DisplayName("필수 필드(localCountryCode) 누락 시 400")
	void 필수필드_누락시_400() throws Exception {
		String body =
				"""
			{
				"startDate": "2026-03-01",
				"endDate": "2026-03-31"
			}
			""";

		mockMvc.perform(
						post("/account-books")
								.with(jwtTestHelper.withJwtAuth(userId))
								.contentType(MediaType.APPLICATION_JSON)
								.content(body))
				.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("localCountryCode가 KR이면 baseCountryCode(KR)과 동일하여 400")
	void localCountryCode가_KR일때_검증실패_400() throws Exception {
		String body =
				"""
			{
				"localCountryCode": "KR",
				"startDate": "2026-03-01",
				"endDate": "2026-03-31"
			}
			""";

		mockMvc.perform(
						post("/account-books")
								.with(jwtTestHelper.withJwtAuth(userId))
								.contentType(MediaType.APPLICATION_JSON)
								.content(body))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("400_ACCOUNT_BOOK_INVALID_COUNTRY_CODE"));
	}

	@Test
	@DisplayName("시작일이 종료일 이후면 400")
	void 시작일이_종료일_이후면_400() throws Exception {
		String body =
				"""
			{
				"localCountryCode": "JP",
				"startDate": "2026-04-01",
				"endDate": "2026-03-01"
			}
			""";

		mockMvc.perform(
						post("/account-books")
								.with(jwtTestHelper.withJwtAuth(userId))
								.contentType(MediaType.APPLICATION_JSON)
								.content(body))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("400_ACCOUNT_BOOK_INVALID_DATE_RANGE"));
	}

	// ========== PATCH /account-books/{id} (가계부 수정) ==========

	@Test
	@DisplayName("가계부 수정 - 204 No Content, DB 변경 확인")
	void 가계부_수정_성공() throws Exception {
		Long accountBookId = createAccountBook();

		String body =
				"""
			{
				"title": "수정된 가계부",
				"localCountryCode": "US",
				"baseCountryCode": "KR",
				"budget": 500000.00,
				"startDate": "2026-04-01",
				"endDate": "2026-04-30"
			}
			""";

		mockMvc.perform(
						patch("/account-books/{id}", accountBookId)
								.with(jwtTestHelper.withJwtAuth(userId))
								.contentType(MediaType.APPLICATION_JSON)
								.content(body))
				.andExpect(status().isNoContent());

		AccountBookEntity updated = accountBookRepository.findById(accountBookId).orElseThrow();
		assertThat(updated.getTitle()).isEqualTo("수정된 가계부");
		assertThat(updated.getLocalCountryCode()).isEqualTo(CountryCode.US);
		assertThat(updated.getBudget()).isEqualByComparingTo(new BigDecimal("500000.00"));
		assertThat(updated.getStartDate()).isEqualTo(LocalDate.of(2026, 4, 1));
		assertThat(updated.getEndDate()).isEqualTo(LocalDate.of(2026, 4, 30));
	}

	@Test
	@DisplayName("존재하지 않는 가계부 수정 - 404")
	void 존재하지않는_가계부_수정_404() throws Exception {
		String body =
				"""
			{
				"title": "수정",
				"localCountryCode": "JP",
				"baseCountryCode": "KR",
				"startDate": "2026-03-01",
				"endDate": "2026-03-31"
			}
			""";

		mockMvc.perform(
						patch("/account-books/{id}", 99999L)
								.with(jwtTestHelper.withJwtAuth(userId))
								.contentType(MediaType.APPLICATION_JSON)
								.content(body))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("404_ACCOUNT_BOOK_NOT_FOUND"));
	}

	@Test
	@DisplayName("다른 사용자 가계부 수정 - 403")
	void 다른_사용자_가계부_수정_403() throws Exception {
		Long accountBookId = createAccountBook();

		UserEntity otherUser =
				userRepository.save(
						UserEntity.builder()
								.email("other@unipocket.com")
								.name("other-user")
								.build());

		String body =
				"""
			{
				"title": "해킹",
				"localCountryCode": "JP",
				"baseCountryCode": "KR",
				"startDate": "2026-03-01",
				"endDate": "2026-03-31"
			}
			""";

		mockMvc.perform(
						patch("/account-books/{id}", accountBookId)
								.with(jwtTestHelper.withJwtAuth(otherUser.getId()))
								.contentType(MediaType.APPLICATION_JSON)
								.content(body))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("403_ACCOUNT_BOOK_UNAUTHORIZED_ACCESS"));
	}

	@Test
	@DisplayName("수정 시 빈 제목 - 400 (@NotBlank 위반)")
	void 수정시_빈_제목_400() throws Exception {
		Long accountBookId = createAccountBook();

		String body =
				"""
			{
				"title": "",
				"localCountryCode": "JP",
				"baseCountryCode": "KR",
				"startDate": "2026-03-01",
				"endDate": "2026-03-31"
			}
			""";

		mockMvc.perform(
						patch("/account-books/{id}", accountBookId)
								.with(jwtTestHelper.withJwtAuth(userId))
								.contentType(MediaType.APPLICATION_JSON)
								.content(body))
				.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("수정 시 동일 국가코드 - 400")
	void 수정시_동일_국가코드_400() throws Exception {
		Long accountBookId = createAccountBook();

		String body =
				"""
			{
				"title": "수정된 가계부",
				"localCountryCode": "KR",
				"baseCountryCode": "KR",
				"startDate": "2026-03-01",
				"endDate": "2026-03-31"
			}
			""";

		mockMvc.perform(
						patch("/account-books/{id}", accountBookId)
								.with(jwtTestHelper.withJwtAuth(userId))
								.contentType(MediaType.APPLICATION_JSON)
								.content(body))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("400_ACCOUNT_BOOK_INVALID_COUNTRY_CODE"));
	}

	// ========== PATCH /account-books/{id}/budget (예산 수정) ==========

	@Test
	@DisplayName("예산 수정 - 200 OK, 응답 JSON 확인")
	void 예산_수정_성공() throws Exception {
		Long accountBookId = createAccountBook();

		String body = """
			{ "budget": 1000000.00 }
			""";

		mockMvc.perform(
						patch("/account-books/{id}/budget", accountBookId)
								.with(jwtTestHelper.withJwtAuth(userId))
								.contentType(MediaType.APPLICATION_JSON)
								.content(body))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accountBookId").value(accountBookId))
				.andExpect(jsonPath("$.baseCountryCode").value("KR"))
				.andExpect(jsonPath("$.localCountryCode").value("JP"))
				.andExpect(jsonPath("$.budget").exists())
				.andExpect(jsonPath("$.budgetCreatedAt").exists())
				.andExpect(jsonPath("$.exchangeRate").exists());
	}

	@Test
	@DisplayName("예산 null 전송 시 400 (@NotNull 위반)")
	void 예산_null_전송시_400() throws Exception {
		Long accountBookId = createAccountBook();

		String body = """
			{ "budget": null }
			""";

		mockMvc.perform(
						patch("/account-books/{id}/budget", accountBookId)
								.with(jwtTestHelper.withJwtAuth(userId))
								.contentType(MediaType.APPLICATION_JSON)
								.content(body))
				.andExpect(status().isBadRequest());
	}

	// ========== DELETE /account-books/{id} (가계부 삭제) ==========

	@Test
	@DisplayName("가계부 삭제 - 204 No Content, DB 삭제 확인")
	void 가계부_삭제_성공() throws Exception {
		Long accountBookId = createAccountBook();
		assertThat(accountBookRepository.findById(accountBookId)).isPresent();

		mockMvc.perform(
						delete("/account-books/{id}", accountBookId)
								.with(jwtTestHelper.withJwtAuth(userId)))
				.andExpect(status().isNoContent());

		assertThat(accountBookRepository.findById(accountBookId)).isEmpty();
	}

	@Test
	@DisplayName("존재하지 않는 가계부 삭제 - 404")
	void 존재하지않는_가계부_삭제_404() throws Exception {
		mockMvc.perform(
						delete("/account-books/{id}", 99999L)
								.with(jwtTestHelper.withJwtAuth(userId)))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("404_ACCOUNT_BOOK_NOT_FOUND"));
	}

	// ========== GET /account-books (목록 조회) ==========

	@Test
	@DisplayName("빈 목록 조회 - 200 + 빈 배열")
	void 빈_목록_조회() throws Exception {
		mockMvc.perform(get("/account-books").with(jwtTestHelper.withJwtAuth(userId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$.length()").value(0));
	}

	@Test
	@DisplayName("가계부 목록 조회 - 2개, isMain 확인")
	void 가계부_목록_조회_성공() throws Exception {
		Long firstId = createAccountBook();
		Long secondId = createAccountBook();

		mockMvc.perform(get("/account-books").with(jwtTestHelper.withJwtAuth(userId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].id").value(firstId))
				.andExpect(jsonPath("$[0].isMain").value(true))
				.andExpect(jsonPath("$[1].id").value(secondId))
				.andExpect(jsonPath("$[1].isMain").value(false));
	}

	// ========== GET /account-books/{id} (단건 조회) ==========

	@Test
	@DisplayName("단건 조회 - 성공")
	void 단건_조회_성공() throws Exception {
		Long accountBookId = createAccountBook();

		mockMvc.perform(
						get("/account-books/{id}", accountBookId)
								.with(jwtTestHelper.withJwtAuth(userId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(accountBookId))
				.andExpect(jsonPath("$.title").value("test-user의 가계부1"))
				.andExpect(jsonPath("$.localCountryCode").value("JP"))
				.andExpect(jsonPath("$.baseCountryCode").value("KR"))
				.andExpect(jsonPath("$.startDate").value("2026-03-01"))
				.andExpect(jsonPath("$.endDate").value("2026-03-31"));
	}

	@Test
	@DisplayName("존재하지 않는 가계부 조회 - 404")
	void 존재하지않는_가계부_조회_404() throws Exception {
		mockMvc.perform(get("/account-books/{id}", 99999L).with(jwtTestHelper.withJwtAuth(userId)))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("404_ACCOUNT_BOOK_NOT_FOUND"));
	}

	@Test
	@DisplayName("다른 사용자 가계부 조회 - 404 (userId 불일치)")
	void 다른_사용자_가계부_조회_404() throws Exception {
		Long accountBookId = createAccountBook();

		UserEntity otherUser =
				userRepository.save(
						UserEntity.builder()
								.email("other@unipocket.com")
								.name("other-user")
								.build());

		mockMvc.perform(
						get("/account-books/{id}", accountBookId)
								.with(jwtTestHelper.withJwtAuth(otherUser.getId())))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("404_ACCOUNT_BOOK_NOT_FOUND"));
	}

	// ========== GET /account-books/{id}/exchange-rate (환율 조회) ==========

	@Test
	@DisplayName("환율 조회 - 예산 미설정 시 400")
	void 환율_조회_예산_미설정시_400() throws Exception {
		Long accountBookId = createAccountBook();

		mockMvc.perform(
						get("/account-books/{id}/exchange-rate", accountBookId)
								.with(jwtTestHelper.withJwtAuth(userId)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("400_ACCOUNT_BOOK_BUDGET_NOT_SET"));
	}

	// ========== Helper Methods ==========

	private Long createAccountBook() throws Exception {
		String body =
				"""
			{
				"localCountryCode": "JP",
				"startDate": "2026-03-01",
				"endDate": "2026-03-31"
			}
			""";

		mockMvc.perform(
						post("/account-books")
								.with(jwtTestHelper.withJwtAuth(userId))
								.contentType(MediaType.APPLICATION_JSON)
								.content(body))
				.andExpect(status().isCreated());

		List<AccountBookEntity> all = accountBookRepository.findAll();
		return all.get(all.size() - 1).getId();
	}
}
