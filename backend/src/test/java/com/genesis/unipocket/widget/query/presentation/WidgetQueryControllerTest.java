package com.genesis.unipocket.widget.query.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.genesis.unipocket.analysis.command.persistence.repository.AccountMonthlyAggregateRepository;
import com.genesis.unipocket.analysis.command.persistence.repository.AccountMonthlyCategoryAggregateRepository;
import com.genesis.unipocket.analysis.command.persistence.repository.AnalysisMonthlyDirtyRepository;
import com.genesis.unipocket.analysis.command.persistence.repository.PairMonthlyAggregateRepository;
import com.genesis.unipocket.auth.command.application.JwtProvider;
import com.genesis.unipocket.auth.command.application.TokenBlacklistService;
import com.genesis.unipocket.auth.common.constant.AuthCookieConstants;
import com.genesis.unipocket.global.common.enums.WidgetType;
import com.genesis.unipocket.user.command.persistence.repository.UserCommandRepository;
import com.genesis.unipocket.widget.query.application.WidgetQueryService;
import jakarta.servlet.http.Cookie;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(WidgetQueryController.class)
class WidgetQueryControllerTest {

	@Autowired private MockMvc mockMvc;

	@MockitoBean private WidgetQueryService widgetQueryService;
	@MockitoBean private JwtProvider jwtProvider;
	@MockitoBean private TokenBlacklistService tokenBlacklistService;
	@MockitoBean private UserCommandRepository userRepository;
	@MockitoBean private AnalysisMonthlyDirtyRepository analysisMonthlyDirtyRepository;
	@MockitoBean private AccountMonthlyAggregateRepository accountMonthlyAggregateRepository;

	@MockitoBean
	private AccountMonthlyCategoryAggregateRepository accountMonthlyCategoryAggregateRepository;

	@MockitoBean private PairMonthlyAggregateRepository pairMonthlyAggregateRepository;

	@SuppressWarnings("unused")
	@MockitoBean
	private JpaMetamodelMappingContext jpaMetamodelMappingContext;

	@Test
	@DisplayName("가계부 위젯 데이터 조회 성공")
	void getWidget_Success() throws Exception {
		UUID userId = UUID.randomUUID();
		String accessToken = "valid_token";
		Long accountBookId = 1L;

		mockAuthentication(accessToken, userId);
		given(
						widgetQueryService.getWidget(
								eq(userId), eq(accountBookId), any(), any(), any(), any()))
				.willReturn(Map.of("data", "test"));

		mockMvc.perform(
						get("/account-books/{accountBookId}/widget", accountBookId)
								.param("widgetType", WidgetType.BUDGET.name())
								.cookie(new Cookie(AuthCookieConstants.ACCESS_TOKEN, accessToken)))
				.andExpect(status().isOk());
	}

	@Test
	@DisplayName("여행 위젯 데이터 조회 성공")
	void getTravelWidget_Success() throws Exception {
		UUID userId = UUID.randomUUID();
		String accessToken = "valid_token";
		Long accountBookId = 1L;
		Long travelId = 2L;

		mockAuthentication(accessToken, userId);
		given(
						widgetQueryService.getWidget(
								eq(userId), eq(accountBookId), eq(travelId), any(), any(), any()))
				.willReturn(Map.of("data", "travel-test"));

		mockMvc.perform(
						get(
										"/account-books/{accountBookId}/travels/{travelId}/widget",
										accountBookId,
										travelId)
								.param("widgetType", WidgetType.BUDGET.name())
								.cookie(new Cookie(AuthCookieConstants.ACCESS_TOKEN, accessToken)))
				.andExpect(status().isOk());
	}

	private void mockAuthentication(String accessToken, UUID userId) {
		given(jwtProvider.validateToken(accessToken)).willReturn(true);
		given(jwtProvider.getJti(accessToken)).willReturn("jti");
		given(tokenBlacklistService.isBlacklisted("jti")).willReturn(false);
		given(jwtProvider.getUserId(accessToken)).willReturn(userId);
	}
}
