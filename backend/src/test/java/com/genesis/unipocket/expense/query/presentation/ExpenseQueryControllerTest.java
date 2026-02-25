package com.genesis.unipocket.expense.query.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.genesis.unipocket.auth.command.application.JwtProvider;
import com.genesis.unipocket.auth.command.application.TokenBlacklistService;
import com.genesis.unipocket.auth.common.constant.AuthCookieConstants;
import com.genesis.unipocket.expense.query.facade.ExpenseQueryFacade;
import com.genesis.unipocket.expense.query.presentation.response.ExpenseFileUrlResponse;
import com.genesis.unipocket.expense.query.presentation.response.ExpenseListResponse;
import com.genesis.unipocket.expense.query.presentation.response.ExpenseMerchantSearchResponse;
import com.genesis.unipocket.expense.query.presentation.response.ExpenseResponse;
import com.genesis.unipocket.global.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.global.common.enums.ExpenseSource;
import com.genesis.unipocket.user.command.persistence.repository.UserCommandRepository;
import jakarta.servlet.http.Cookie;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ExpenseQueryController.class)
class ExpenseQueryControllerTest {

	@Autowired private MockMvc mockMvc;

	@MockitoBean private ExpenseQueryFacade expenseQueryFacade;
	@MockitoBean private JwtProvider jwtProvider;
	@MockitoBean private TokenBlacklistService tokenBlacklistService;
	@MockitoBean private UserCommandRepository userRepository;

	@SuppressWarnings("unused")
	@MockitoBean
	private JpaMetamodelMappingContext jpaMetamodelMappingContext;

	@Test
	@DisplayName("지출내역 상세 조회 성공")
	void getExpense_Success() throws Exception {
		UUID userId = UUID.randomUUID();
		String accessToken = "valid_token";
		Long accountBookId = 1L;
		Long expenseId = 10L;

		ExpenseResponse response =
				new ExpenseResponse(
						expenseId,
						accountBookId,
						null,
						"스타벅스",
						new BigDecimal("1300.00"),
						Category.FOOD,
						new ExpenseResponse.PaymentMethodResponse(true, null),
						OffsetDateTime.now(),
						OffsetDateTime.now(),
						"5000",
						CurrencyCode.JPY,
						"50000",
						CurrencyCode.KRW,
						"커피",
						ExpenseSource.MANUAL,
						null,
						null,
						null);

		mockAuthentication(accessToken, userId);
		given(expenseQueryFacade.getExpense(any(Long.class), any(Long.class), any(UUID.class)))
				.willReturn(response);

		mockMvc.perform(
						get(
										"/account-books/{accountBookId}/expenses/{expenseId}",
										accountBookId,
										expenseId)
								.cookie(new Cookie(AuthCookieConstants.ACCESS_TOKEN, accessToken)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.expenseId").value(expenseId))
				.andExpect(jsonPath("$.merchantName").value("스타벅스"))
				.andExpect(jsonPath("$.category").exists());
	}

	@Test
	@DisplayName("지출내역 리스트 조회 성공")
	void getExpenses_Success() throws Exception {
		UUID userId = UUID.randomUUID();
		String accessToken = "valid_token";
		Long accountBookId = 1L;

		ExpenseListResponse response = ExpenseListResponse.of(List.of(), 0, 0, 20);

		mockAuthentication(accessToken, userId);
		given(
						expenseQueryFacade.getExpenses(
								eq(accountBookId), eq(userId), any(), any(Pageable.class)))
				.willReturn(response);

		mockMvc.perform(
						get("/account-books/{accountBookId}/expenses", accountBookId)
								.cookie(new Cookie(AuthCookieConstants.ACCESS_TOKEN, accessToken)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalCount").value(0))
				.andExpect(jsonPath("$.expenses").isArray());
	}

	@Test
	@DisplayName("거래처명 자동완성 검색 성공")
	void searchMerchantNames_Success() throws Exception {
		UUID userId = UUID.randomUUID();
		String accessToken = "valid_token";
		Long accountBookId = 1L;

		ExpenseMerchantSearchResponse response =
				new ExpenseMerchantSearchResponse(List.of("스타벅스", "스타벅스 강남점"));

		mockAuthentication(accessToken, userId);
		given(expenseQueryFacade.searchMerchantNames(accountBookId, userId, "스타", 10))
				.willReturn(response);

		mockMvc.perform(
						get("/account-books/{accountBookId}/expenses/merchant-names", accountBookId)
								.param("q", "스타")
								.param("limit", "10")
								.cookie(new Cookie(AuthCookieConstants.ACCESS_TOKEN, accessToken)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.merchantNames").isArray())
				.andExpect(jsonPath("$.merchantNames[0]").value("스타벅스"));
	}

	@Test
	@DisplayName("지출 파일 열람 URL 발급 성공")
	void getExpenseFileUrl_Success() throws Exception {
		UUID userId = UUID.randomUUID();
		String accessToken = "valid_token";
		Long accountBookId = 1L;
		Long expenseId = 10L;

		ExpenseFileUrlResponse response =
				new ExpenseFileUrlResponse("https://s3.example.com/file.jpg?signed", 600);

		mockAuthentication(accessToken, userId);
		given(expenseQueryFacade.getExpenseFileUrl(expenseId, accountBookId, userId))
				.willReturn(response);

		mockMvc.perform(
						get(
										"/account-books/{accountBookId}/expenses/{expenseId}/file-url",
										accountBookId,
										expenseId)
								.cookie(new Cookie(AuthCookieConstants.ACCESS_TOKEN, accessToken)))
				.andExpect(status().isOk())
				.andExpect(
						jsonPath("$.presignedUrl").value("https://s3.example.com/file.jpg?signed"))
				.andExpect(jsonPath("$.expiresInSeconds").value(600));
	}

	private void mockAuthentication(String accessToken, UUID userId) {
		given(jwtProvider.validateToken(accessToken)).willReturn(true);
		given(jwtProvider.getJti(accessToken)).willReturn("jti");
		given(tokenBlacklistService.isBlacklisted("jti")).willReturn(false);
		given(jwtProvider.getUserId(accessToken)).willReturn(userId);
	}
}
