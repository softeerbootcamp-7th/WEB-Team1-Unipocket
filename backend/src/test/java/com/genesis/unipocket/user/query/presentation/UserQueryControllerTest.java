package com.genesis.unipocket.user.query.presentation;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.genesis.unipocket.auth.command.application.JwtProvider;
import com.genesis.unipocket.auth.command.application.TokenBlacklistService;
import com.genesis.unipocket.user.command.persistence.entity.enums.CardCompany;
import com.genesis.unipocket.user.command.persistence.entity.enums.UserRole;
import com.genesis.unipocket.user.command.persistence.entity.enums.UserStatus;
import com.genesis.unipocket.user.query.persistence.response.UserCardQueryResponse;
import com.genesis.unipocket.user.query.persistence.response.UserQueryResponse;
import com.genesis.unipocket.user.query.service.UserQueryService;
import jakarta.servlet.http.Cookie;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserQueryController.class)
class UserQueryControllerTest {

	@Autowired private MockMvc mockMvc;

	@MockBean private UserQueryService userQueryService;

	@MockBean private JwtProvider jwtProvider;

	@MockBean private TokenBlacklistService tokenBlacklistService;

	@MockBean private JpaMetamodelMappingContext jpaMetamodelMappingContext;

	@Autowired private ObjectMapper objectMapper;

	@Test
	@DisplayName("내 정보 조회 성공")
	void getMyInfo_Success() throws Exception {
		UUID userId = UUID.randomUUID();
		String accessToken = "valid_token";
		UserQueryResponse response =
				new UserQueryResponse(
						userId,
						"test@example.com",
						"Tester",
						"img.jpg",
						UserRole.ROLE_USER,
						UserStatus.ACTIVE);

		given(jwtProvider.validateToken(accessToken)).willReturn(true);
		given(jwtProvider.getJti(accessToken)).willReturn("jti");
		given(tokenBlacklistService.isBlacklisted("jti")).willReturn(false);
		given(jwtProvider.getUserId(accessToken)).willReturn(userId);

		given(userQueryService.getUserInfo(userId)).willReturn(response);

		mockMvc.perform(get("/api/users/me").cookie(new Cookie("access_token", accessToken)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.email").value("test@example.com"))
				.andExpect(jsonPath("$.name").value("Tester"));
	}

	@Test
	@DisplayName("카드 목록 조회 성공")
	void getCards_Success() throws Exception {
		UUID userId = UUID.randomUUID();
		String accessToken = "valid_token";
		UserCardQueryResponse card1 =
				new UserCardQueryResponse(1L, "Card 1", "1234", CardCompany.HYUNDAI);
		List<UserCardQueryResponse> response = Arrays.asList(card1);

		given(jwtProvider.validateToken(accessToken)).willReturn(true);
		given(jwtProvider.getJti(accessToken)).willReturn("jti");
		given(tokenBlacklistService.isBlacklisted("jti")).willReturn(false);
		given(jwtProvider.getUserId(accessToken)).willReturn(userId);

		given(userQueryService.getCards(userId)).willReturn(response);

		mockMvc.perform(get("/api/users/cards").cookie(new Cookie("access_token", accessToken)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].nickName").value("Card 1"))
				.andExpect(jsonPath("$[0].cardCompany").value("HYUNDAI"));
	}

	@Test
	@DisplayName("카드사 목록 조회 성공")
	void getCardCompanies_Success() throws Exception {
		List<CardCompany> companies = Arrays.asList(CardCompany.HYUNDAI, CardCompany.SAMSUNG);

		given(userQueryService.getCardCompanies()).willReturn(companies);

		mockMvc.perform(get("/api/users/cards/companies"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0]").value("HYUNDAI"))
				.andExpect(jsonPath("$[1]").value("SAMSUNG"));
	}
}
