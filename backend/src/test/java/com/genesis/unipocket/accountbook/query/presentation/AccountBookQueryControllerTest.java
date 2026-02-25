package com.genesis.unipocket.accountbook.query.presentation;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.genesis.unipocket.accountbook.query.persistence.response.AccountBookAmountResponse;
import com.genesis.unipocket.accountbook.query.persistence.response.AccountBookDetailResponse;
import com.genesis.unipocket.accountbook.query.persistence.response.AccountBookExchangeRateResponse;
import com.genesis.unipocket.accountbook.query.persistence.response.AccountBookSummaryResponse;
import com.genesis.unipocket.accountbook.query.service.AccountBookAmountQueryService;
import com.genesis.unipocket.accountbook.query.service.AccountBookQueryService;
import com.genesis.unipocket.auth.command.application.JwtProvider;
import com.genesis.unipocket.auth.command.application.TokenBlacklistService;
import com.genesis.unipocket.auth.common.constant.AuthCookieConstants;
import com.genesis.unipocket.global.common.enums.CountryCode;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.user.command.persistence.repository.UserCommandRepository;
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
	@MockitoBean private AccountBookAmountQueryService accountBookAmountQueryService;
	@MockitoBean private JwtProvider jwtProvider;
	@MockitoBean private TokenBlacklistService tokenBlacklistService;
	@MockitoBean private UserCommandRepository userRepository;

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
				.andExpect(jsonPath("$[0].accountBookId").value(1L))
				.andExpect(jsonPath("$[0].title").value("메인 가계부"))
				.andExpect(jsonPath("$[0].isMain").value(true))
				.andExpect(jsonPath("$[1].accountBookId").value(2L));
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
								LocalDate.of(2026, 1, 1),
								LocalDate.of(2026, 1, 31)));
		mockAuthentication(accessToken, userId);

		mockMvc.perform(
						get("/account-books/{accountBookId}", accountBookId)
								.cookie(new Cookie(AuthCookieConstants.ACCESS_TOKEN, accessToken)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accountBookId").value(accountBookId))
				.andExpect(jsonPath("$.title").value("메인 가계부"));
	}

	@Test
	@DisplayName("가계부 지출 합계 조회 성공")
	void getAccountBookAmount_Success() throws Exception {
		UUID userId = UUID.randomUUID();
		String accessToken = "valid_token";
		Long accountBookId = 1L;

		given(accountBookAmountQueryService.getAccountBookAmount(userId.toString(), accountBookId))
				.willReturn(
						new AccountBookAmountResponse(
								CountryCode.JP,
								CurrencyCode.JPY,
								CountryCode.KR,
								CurrencyCode.KRW,
								new BigDecimal("12000.50"),
								new BigDecimal("109000.10"),
								new BigDecimal("1200.00"),
								new BigDecimal("11000.00"),
								null,
								null));
		mockAuthentication(accessToken, userId);

		mockMvc.perform(
						get("/account-books/{accountBookId}/amount", accountBookId)
								.cookie(new Cookie(AuthCookieConstants.ACCESS_TOKEN, accessToken)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.localCountryCode").value("JP"))
				.andExpect(jsonPath("$.localCurrencyCode").value("JPY"))
				.andExpect(jsonPath("$.baseCountryCode").value("KR"))
				.andExpect(jsonPath("$.baseCurrencyCode").value("KRW"))
				.andExpect(jsonPath("$.totalLocalAmount").value("12000.50"))
				.andExpect(jsonPath("$.totalBaseAmount").value("109000.10"))
				.andExpect(jsonPath("$.thisMonthLocalAmount").value("1200.00"))
				.andExpect(jsonPath("$.thisMonthBaseAmount").value("11000.00"));

		verify(accountBookAmountQueryService)
				.getAccountBookAmount(userId.toString(), accountBookId);
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
