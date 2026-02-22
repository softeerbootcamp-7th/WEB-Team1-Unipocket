package com.genesis.unipocket.exchange.query.presentation;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.genesis.unipocket.auth.command.application.JwtProvider;
import com.genesis.unipocket.auth.command.application.TokenBlacklistService;
import com.genesis.unipocket.exchange.query.application.ExchangeRateService;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ExchangeRateQueryController.class)
class ExchangeRateQueryControllerTest {

	@Autowired private MockMvc mockMvc;

	@MockitoBean private ExchangeRateService exchangeRateService;
	@MockitoBean private JwtProvider jwtProvider;
	@MockitoBean private TokenBlacklistService tokenBlacklistService;

	@SuppressWarnings("unused")
	@MockitoBean
	private JpaMetamodelMappingContext jpaMetamodelMappingContext;

	@Test
	@DisplayName("통화 간 환율 조회 성공")
	void getExchangeRate_Success() throws Exception {
		OffsetDateTime occurredAt = OffsetDateTime.parse("2026-02-22T10:30:00Z");
		BigDecimal exchangeRate = new BigDecimal("0.0091");

		given(exchangeRateService.getExchangeRate(CurrencyCode.KRW, CurrencyCode.JPY, occurredAt))
				.willReturn(exchangeRate);

		mockMvc.perform(
						get("/exchange-rate")
								.param("occurredAt", occurredAt.toString())
								.param("baseCurrencyCode", "KRW")
								.param("localCurrencyCode", "JPY"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.occurredAt").value("2026-02-22T10:30:00Z"))
				.andExpect(jsonPath("$.baseCurrencyCode").value("KRW"))
				.andExpect(jsonPath("$.localCurrencyCode").value("JPY"))
				.andExpect(jsonPath("$.exchangeRate").value(0.0091));

		verify(exchangeRateService).getExchangeRate(CurrencyCode.KRW, CurrencyCode.JPY, occurredAt);
	}

	@Test
	@DisplayName("통화 간 환율 조회 실패 - 필수 파라미터 누락")
	void getExchangeRate_Fail_WhenOccurredAtMissing() throws Exception {
		mockMvc.perform(
						get("/exchange-rate")
								.param("baseCurrencyCode", "KRW")
								.param("localCurrencyCode", "JPY"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("400_INVALID_INPUT_VALUE"));
	}
}
