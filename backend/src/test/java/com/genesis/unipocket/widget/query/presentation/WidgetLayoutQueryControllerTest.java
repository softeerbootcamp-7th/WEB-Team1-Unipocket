package com.genesis.unipocket.widget.query.presentation;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.genesis.unipocket.auth.command.application.JwtProvider;
import com.genesis.unipocket.auth.command.application.TokenBlacklistService;
import com.genesis.unipocket.auth.common.constant.AuthCookieConstants;
import com.genesis.unipocket.user.command.persistence.repository.UserCommandRepository;
import com.genesis.unipocket.widget.common.validate.UserAccountBookValidator;
import com.genesis.unipocket.widget.common.validate.UserTravelValidator;
import com.genesis.unipocket.widget.query.application.WidgetLayoutQueryService;
import jakarta.servlet.http.Cookie;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(WidgetLayoutQueryController.class)
class WidgetLayoutQueryControllerTest {

	@Autowired private MockMvc mockMvc;

	@MockitoBean private WidgetLayoutQueryService widgetLayoutQueryService;
	@MockitoBean private UserAccountBookValidator userAccountBookValidator;
	@MockitoBean private UserTravelValidator userTravelValidator;
	@MockitoBean private JwtProvider jwtProvider;
	@MockitoBean private TokenBlacklistService tokenBlacklistService;
	@MockitoBean private UserCommandRepository userRepository;

	@SuppressWarnings("unused")
	@MockitoBean
	private JpaMetamodelMappingContext jpaMetamodelMappingContext;

	@Test
	@DisplayName("가계부 위젯 순서 조회 성공")
	void getAccountBookWidgets_Success() throws Exception {
		UUID userId = UUID.randomUUID();
		String accessToken = "valid_token";
		Long accountBookId = 1L;

		mockAuthentication(accessToken, userId);
		doNothing().when(userAccountBookValidator).validateUserAccountBook(userId, accountBookId);
		given(widgetLayoutQueryService.getAccountBookWidgets(accountBookId)).willReturn(List.of());

		mockMvc.perform(
						get("/account-books/{accountBookId}/widgets", accountBookId)
								.cookie(new Cookie(AuthCookieConstants.ACCESS_TOKEN, accessToken)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray());
	}

	@Test
	@DisplayName("여행 위젯 순서 조회 성공")
	void getTravelWidgets_Success() throws Exception {
		UUID userId = UUID.randomUUID();
		String accessToken = "valid_token";
		Long accountBookId = 1L;
		Long travelId = 2L;

		mockAuthentication(accessToken, userId);
		doNothing().when(userAccountBookValidator).validateUserAccountBook(userId, accountBookId);
		doNothing().when(userTravelValidator).validateTravelInAccountBook(accountBookId, travelId);
		given(widgetLayoutQueryService.getTravelWidgets(travelId)).willReturn(List.of());

		mockMvc.perform(
						get(
										"/account-books/{accountBookId}/travels/{travelId}/widgets",
										accountBookId,
										travelId)
								.cookie(new Cookie(AuthCookieConstants.ACCESS_TOKEN, accessToken)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray());
	}

	private void mockAuthentication(String accessToken, UUID userId) {
		given(jwtProvider.validateToken(accessToken)).willReturn(true);
		given(jwtProvider.getJti(accessToken)).willReturn("jti");
		given(tokenBlacklistService.isBlacklisted("jti")).willReturn(false);
		given(jwtProvider.getUserId(accessToken)).willReturn(userId);
	}
}
