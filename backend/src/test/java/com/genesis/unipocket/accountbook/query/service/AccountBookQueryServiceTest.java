package com.genesis.unipocket.accountbook.query.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import com.genesis.unipocket.accountbook.query.persistence.repository.AccountBookQueryRepository;
import com.genesis.unipocket.accountbook.query.persistence.response.AccountBookDetailResponse;
import com.genesis.unipocket.accountbook.query.persistence.response.AccountBookExchangeRateResponse;
import com.genesis.unipocket.accountbook.query.persistence.response.AccountBookQueryResponse;
import com.genesis.unipocket.accountbook.query.persistence.response.AccountBookSummaryResponse;
import com.genesis.unipocket.expense.command.application.ExchangeRateService;
import com.genesis.unipocket.global.common.enums.CountryCode;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.user.command.persistence.entity.UserEntity;
import com.genesis.unipocket.user.command.persistence.repository.UserCommandRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccountBookQueryService 단위 테스트")
class AccountBookQueryServiceTest {

	@Mock private AccountBookQueryRepository repository;
	@Mock private UserCommandRepository userRepository;
	@Mock private ExchangeRateService exchangeRateService;

	@InjectMocks private AccountBookQueryService accountBookQueryService;

	private final String userId = UUID.randomUUID().toString();

	@Test
	@DisplayName("가계부 조회 - 성공")
	void getAccountBook_Success() {
		Long accountBookId = 1L;
		AccountBookQueryResponse response =
				new AccountBookQueryResponse(
						accountBookId,
						"Title",
						CountryCode.US,
						CountryCode.KR,
						LocalDate.now(),
						LocalDate.now());

		given(repository.findById(accountBookId)).willReturn(Optional.of(response));

		AccountBookQueryResponse result = accountBookQueryService.getAccountBook(accountBookId);

		assertThat(result.id()).isEqualTo(accountBookId);
		assertThat(result.title()).isEqualTo("Title");
	}

	@Test
	@DisplayName("가계부 조회 - 실패 (존재하지 않음)")
	void getAccountBook_NotFound() {
		Long accountBookId = 1L;
		given(repository.findById(accountBookId)).willReturn(Optional.empty());

		assertThatThrownBy(() -> accountBookQueryService.getAccountBook(accountBookId))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("code", ErrorCode.ACCOUNT_BOOK_NOT_FOUND);
	}

	@Test
	@DisplayName("내 가계부 목록 조회 - 성공")
	void getAccountBooks_Success() {
		AccountBookSummaryResponse response1 = new AccountBookSummaryResponse(1L, "Title1", true);
		AccountBookSummaryResponse response2 = new AccountBookSummaryResponse(2L, "Title2", false);
		UserEntity user =
				UserEntity.builder().name("tester").email("t@t.com").mainBucketId(1L).build();
		given(userRepository.findById(UUID.fromString(userId))).willReturn(Optional.of(user));
		given(repository.findAllByUserId(UUID.fromString(userId), 1L))
				.willReturn(List.of(response1, response2));

		List<AccountBookSummaryResponse> result = accountBookQueryService.getAccountBooks(userId);

		assertThat(result).hasSize(2);
		assertThat(result.get(0).title()).isEqualTo("Title1");
	}

	@Test
	@DisplayName("가계부 상세 조회 - 성공")
	void getAccountBookDetail_Success() {
		Long accountBookId = 1L;
		AccountBookDetailResponse response =
				new AccountBookDetailResponse(
						accountBookId,
						"Title",
						CountryCode.US,
						CountryCode.KR,
						BigDecimal.valueOf(10000),
						LocalDateTime.of(2026, 2, 12, 8, 0, 0),
						List.of(),
						LocalDate.now(),
						LocalDate.now());

		given(repository.findDetailById(UUID.fromString(userId), accountBookId))
				.willReturn(Optional.of(response));

		AccountBookDetailResponse result =
				accountBookQueryService.getAccountBookDetail(userId, accountBookId);

		assertThat(result.id()).isEqualTo(accountBookId);
		assertThat(result.title()).isEqualTo("Title");
	}

	@Test
	@DisplayName("가계부 상세 조회 - 실패 (존재하지 않음 또는 권한 없음)")
	void getAccountBookDetail_NotFoundOrUnauthorized() {
		Long accountBookId = 1L;
		given(repository.findDetailById(UUID.fromString(userId), accountBookId))
				.willReturn(Optional.empty());

		assertThatThrownBy(
						() -> accountBookQueryService.getAccountBookDetail(userId, accountBookId))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("code", ErrorCode.ACCOUNT_BOOK_NOT_FOUND);
	}

	@Test
	@DisplayName("가계부 기준/상대 국가 환율 조회 - 성공")
	void getAccountBookExchangeRate_Success() {
		Long accountBookId = 1L;
		AccountBookDetailResponse accountBookDetailResponse =
				new AccountBookDetailResponse(
						accountBookId,
						"Title",
						CountryCode.US,
						CountryCode.KR,
						BigDecimal.valueOf(10000),
						LocalDateTime.of(2026, 2, 12, 8, 0, 0),
						List.of(),
						LocalDate.now(),
						LocalDate.now());

		given(repository.findDetailById(UUID.fromString(userId), accountBookId))
				.willReturn(Optional.of(accountBookDetailResponse));
		given(
						exchangeRateService.getExchangeRate(
								eq(CurrencyCode.KRW),
								eq(CurrencyCode.USD),
								any(LocalDateTime.class)))
				.willReturn(BigDecimal.valueOf(0.00075));

		AccountBookExchangeRateResponse result =
				accountBookQueryService.getAccountBookExchangeRate(userId, accountBookId);

		assertThat(result.baseCountryCode()).isEqualTo(CountryCode.KR);
		assertThat(result.localCountryCode()).isEqualTo(CountryCode.US);
		assertThat(result.exchangeRate()).isEqualByComparingTo("0.00075");
		assertThat(result.budgetCreatedAt()).isEqualTo(LocalDateTime.of(2026, 2, 12, 8, 0, 0));
	}

	@Test
	@DisplayName("가계부 기준/상대 국가 환율 조회 - 실패 (예산 미설정)")
	void getAccountBookExchangeRate_Fail_WhenBudgetNotSet() {
		Long accountBookId = 1L;
		AccountBookDetailResponse accountBookDetailResponse =
				new AccountBookDetailResponse(
						accountBookId,
						"Title",
						CountryCode.US,
						CountryCode.KR,
						null,
						null,
						List.of(),
						LocalDate.now(),
						LocalDate.now());

		given(repository.findDetailById(UUID.fromString(userId), accountBookId))
				.willReturn(Optional.of(accountBookDetailResponse));

		assertThatThrownBy(
						() ->
								accountBookQueryService.getAccountBookExchangeRate(
										userId, accountBookId))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("code", ErrorCode.ACCOUNT_BOOK_BUDGET_NOT_SET);
	}
}
