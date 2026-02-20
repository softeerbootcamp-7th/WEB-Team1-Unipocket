package com.genesis.unipocket.expense.command.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.genesis.unipocket.expense.application.result.ExpenseResult;
import com.genesis.unipocket.expense.command.application.ExpenseCommandContextService;
import com.genesis.unipocket.expense.command.application.ExpenseCommandService;
import com.genesis.unipocket.expense.command.application.command.ExpenseCreateCommand;
import com.genesis.unipocket.expense.command.application.command.ExpenseUpdateCommand;
import com.genesis.unipocket.expense.command.facade.port.AccountBookInfoFetchService;
import com.genesis.unipocket.expense.command.presentation.request.ExpenseManualCreateRequest;
import com.genesis.unipocket.expense.command.presentation.request.ExpenseUpdateRequest;
import com.genesis.unipocket.global.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CountryCode;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.global.common.enums.ExpenseSource;
import com.genesis.unipocket.tempexpense.command.facade.port.AccountBookOwnershipValidator;
import com.genesis.unipocket.tempexpense.command.facade.port.dto.AccountBookInfo;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;
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
	@Mock private ExpenseCommandContextService expenseCommandContextService;
	@Mock private AccountBookInfoFetchService accountBookInfoFetchService;
	@Mock private AccountBookOwnershipValidator accountBookOwnershipValidator;

	@InjectMocks private ExpenseCommandFacade expenseCommandFacade;

	@Test
	@DisplayName("수기 생성 - 선형 위임 흐름으로 command 생성 및 service 호출")
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

		when(accountBookInfoFetchService.getAccountBook(accountBookId, userId.toString()))
				.thenReturn(
						new AccountBookInfo(
								accountBookId, userId.toString(), CountryCode.KR, CountryCode.US));
		when(expenseCommandContextService.resolveLocalCurrencyCode(null, CurrencyCode.USD))
				.thenReturn(CurrencyCode.USD);
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
								null,
								null,
								null,
								null,
								ExpenseSource.MANUAL,
								null,
								"memo",
								null));
		when(expenseCommandContextService.enrichWithCardInfo(any(ExpenseResult.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

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
		verify(accountBookOwnershipValidator).validateOwnership(accountBookId, userId.toString());
		verify(expenseCommandContextService).resolveLocalCurrencyCode(null, CurrencyCode.USD);
		verify(expenseCommandContextService).enrichWithCardInfo(any(ExpenseResult.class));
	}

	@Test
	@DisplayName("수정 - 선형 위임 흐름으로 command 생성 및 service 호출")
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

		when(accountBookInfoFetchService.getAccountBook(accountBookId, userId.toString()))
				.thenReturn(
						new AccountBookInfo(
								accountBookId, userId.toString(), CountryCode.KR, CountryCode.US));
		when(expenseCommandContextService.resolveLocalCurrencyCode(
						CurrencyCode.JPY, CurrencyCode.USD))
				.thenReturn(CurrencyCode.JPY);
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
		when(expenseCommandContextService.enrichWithCardInfo(any(ExpenseResult.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		expenseCommandFacade.updateExpense(expenseId, accountBookId, userId, request);

		ArgumentCaptor<ExpenseUpdateCommand> commandCaptor =
				ArgumentCaptor.forClass(ExpenseUpdateCommand.class);
		verify(expenseService).updateExpense(commandCaptor.capture());
		ExpenseUpdateCommand command = commandCaptor.getValue();

		assertThat(command.expenseId()).isEqualTo(expenseId);
		assertThat(command.localCurrencyCode()).isEqualTo(CurrencyCode.JPY);
		assertThat(command.baseCurrencyCode()).isEqualTo(CurrencyCode.KRW);
		assertThat(command.travelId()).isEqualTo(77L);
		verify(accountBookOwnershipValidator)
				.validateOwnership(eq(accountBookId), eq(userId.toString()));
		verify(expenseCommandContextService)
				.resolveLocalCurrencyCode(CurrencyCode.JPY, CurrencyCode.USD);
		verify(expenseCommandContextService).enrichWithCardInfo(any(ExpenseResult.class));
	}

	@Test
	@DisplayName("삭제 - 선형 위임 흐름으로 service 호출")
	void deleteExpense_linearFlow() {
		Long accountBookId = 7L;
		Long expenseId = 99L;
		UUID userId = UUID.randomUUID();

		expenseCommandFacade.deleteExpense(expenseId, accountBookId, userId);

		verify(accountBookOwnershipValidator).validateOwnership(accountBookId, userId.toString());
		verify(expenseService).deleteExpense(expenseId, accountBookId);
	}
}
