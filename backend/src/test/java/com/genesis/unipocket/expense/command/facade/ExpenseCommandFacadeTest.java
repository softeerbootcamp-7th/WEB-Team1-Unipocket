package com.genesis.unipocket.expense.command.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.genesis.unipocket.analysis.command.application.AnalysisMonthlyDirtyMarkerService;
import com.genesis.unipocket.expense.command.application.ExpenseCommandService;
import com.genesis.unipocket.expense.command.application.command.ExpenseCreateCommand;
import com.genesis.unipocket.expense.command.application.command.ExpenseUpdateCommand;
import com.genesis.unipocket.expense.command.application.result.ExpenseResult;
import com.genesis.unipocket.expense.command.facade.port.AccountBookFetchService;
import com.genesis.unipocket.expense.command.facade.port.dto.AccountBookInfo;
import com.genesis.unipocket.expense.command.presentation.request.ExpenseBulkUpdateItemRequest;
import com.genesis.unipocket.expense.command.presentation.request.ExpenseBulkUpdateRequest;
import com.genesis.unipocket.expense.command.presentation.request.ExpenseManualCreateRequest;
import com.genesis.unipocket.expense.command.presentation.request.ExpenseUpdateRequest;
import com.genesis.unipocket.expense.common.facade.port.UserCardFetchService;
import com.genesis.unipocket.expense.common.facade.port.dto.UserCardInfo;
import com.genesis.unipocket.expense.common.validation.ExpenseOwnershipValidator;
import com.genesis.unipocket.global.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CountryCode;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.global.common.enums.ExpenseSource;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.global.util.CountryCodeTimezoneMapper;
import com.genesis.unipocket.user.common.enums.CardCompany;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExpenseCommandFacadeTest {

	@Mock private ExpenseCommandService expenseService;
	@Mock private UserCardFetchService userCardFetchService;
	@Mock private AccountBookFetchService accountBookFetchService;
	@Mock private AnalysisMonthlyDirtyMarkerService analysisMonthlyDirtyMarkerService;
	@Mock private ExpenseOwnershipValidator expenseOwnershipValidator;

	@InjectMocks private ExpenseCommandFacade expenseCommandFacade;

	@Test
	@DisplayName("수기 생성 - command 생성/호출 후 카드 정보를 결합한다")
	void createExpenseManual_linearFlow() {
		Long accountBookId = 7L;
		UUID userId = UUID.randomUUID();
		Instant occurredAt = Instant.parse("2026-02-17T03:04:05Z");
		ExpenseManualCreateRequest request =
				new ExpenseManualCreateRequest(
						"스타벅스",
						Category.FOOD,
						11L,
						occurredAt,
						new BigDecimal("100.00"),
						null,
						null,
						"memo",
						55L);

		when(accountBookFetchService.getAccountBook(accountBookId, userId.toString()))
				.thenReturn(
						new AccountBookInfo(
								accountBookId,
								userId.toString(),
								CountryCode.KR,
								CountryCode.US,
								LocalDate.of(2026, 1, 1),
								LocalDate.of(2026, 12, 31)));
		when(expenseService.createExpenseManual(any(ExpenseCreateCommand.class)))
				.thenReturn(
						new ExpenseResult(
								1L,
								accountBookId,
								55L,
								Category.FOOD,
								CurrencyCode.KRW,
								new BigDecimal("130000.00"),
								new BigDecimal("1300.00"),
								CurrencyCode.USD,
								new BigDecimal("100.00"),
								occurredAt.atOffset(ZoneOffset.UTC),
								occurredAt.atOffset(ZoneOffset.UTC),
								"스타벅스",
								null,
								11L,
								null,
								null,
								null,
								ExpenseSource.MANUAL,
								null,
								"memo",
								null));
		when(userCardFetchService.getUserCard(11L))
				.thenReturn(
						Optional.of(new UserCardInfo(11L, CardCompany.SAMSUNG, "main", "1234")));
		when(userCardFetchService.getUserCardOwnedBy(11L, userId.toString()))
				.thenReturn(
						Optional.of(new UserCardInfo(11L, CardCompany.SAMSUNG, "main", "1234")));

		ExpenseResult result =
				expenseCommandFacade.createExpenseManual(request, accountBookId, userId);

		ArgumentCaptor<ExpenseCreateCommand> commandCaptor =
				ArgumentCaptor.forClass(ExpenseCreateCommand.class);
		verify(expenseService).createExpenseManual(commandCaptor.capture());
		ExpenseCreateCommand command = commandCaptor.getValue();

		assertThat(command.localCurrencyCode()).isEqualTo(CurrencyCode.USD);
		assertThat(command.baseCurrencyCode()).isEqualTo(CurrencyCode.KRW);
		assertThat(command.userCardId()).isEqualTo(11L);
		assertThat(command.travelId()).isEqualTo(55L);
		assertThat(command.occurredAt()).isEqualTo(occurredAt.atOffset(ZoneOffset.UTC));

		assertThat(result.cardCompany()).isEqualTo(CardCompany.SAMSUNG);
		assertThat(result.cardLabel()).isEqualTo("main");
		assertThat(result.cardLastDigits()).isEqualTo("1234");

		verify(expenseOwnershipValidator).validateOwnership(accountBookId, userId.toString());
		verify(analysisMonthlyDirtyMarkerService)
				.markDirty(accountBookId, occurredAt.atOffset(ZoneOffset.UTC));
		verify(userCardFetchService).getUserCardOwnedBy(11L, userId.toString());
		verify(userCardFetchService).getUserCard(11L);
	}

	@Test
	@DisplayName("수기 생성 - 다른 사용자 카드면 CARD_NOT_OWNED 예외를 반환한다")
	void createExpenseManual_unownedCard_throwsCardNotOwned() {
		Long accountBookId = 7L;
		UUID userId = UUID.randomUUID();
		Instant occurredAt = Instant.parse("2026-02-17T03:04:05Z");
		ExpenseManualCreateRequest request =
				new ExpenseManualCreateRequest(
						"스타벅스",
						Category.FOOD,
						11L,
						occurredAt,
						new BigDecimal("100.00"),
						null,
						null,
						"memo",
						55L);

		when(accountBookFetchService.getAccountBook(accountBookId, userId.toString()))
				.thenReturn(
						new AccountBookInfo(
								accountBookId,
								userId.toString(),
								CountryCode.KR,
								CountryCode.US,
								LocalDate.of(2026, 1, 1),
								LocalDate.of(2026, 12, 31)));
		when(userCardFetchService.getUserCardOwnedBy(11L, userId.toString()))
				.thenReturn(Optional.empty());
		when(userCardFetchService.getUserCard(11L))
				.thenReturn(
						Optional.of(new UserCardInfo(11L, CardCompany.SAMSUNG, "other", "1234")));

		assertThatThrownBy(
						() ->
								expenseCommandFacade.createExpenseManual(
										request, accountBookId, userId))
				.isInstanceOf(BusinessException.class)
				.satisfies(
						ex ->
								assertThat(((BusinessException) ex).getCode())
										.isEqualTo(ErrorCode.CARD_NOT_OWNED));

		verify(expenseService, never()).createExpenseManual(any());
	}

	@Test
	@DisplayName("수기 생성 - 가계부 종료일이 없으면 오늘까지만 허용한다")
	void createExpenseManual_whenAccountBookEndDateIsNull_allowsUntilTodayOnly() {
		Long accountBookId = 7L;
		UUID userId = UUID.randomUUID();
		ZoneId accountBookZoneId = CountryCodeTimezoneMapper.getZoneId(CountryCode.US);
		LocalDate tomorrow = LocalDate.now(accountBookZoneId).plusDays(1);
		Instant occurredAt = tomorrow.atTime(12, 0).atZone(accountBookZoneId).toInstant();
		ExpenseManualCreateRequest request =
				new ExpenseManualCreateRequest(
						"스타벅스",
						Category.FOOD,
						null,
						occurredAt,
						new BigDecimal("100.00"),
						null,
						null,
						"memo",
						null);

		when(accountBookFetchService.getAccountBook(accountBookId, userId.toString()))
				.thenReturn(
						new AccountBookInfo(
								accountBookId,
								userId.toString(),
								CountryCode.KR,
								CountryCode.US,
								LocalDate.now(accountBookZoneId).minusDays(30),
								null));

		assertThatThrownBy(
						() ->
								expenseCommandFacade.createExpenseManual(
										request, accountBookId, userId))
				.isInstanceOf(BusinessException.class)
				.satisfies(
						ex ->
								assertThat(((BusinessException) ex).getCode())
										.isEqualTo(ErrorCode.EXPENSE_OUT_OF_ACCOUNT_BOOK_PERIOD));

		verify(expenseService, never()).createExpenseManual(any());
		verify(userCardFetchService, never()).getUserCardOwnedBy(any(), any());
	}

	@Test
	@DisplayName("수기 생성 - 존재하지 않는 카드면 CARD_NOT_FOUND 예외를 반환한다")
	void createExpenseManual_missingCard_throwsCardNotFound() {
		Long accountBookId = 7L;
		UUID userId = UUID.randomUUID();
		Instant occurredAt = Instant.parse("2026-02-17T03:04:05Z");
		ExpenseManualCreateRequest request =
				new ExpenseManualCreateRequest(
						"스타벅스",
						Category.FOOD,
						999L,
						occurredAt,
						new BigDecimal("100.00"),
						null,
						null,
						"memo",
						55L);

		when(accountBookFetchService.getAccountBook(accountBookId, userId.toString()))
				.thenReturn(
						new AccountBookInfo(
								accountBookId,
								userId.toString(),
								CountryCode.KR,
								CountryCode.US,
								LocalDate.of(2026, 1, 1),
								LocalDate.of(2026, 12, 31)));
		when(userCardFetchService.getUserCardOwnedBy(999L, userId.toString()))
				.thenReturn(Optional.empty());
		when(userCardFetchService.getUserCard(999L)).thenReturn(Optional.empty());

		assertThatThrownBy(
						() ->
								expenseCommandFacade.createExpenseManual(
										request, accountBookId, userId))
				.isInstanceOf(BusinessException.class)
				.satisfies(
						ex ->
								assertThat(((BusinessException) ex).getCode())
										.isEqualTo(ErrorCode.CARD_NOT_FOUND));

		verify(expenseService, never()).createExpenseManual(any());
	}

	@Test
	@DisplayName("수정 - command 생성/호출 후 카드 정보를 결합한다")
	void updateExpense_linearFlow() {
		Long accountBookId = 7L;
		Long expenseId = 99L;
		UUID userId = UUID.randomUUID();
		Instant occurredAt = Instant.parse("2026-02-17T03:04:05Z");
		ExpenseUpdateRequest request =
				new ExpenseUpdateRequest(
						"택시",
						Category.TRANSPORT,
						null,
						occurredAt,
						new BigDecimal("1000.00"),
						CurrencyCode.JPY,
						null,
						"memo",
						77L);

		when(accountBookFetchService.getAccountBook(accountBookId, userId.toString()))
				.thenReturn(
						new AccountBookInfo(
								accountBookId,
								userId.toString(),
								CountryCode.KR,
								CountryCode.US,
								LocalDate.of(2026, 1, 1),
								LocalDate.of(2026, 12, 31)));
		when(expenseService.updateExpense(any(ExpenseUpdateCommand.class)))
				.thenReturn(
						new ExpenseResult(
								expenseId,
								accountBookId,
								77L,
								Category.TRANSPORT,
								CurrencyCode.KRW,
								new BigDecimal("10000.00"),
								new BigDecimal("10.00"),
								CurrencyCode.JPY,
								new BigDecimal("1000.00"),
								occurredAt.atOffset(ZoneOffset.UTC),
								occurredAt.atOffset(ZoneOffset.UTC),
								"택시",
								null,
								null,
								null,
								null,
								null,
								ExpenseSource.MANUAL,
								null,
								"memo",
								null));
		ExpenseResult result =
				expenseCommandFacade.updateExpense(expenseId, accountBookId, userId, request);

		ArgumentCaptor<ExpenseUpdateCommand> commandCaptor =
				ArgumentCaptor.forClass(ExpenseUpdateCommand.class);
		verify(expenseService).updateExpense(commandCaptor.capture());
		ExpenseUpdateCommand command = commandCaptor.getValue();

		assertThat(command.expenseId()).isEqualTo(expenseId);
		assertThat(command.localCurrencyCode()).isEqualTo(CurrencyCode.JPY);
		assertThat(command.baseCurrencyCode()).isEqualTo(CurrencyCode.KRW);
		assertThat(command.travelId()).isEqualTo(77L);
		assertThat(result.cardCompany()).isNull();

		verify(expenseOwnershipValidator)
				.validateOwnership(eq(accountBookId), eq(userId.toString()));
		verify(userCardFetchService, never()).getUserCard(any());
	}

	@Test
	@DisplayName("삭제 - 선형 위임 흐름으로 service 호출")
	void deleteExpense_linearFlow() {
		Long accountBookId = 7L;
		Long expenseId = 99L;
		UUID userId = UUID.randomUUID();

		expenseCommandFacade.deleteExpense(expenseId, accountBookId, userId);

		verify(expenseOwnershipValidator).validateOwnership(accountBookId, userId.toString());
		verify(expenseService).deleteExpense(expenseId, accountBookId);
	}

	@Test
	@DisplayName("일괄 수정 - 각 항목을 command로 변환해 순차 호출")
	void updateExpensesBulk_linearFlow() {
		Long accountBookId = 7L;
		UUID userId = UUID.randomUUID();
		Instant occurredAt = Instant.parse("2026-02-17T03:04:05Z");

		ExpenseBulkUpdateRequest request =
				new ExpenseBulkUpdateRequest(
						List.of(
								new ExpenseBulkUpdateItemRequest(
										101L,
										"택시",
										Category.TRANSPORT,
										null,
										occurredAt,
										new BigDecimal("1000.00"),
										CurrencyCode.JPY,
										null,
										"memo-1",
										1L),
								new ExpenseBulkUpdateItemRequest(
										102L,
										"카페",
										Category.FOOD,
										null,
										occurredAt.plusSeconds(60),
										new BigDecimal("50.00"),
										null,
										null,
										"memo-2",
										null)));

		when(accountBookFetchService.getAccountBook(accountBookId, userId.toString()))
				.thenReturn(
						new AccountBookInfo(
								accountBookId,
								userId.toString(),
								CountryCode.KR,
								CountryCode.US,
								LocalDate.of(2026, 1, 1),
								LocalDate.of(2026, 12, 31)));
		when(expenseService.updateExpense(any(ExpenseUpdateCommand.class)))
				.thenReturn(
						new ExpenseResult(
								101L,
								accountBookId,
								1L,
								Category.TRANSPORT,
								CurrencyCode.KRW,
								new BigDecimal("10000.00"),
								new BigDecimal("10.00"),
								CurrencyCode.JPY,
								new BigDecimal("1000.00"),
								occurredAt.atOffset(ZoneOffset.UTC),
								occurredAt.atOffset(ZoneOffset.UTC),
								"택시",
								null,
								null,
								null,
								null,
								null,
								ExpenseSource.MANUAL,
								null,
								"memo-1",
								null));
		List<ExpenseResult> results =
				expenseCommandFacade.updateExpensesBulk(accountBookId, userId, request);

		ArgumentCaptor<ExpenseUpdateCommand> commandCaptor =
				ArgumentCaptor.forClass(ExpenseUpdateCommand.class);
		verify(expenseService, times(2)).updateExpense(commandCaptor.capture());
		assertThat(commandCaptor.getAllValues()).hasSize(2);
		assertThat(commandCaptor.getAllValues().get(0).expenseId()).isEqualTo(101L);
		assertThat(commandCaptor.getAllValues().get(1).expenseId()).isEqualTo(102L);
		assertThat(commandCaptor.getAllValues().get(1).localCurrencyCode())
				.isEqualTo(CurrencyCode.USD);
		assertThat(results).hasSize(2);

		verify(expenseOwnershipValidator).validateOwnership(accountBookId, userId.toString());
		verify(userCardFetchService, never()).getUserCard(any());
	}

	@Test
	@DisplayName("일괄 수정 - 가계부 기간 밖 지출일시는 예외를 반환한다")
	void updateExpensesBulk_whenOccurredAtOutOfAccountBookPeriod_throws() {
		Long accountBookId = 7L;
		UUID userId = UUID.randomUUID();
		ZoneId accountBookZoneId = CountryCodeTimezoneMapper.getZoneId(CountryCode.US);
		LocalDate tomorrow = LocalDate.now(accountBookZoneId).plusDays(1);
		Instant occurredAt = tomorrow.atTime(12, 0).atZone(accountBookZoneId).toInstant();

		ExpenseBulkUpdateRequest request =
				new ExpenseBulkUpdateRequest(
						List.of(
								new ExpenseBulkUpdateItemRequest(
										101L,
										"택시",
										Category.TRANSPORT,
										null,
										occurredAt,
										new BigDecimal("1000.00"),
										CurrencyCode.JPY,
										null,
										"memo-1",
										null)));

		when(accountBookFetchService.getAccountBook(accountBookId, userId.toString()))
				.thenReturn(
						new AccountBookInfo(
								accountBookId,
								userId.toString(),
								CountryCode.KR,
								CountryCode.US,
								LocalDate.now(accountBookZoneId).minusDays(30),
								null));

		assertThatThrownBy(
						() ->
								expenseCommandFacade.updateExpensesBulk(
										accountBookId, userId, request))
				.isInstanceOf(BusinessException.class)
				.satisfies(
						ex ->
								assertThat(((BusinessException) ex).getCode())
										.isEqualTo(ErrorCode.EXPENSE_OUT_OF_ACCOUNT_BOOK_PERIOD));

		verify(expenseService, never()).updateExpense(any());
	}
}
