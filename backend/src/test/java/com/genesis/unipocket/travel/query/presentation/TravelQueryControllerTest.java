package com.genesis.unipocket.travel.query.presentation;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.genesis.unipocket.auth.command.application.JwtProvider;
import com.genesis.unipocket.auth.command.application.TokenBlacklistService;
import com.genesis.unipocket.auth.common.constant.AuthCookieConstants;
import com.genesis.unipocket.global.common.enums.CountryCode;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.travel.query.application.TravelAmountQueryService;
import com.genesis.unipocket.travel.query.application.TravelQueryService;
import com.genesis.unipocket.travel.query.presentation.response.TravelAmountResponse;
import jakarta.servlet.http.Cookie;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TravelQueryController.class)
class TravelQueryControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private TravelQueryService travelQueryService;
	@MockitoBean
	private TravelAmountQueryService travelAmountQueryService;
	@MockitoBean
	private JwtProvider jwtProvider;
	@MockitoBean
	private TokenBlacklistService tokenBlacklistService;

	@SuppressWarnings("unused")
	@MockitoBean
	private JpaMetamodelMappingContext jpaMetamodelMappingContext;

	@Test
	@DisplayName("여행 지출 총액 조회 성공")
	void getTravelAmount_success() throws Exception {
		UUID userId = UUID.randomUUID();
		String accessToken = "valid_token";
		Long accountBookId = 1L;
		Long travelId = 2L;

		given(travelAmountQueryService.getTravelAmount(accountBookId, travelId, userId.toString()))
				.willReturn(
						new TravelAmountResponse(
								CountryCode.JP,
								CurrencyCode.JPY,
								CountryCode.KR,
								CurrencyCode.KRW,
								new BigDecimal("1200.00"),
								new BigDecimal("11000.00"),
								null,
								null));
		mockAuthentication(accessToken, userId);

		mockMvc.perform(
				get(
						"/account-books/{accountBookId}/travels/{travelId}/amount",
						accountBookId,
						travelId)
						.cookie(new Cookie(AuthCookieConstants.ACCESS_TOKEN, accessToken)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.localCountryCode").value("JP"))
				.andExpect(jsonPath("$.localCurrencyCode").value("JPY"))
				.andExpect(jsonPath("$.baseCountryCode").value("KR"))
				.andExpect(jsonPath("$.baseCurrencyCode").value("KRW"))
				.andExpect(jsonPath("$.totalLocalAmount").value("1200.00"))
				.andExpect(jsonPath("$.totalBaseAmount").value("11000.00"));

		verify(travelAmountQueryService)
				.getTravelAmount(accountBookId, travelId, userId.toString());
	}

	private void mockAuthentication(String accessToken, UUID userId) {
		given(jwtProvider.validateToken(accessToken)).willReturn(true);
		given(jwtProvider.getJti(accessToken)).willReturn("jti");
		given(tokenBlacklistService.isBlacklisted("jti")).willReturn(false);
		given(jwtProvider.getUserId(accessToken)).willReturn(userId);
	}
}
