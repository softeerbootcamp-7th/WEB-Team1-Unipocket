package com.genesis.unipocket.travel.command.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.genesis.unipocket.auth.command.application.JwtProvider;
import com.genesis.unipocket.auth.command.application.TokenBlacklistService;
import com.genesis.unipocket.auth.common.constant.AuthCookieConstants;
import com.genesis.unipocket.travel.command.application.result.TravelBudgetUpdateResult;
import com.genesis.unipocket.travel.command.facade.TravelCommandFacade;
import com.genesis.unipocket.travel.command.facade.port.dto.TravelImageUploadPathInfo;
import com.genesis.unipocket.travel.command.presentation.request.TravelBudgetUpdateRequest;
import com.genesis.unipocket.travel.command.presentation.request.TravelImageUploadPathRequest;
import com.genesis.unipocket.travel.command.presentation.request.TravelRequest;
import com.genesis.unipocket.travel.command.presentation.request.TravelUpdateRequest;
import com.genesis.unipocket.user.command.persistence.repository.UserCommandRepository;
import jakarta.servlet.http.Cookie;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TravelCommandController.class)
class TravelCommandControllerTest {

	@Autowired private MockMvc mockMvc;
	@Autowired private ObjectMapper objectMapper;

	@MockitoBean private TravelCommandFacade travelCommandFacade;
	@MockitoBean private JwtProvider jwtProvider;
	@MockitoBean private TokenBlacklistService tokenBlacklistService;
	@MockitoBean private UserCommandRepository userRepository;

	@SuppressWarnings("unused")
	@MockitoBean
	private JpaMetamodelMappingContext jpaMetamodelMappingContext;

	@Test
	@DisplayName("여행 생성 성공")
	void createTravel_Success() throws Exception {
		UUID userId = UUID.randomUUID();
		String accessToken = "valid_token";
		Long accountBookId = 1L;
		Long travelId = 5L;
		TravelRequest request =
				new TravelRequest("도쿄", LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 7), null);

		mockAuthentication(accessToken, userId);
		given(travelCommandFacade.createTravel(any(), eq(userId))).willReturn(travelId);

