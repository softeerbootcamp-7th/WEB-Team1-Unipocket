package com.genesis.unipocket.accountbook.query.presentation;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.genesis.unipocket.accountbook.query.persistence.response.AccountBookDetailResponse;
import com.genesis.unipocket.accountbook.query.persistence.response.AccountBookExchangeRateResponse;
import com.genesis.unipocket.accountbook.query.persistence.response.AccountBookSummaryResponse;
import com.genesis.unipocket.accountbook.query.service.AccountBookQueryService;
import com.genesis.unipocket.auth.command.application.JwtProvider;
import com.genesis.unipocket.auth.command.application.TokenBlacklistService;
import com.genesis.unipocket.auth.common.constant.AuthCookieConstants;
import com.genesis.unipocket.global.common.enums.CountryCode;
import jakarta.servlet.http.Cookie;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AccountBookQueryController.class)
class AccountBookQueryControllerTest {

	@Autowired private MockMvc mockMvc;

	@MockitoBean private AccountBookQueryService accountBookQueryService;
	@MockitoBean private JwtProvider jwtProvider;
	@MockitoBean private TokenBlacklistService tokenBlacklistService;

	@SuppressWarnings("unused")
	@MockitoBean
	private JpaMetamodelMappingContext jpaMetamodelMappingContext;

	@Test
	@DisplayName("내 가계부 목록 조회 성공")
	void getAccountBooks_Success() throws Exception {
		UUID userId = UUID.randomUUID();
		String accessToken = "valid_token";

		given(accountBookQueryService.getAccountBooks(userId.toString()))
				.willReturn(
						List.of(
								new AccountBookSummaryResponse(1L, "메인 가계부", true),
								new AccountBookSummaryResponse(2L, "보조 가계부", false)));
		mockAuthentication(accessToken, userId);

		mockMvc.perform(
						get("/account-books")
								.cookie(new Cookie(AuthCookieConstants.ACCESS_TOKEN, accessToken)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(1L))
				.andExpect(jsonPath("$[0].title").value("메인 가계부"))
				.andExpect(jsonPath("$[0].isMain").value(true))
				.andExpect(jsonPath("$[1].id").value(2L));
	}

	@Test
	@DisplayName("가계부 상세 조회 성공")
	void getAccountBook_Success() throws Exception {
		UUID userId = UUID.randomUUID();
		String accessToken = "valid_token";
		Long accountBookId = 1L;

		given(accountBookQueryService.getAccountBookDetail(userId.toString(), accountBookId))
				.willReturn(
						new AccountBookDetailResponse(
								accountBookId,
								"메인 가계부",
								CountryCode.US,
								CountryCode.KR,
								BigDecimal.valueOf(300000),
								LocalDateTime.of(2026, 1, 1, 9, 0, 0),
								List.of(),
								LocalDate.of(2026, 1, 1),
								LocalDate.of(2026, 1, 31)));
		mockAuthentication(accessToken, userId);

		mockMvc.perform(
						get("/account-books/{accountBookId}", accountBookId)
								.cookie(new Cookie(AuthCookieConstants.ACCESS_TOKEN, accessToken)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(accountBookId))
				.andExpect(jsonPath("$.title").value("메인 가계부"))
				.andExpect(jsonPath("$.budget").value("300000.00"))
				.andExpect(jsonPath("$.budgetCreatedAt").value("2026-01-01T09:00:00"));
	}

	@Test
	@DisplayName("가계부 기준/상대 국가 환율 조회 성공")
	void getAccountBookExchangeRate_Success() throws Exception {
		UUID userId = UUID.randomUUID();
		String accessToken = "valid_token";
		Long accountBookId = 1L;

		given(
						accountBookQueryService.getAccountBookExchangeRate(
								userId.toString(), accountBookId, null))
				.willReturn(
						new AccountBookExchangeRateResponse(
								CountryCode.KR,
								CountryCode.JP,
								BigDecimal.valueOf(0.11),
								LocalDateTime.of(2026, 2, 1, 9, 0, 0)));
		mockAuthentication(accessToken, userId);

		mockMvc.perform(
						get("/account-books/{accountBookId}/exchange-rate", accountBookId)
								.cookie(new Cookie(AuthCookieConstants.ACCESS_TOKEN, accessToken)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.baseCountryCode").value("KR"))
				.andExpect(jsonPath("$.localCountryCode").value("JP"))
				.andExpect(jsonPath("$.exchangeRate").value("0.11"))
				.andExpect(jsonPath("$.budgetCreatedAt").value("2026-02-01T09:00:00"));

		verify(accountBookQueryService)
				.getAccountBookExchangeRate(userId.toString(), accountBookId, null);
	}

	@Test
	@DisplayName("가계부 기준/상대 국가 환율 조회 성공 - occurredAt 지정")
	void getAccountBookExchangeRate_Success_WithOccurredAt() throws Exception {
		UUID userId = UUID.randomUUID();
		String accessToken = "valid_token";
		Long accountBookId = 1L;
		LocalDateTime occurredAt = LocalDateTime.of(2026, 2, 13, 10, 30, 0);

		given(
						accountBookQueryService.getAccountBookExchangeRate(
								userId.toString(), accountBookId, occurredAt))
				.willReturn(
						new AccountBookExchangeRateResponse(
								CountryCode.KR,
								CountryCode.JP,
								BigDecimal.valueOf(0.11),
								LocalDateTime.of(2026, 2, 1, 9, 0, 0)));
		mockAuthentication(accessToken, userId);

		mockMvc.perform(
						get("/account-books/{accountBookId}/exchange-rate", accountBookId)
								.param("occurredAt", "2026-02-13T10:30:00")
								.cookie(new Cookie("access_token", accessToken)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.baseCountryCode").value("KR"))
				.andExpect(jsonPath("$.localCountryCode").value("JP"))
				.andExpect(jsonPath("$.exchangeRate").value("0.11"))
				.andExpect(jsonPath("$.budgetCreatedAt").value("2026-02-01T09:00:00"));

		verify(accountBookQueryService)
				.getAccountBookExchangeRate(userId.toString(), accountBookId, occurredAt);
	}

	private void mockAuthentication(String accessToken, UUID userId) {
		given(jwtProvider.validateToken(accessToken)).willReturn(true);
		given(jwtProvider.getJti(accessToken)).willReturn("jti");
		given(tokenBlacklistService.isBlacklisted("jti")).willReturn(false);
		given(jwtProvider.getUserId(accessToken)).willReturn(userId);
	}
}
