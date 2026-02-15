package com.genesis.unipocket.user.command.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.genesis.unipocket.auth.command.application.JwtProvider;
import com.genesis.unipocket.auth.common.constant.AuthCookieConstants;
import com.genesis.unipocket.user.command.facade.UserCommandFacade;
import com.genesis.unipocket.user.command.persistence.entity.enums.CardCompany;
import com.genesis.unipocket.user.command.presentation.request.UserCardRequest;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserCommandController.class)
class UserCommandControllerTest {

	@Autowired private MockMvc mockMvc;

	@MockitoBean private UserCommandFacade userCommandFacade;

	@MockitoBean private JwtProvider jwtProvider;

	@MockitoBean
	private com.genesis.unipocket.auth.command.application.TokenBlacklistService
			tokenBlacklistService;

	@MockitoBean
	private org.springframework.data.jpa.mapping.JpaMetamodelMappingContext
			jpaMetamodelMappingContext;

	@Autowired private ObjectMapper objectMapper;

	@Test
	@DisplayName("회원 탈퇴 성공")
	void withdraw_Success() throws Exception {
		UUID userId = UUID.randomUUID();
		String accessToken = "valid_token";

		// Mock authentication
		given(jwtProvider.validateToken(accessToken)).willReturn(true);
		given(jwtProvider.getJti(accessToken)).willReturn("jti");
		given(tokenBlacklistService.isBlacklisted("jti")).willReturn(false);
		given(jwtProvider.getUserId(accessToken)).willReturn(userId);

		doNothing().when(userCommandFacade).withdraw(userId);

		mockMvc.perform(
						delete("/users/me")
								.cookie(
										new jakarta.servlet.http.Cookie(
												AuthCookieConstants.ACCESS_TOKEN, accessToken)))
				.andExpect(status().isNoContent());
	}

	@Test
	@DisplayName("카드 생성 성공")
	void createCard_Success() throws Exception {
		UUID userId = UUID.randomUUID();
		Long cardId = 1L;
		String accessToken = "valid_token";
		UserCardRequest request = new UserCardRequest("My Card", "1234", CardCompany.HYUNDAI);

		// Mock authentication
		given(jwtProvider.validateToken(accessToken)).willReturn(true);
		given(jwtProvider.getJti(accessToken)).willReturn("jti");
		given(tokenBlacklistService.isBlacklisted("jti")).willReturn(false);
		given(jwtProvider.getUserId(accessToken)).willReturn(userId);

		given(userCommandFacade.createCard(any(UserCardRequest.class), any())).willReturn(cardId);

		mockMvc.perform(
						post("/users/cards")
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request))
								.cookie(
										new jakarta.servlet.http.Cookie(
												AuthCookieConstants.ACCESS_TOKEN, accessToken)))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", "/users/cards/" + cardId));
	}

	@Test
	@DisplayName("카드 삭제 성공")
	void deleteCard_Success() throws Exception {
		Long cardId = 1L;
		UUID userId = UUID.randomUUID();
		String accessToken = "valid_token";

		// Mock authentication
		given(jwtProvider.validateToken(accessToken)).willReturn(true);
		given(jwtProvider.getJti(accessToken)).willReturn("jti");
		given(tokenBlacklistService.isBlacklisted("jti")).willReturn(false);
		given(jwtProvider.getUserId(accessToken)).willReturn(userId);

		doNothing().when(userCommandFacade).deleteCard(any(), any());

		mockMvc.perform(
						delete("/users/cards/{cardId}", cardId)
								.cookie(
										new jakarta.servlet.http.Cookie(
												AuthCookieConstants.ACCESS_TOKEN, accessToken)))
				.andExpect(status().isNoContent());
	}
}
