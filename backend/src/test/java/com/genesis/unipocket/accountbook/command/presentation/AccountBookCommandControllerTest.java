package com.genesis.unipocket.accountbook.command.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.genesis.unipocket.accountbook.command.facade.AccountBookCommandFacade;
import com.genesis.unipocket.accountbook.command.presentation.request.AccountBookCreateRequest;
import com.genesis.unipocket.accountbook.command.presentation.request.AccountBookUpdateRequest;
import com.genesis.unipocket.auth.command.application.JwtProvider;
import com.genesis.unipocket.auth.command.application.TokenBlacklistService;
import com.genesis.unipocket.global.common.enums.CountryCode;
import jakarta.servlet.http.Cookie;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AccountBookCommandController.class)
class AccountBookCommandControllerTest {

	@Autowired private MockMvc mockMvc;
	@Autowired private ObjectMapper objectMapper;

	@MockitoBean private AccountBookCommandFacade accountBookCommandFacade;
	@MockitoBean private JwtProvider jwtProvider;
	@MockitoBean private TokenBlacklistService tokenBlacklistService;

	@SuppressWarnings("unused")
	@MockitoBean
	private JpaMetamodelMappingContext jpaMetamodelMappingContext;

	@Test
	@DisplayName("가계부 생성 성공")
	void createAccountBook_Success() throws Exception {
		UUID userId = UUID.randomUUID();
		String accessToken = "valid_token";
		Long accountBookId = 1L;
		AccountBookCreateRequest request =
				new AccountBookCreateRequest(
						CountryCode.US, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));

		mockAuthentication(accessToken, userId);
		given(accountBookCommandFacade.createAccountBook(eq(userId), eq(request)))
				.willReturn(accountBookId);

		mockMvc.perform(
						post("/api/account-books")
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request))
								.cookie(new Cookie("access_token", accessToken)))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", "/api/account-books/" + accountBookId));
	}

	@Test
	@DisplayName("가계부 수정 성공")
	void updateAccountBook_Success() throws Exception {
		UUID userId = UUID.randomUUID();
		String accessToken = "valid_token";
		Long accountBookId = 3L;
		AccountBookUpdateRequest request =
				new AccountBookUpdateRequest(
						"제목 수정",
						CountryCode.JP,
						CountryCode.KR,
						300000L,
						LocalDate.of(2026, 2, 1),
						LocalDate.of(2026, 2, 28));

		mockAuthentication(accessToken, userId);
		given(
						accountBookCommandFacade.updateAccountBook(
								eq(userId), eq(accountBookId), eq(request)))
				.willReturn(accountBookId);

		mockMvc.perform(
						patch("/api/account-books/{accountBookId}", accountBookId)
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request))
								.cookie(new Cookie("access_token", accessToken)))
				.andExpect(status().isNoContent());
	}

	@Test
	@DisplayName("가계부 삭제 성공")
	void deleteAccountBook_Success() throws Exception {
		UUID userId = UUID.randomUUID();
		String accessToken = "valid_token";
		Long accountBookId = 5L;

		mockAuthentication(accessToken, userId);
		doNothing().when(accountBookCommandFacade).deleteAccountBook(userId, accountBookId);

		mockMvc.perform(
						delete("/api/account-books/{accountBookId}", accountBookId)
								.cookie(new Cookie("access_token", accessToken)))
				.andExpect(status().isNoContent());
	}

	@Test
	@DisplayName("가계부 생성 실패 - 필수값 누락")
	void createAccountBook_Fail_WhenMissingRequiredField() throws Exception {
		UUID userId = UUID.randomUUID();
		String accessToken = "valid_token";
		String invalidBody = "{\"startDate\":\"2026-01-01\",\"endDate\":\"2026-01-31\"}";

		mockAuthentication(accessToken, userId);

		mockMvc.perform(
						post("/api/account-books")
								.contentType(MediaType.APPLICATION_JSON)
								.content(invalidBody)
								.cookie(new Cookie("access_token", accessToken)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("400_INVALID_INPUT_VALUE"))
				.andExpect(
						jsonPath("$.message").value("400_ACCOUNT_BOOK_CREATE_VALIDATION_FAILED"));

		verify(accountBookCommandFacade, never()).createAccountBook(any(), any());
	}

	@Test
	@DisplayName("가계부 수정 실패 - title 공백")
	void updateAccountBook_Fail_WhenBlankTitle() throws Exception {
		UUID userId = UUID.randomUUID();
		String accessToken = "valid_token";
		Long accountBookId = 3L;
		AccountBookUpdateRequest request =
				new AccountBookUpdateRequest(
						" ",
						CountryCode.JP,
						CountryCode.KR,
						300000L,
						LocalDate.of(2026, 2, 1),
						LocalDate.of(2026, 2, 28));

		mockAuthentication(accessToken, userId);

		mockMvc.perform(
						patch("/api/account-books/{accountBookId}", accountBookId)
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request))
								.cookie(new Cookie("access_token", accessToken)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("400_INVALID_INPUT_VALUE"))
				.andExpect(
						jsonPath("$.message").value("400_ACCOUNT_BOOK_UPDATE_VALIDATION_FAILED"));

		verify(accountBookCommandFacade, never()).updateAccountBook(any(), any(), any());
	}

	private void mockAuthentication(String accessToken, UUID userId) {
		given(jwtProvider.validateToken(accessToken)).willReturn(true);
		given(jwtProvider.getJti(accessToken)).willReturn("jti");
		given(tokenBlacklistService.isBlacklisted("jti")).willReturn(false);
		given(jwtProvider.getUserId(accessToken)).willReturn(userId);
	}
}
