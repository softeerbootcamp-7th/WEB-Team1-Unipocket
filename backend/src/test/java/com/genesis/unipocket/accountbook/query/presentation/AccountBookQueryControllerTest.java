package com.genesis.unipocket.accountbook.query.presentation;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.genesis.unipocket.accountbook.query.persistence.response.AccountBookDetailResponse;
import com.genesis.unipocket.accountbook.query.persistence.response.AccountBookSummaryResponse;
import com.genesis.unipocket.accountbook.query.service.AccountBookQueryService;
import com.genesis.unipocket.auth.command.application.JwtProvider;
import com.genesis.unipocket.auth.command.application.TokenBlacklistService;
import com.genesis.unipocket.global.common.enums.CountryCode;
import jakarta.servlet.http.Cookie;
import java.time.LocalDate;
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

		mockMvc.perform(get("/api/account-books").cookie(new Cookie("access_token", accessToken)))
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
								300000L,
								List.of(),
								LocalDate.of(2026, 1, 1),
								LocalDate.of(2026, 1, 31)));
		mockAuthentication(accessToken, userId);

		mockMvc.perform(
						get("/api/account-books/{accountBookId}", accountBookId)
								.cookie(new Cookie("access_token", accessToken)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(accountBookId))
				.andExpect(jsonPath("$.title").value("메인 가계부"))
				.andExpect(jsonPath("$.budget").value(300000));
	}

	private void mockAuthentication(String accessToken, UUID userId) {
		given(jwtProvider.validateToken(accessToken)).willReturn(true);
		given(jwtProvider.getJti(accessToken)).willReturn("jti");
		given(tokenBlacklistService.isBlacklisted("jti")).willReturn(false);
		given(jwtProvider.getUserId(accessToken)).willReturn(userId);
	}
}