		mockMvc.perform(
						post("/account-books/{accountBookId}/travels", accountBookId)
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request))
								.cookie(new Cookie(AuthCookieConstants.ACCESS_TOKEN, accessToken)))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", "/account-books/1/travels/5"));
	}

	@Test
	@DisplayName("여행 생성 실패 - 여행지 이름 누락")
	void createTravel_FailValidation() throws Exception {
		UUID userId = UUID.randomUUID();
		String accessToken = "valid_token";
		Long accountBookId = 1L;
		TravelRequest request =
				new TravelRequest("", LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 7), null);

		mockAuthentication(accessToken, userId);

		mockMvc.perform(
						post("/account-books/{accountBookId}/travels", accountBookId)
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request))
								.cookie(new Cookie(AuthCookieConstants.ACCESS_TOKEN, accessToken)))
				.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("여행 이미지 업로드 URL 발급 성공")
	void issueTravelImageUploadPath_Success() throws Exception {
		UUID userId = UUID.randomUUID();
		String accessToken = "valid_token";
		Long accountBookId = 1L;
		TravelImageUploadPathRequest request = new TravelImageUploadPathRequest("image/jpeg");
		TravelImageUploadPathInfo info =
				new TravelImageUploadPathInfo("https://s3.example.com/upload", "travel/img123.jpg");

		mockAuthentication(accessToken, userId);
		given(travelCommandFacade.issueTravelImageUploadPath(accountBookId, userId, "image/jpeg"))
				.willReturn(info);

		mockMvc.perform(
						post(
										"/account-books/{accountBookId}/travels/images/presigned-url",
										accountBookId)
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request))
								.cookie(new Cookie(AuthCookieConstants.ACCESS_TOKEN, accessToken)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.presignedUrl").value("https://s3.example.com/upload"))
				.andExpect(jsonPath("$.imageKey").value("travel/img123.jpg"));
	}

	@Test
	@DisplayName("여행 수정 성공")
	void updateTravel_Success() throws Exception {
		UUID userId = UUID.randomUUID();
		String accessToken = "valid_token";
		Long accountBookId = 1L;
		Long travelId = 5L;
		TravelRequest request =
				new TravelRequest("오사카", LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 5), null);

		mockAuthentication(accessToken, userId);
		doNothing().when(travelCommandFacade).updateTravel(eq(accountBookId), any(), eq(userId));

		mockMvc.perform(
						put(
										"/account-books/{accountBookId}/travels/{travelId}",
										accountBookId,
										travelId)
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request))
								.cookie(new Cookie(AuthCookieConstants.ACCESS_TOKEN, accessToken)))
				.andExpect(status().isOk());
	}

	@Test
	@DisplayName("여행 예산 수정 성공")
	void updateTravelBudget_Success() throws Exception {
		UUID userId = UUID.randomUUID();
		String accessToken = "valid_token";
		Long accountBookId = 1L;
		Long travelId = 5L;
		TravelBudgetUpdateRequest request =
				new TravelBudgetUpdateRequest(new BigDecimal("500000.00"));
		TravelBudgetUpdateResult result =
				new TravelBudgetUpdateResult(
						travelId, new BigDecimal("500000.00"), LocalDateTime.now());

		mockAuthentication(accessToken, userId);
		given(
						travelCommandFacade.updateTravelBudget(
								accountBookId, travelId, new BigDecimal("500000.00"), userId))
				.willReturn(result);

		mockMvc.perform(
						patch(
										"/account-books/{accountBookId}/travels/{travelId}/budget",
										accountBookId,
										travelId)
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request))
								.cookie(new Cookie(AuthCookieConstants.ACCESS_TOKEN, accessToken)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.travelId").value(travelId));
	}

	@Test
	@DisplayName("여행 패치 수정 성공")
	void patchTravel_Success() throws Exception {
		UUID userId = UUID.randomUUID();
		String accessToken = "valid_token";
		Long accountBookId = 1L;
		Long travelId = 5L;
		TravelUpdateRequest request = new TravelUpdateRequest("후쿠오카", null, null, null);

		mockAuthentication(accessToken, userId);
		doNothing().when(travelCommandFacade).patchTravel(any(), eq(userId));

		mockMvc.perform(
						patch(
										"/account-books/{accountBookId}/travels/{travelId}",
										accountBookId,
										travelId)
								.contentType(MediaType.APPLICATION_JSON)
								.content(objectMapper.writeValueAsString(request))
								.cookie(new Cookie(AuthCookieConstants.ACCESS_TOKEN, accessToken)))
				.andExpect(status().isOk());
	}

	@Test
	@DisplayName("여행 삭제 성공")
	void deleteTravel_Success() throws Exception {
		UUID userId = UUID.randomUUID();
		String accessToken = "valid_token";
		Long accountBookId = 1L;
		Long travelId = 5L;

		mockAuthentication(accessToken, userId);
		doNothing().when(travelCommandFacade).deleteTravel(travelId, userId);

		mockMvc.perform(
						delete(
										"/account-books/{accountBookId}/travels/{travelId}",
										accountBookId,
										travelId)
								.cookie(new Cookie(AuthCookieConstants.ACCESS_TOKEN, accessToken)))
				.andExpect(status().isNoContent());
	}

	private void mockAuthentication(String accessToken, UUID userId) {
		given(jwtProvider.validateToken(accessToken)).willReturn(true);
		given(jwtProvider.getJti(accessToken)).willReturn("jti");
		given(tokenBlacklistService.isBlacklisted("jti")).willReturn(false);
		given(jwtProvider.getUserId(accessToken)).willReturn(userId);
	}
}
