package com.genesis.unipocket.accountbook.command.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.genesis.unipocket.accountbook.command.application.AccountBookCommandService;
import com.genesis.unipocket.accountbook.command.application.result.AccountBookResult;
import com.genesis.unipocket.accountbook.command.facade.port.AccountBookDefaultWidgetPort;
import com.genesis.unipocket.accountbook.command.presentation.request.AccountBookUpdateRequest;
import com.genesis.unipocket.accountbook.query.persistence.response.AccountBookQueryResponse;
import com.genesis.unipocket.accountbook.query.service.AccountBookQueryService;
import com.genesis.unipocket.expense.command.application.ExpenseCommandService;
import com.genesis.unipocket.global.common.enums.CountryCode;
import com.genesis.unipocket.user.query.service.UserQueryService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccountBookCommandFacadeTest {

	@Mock private AccountBookCommandService accountBookCommandService;
	@Mock private UserQueryService userQueryService;
	@Mock private AccountBookDefaultWidgetPort accountBookDefaultWidgetPort;
	@Mock private ExpenseCommandService expenseCommandService;
	@Mock private AccountBookQueryService accountBookQueryService;

	@InjectMocks private AccountBookCommandFacade accountBookCommandFacade;

	@Test
	@DisplayName("updateAccountBook_localCountryCodeChanged_callsUpdateBaseCurrency")
	void updateAccountBook_localCountryCodeChanged_callsUpdateBaseCurrency() {
		// given
		UUID userId = UUID.randomUUID();
		Long accountBookId = 1L;
		LocalDate start = LocalDate.of(2026, 1, 1);
		LocalDate end = LocalDate.of(2026, 12, 31);

		AccountBookQueryResponse current =
				new AccountBookQueryResponse(
						accountBookId, "title", CountryCode.US, CountryCode.KR, start, end);
		when(accountBookQueryService.getAccountBook(accountBookId)).thenReturn(current);

		AccountBookUpdateRequest req =
				new AccountBookUpdateRequest(
						"title2",
						CountryCode.JP,
						CountryCode.KR,
						BigDecimal.valueOf(100000),
						start,
						end);

		when(accountBookCommandService.update(any()))
				.thenReturn(
						new AccountBookResult(
								accountBookId,
								"title2",
								CountryCode.JP,
								CountryCode.KR,
								start,
								end));

		// when
		var response = accountBookCommandFacade.updateAccountBook(userId, accountBookId, req);

		// then
		verify(expenseCommandService)
				.updateBaseCurrency(accountBookId, CountryCode.KR.getCurrencyCode());
		assertThat(response.baseCurrencyCode()).isEqualTo(CountryCode.KR);
	}

	@Test
	@DisplayName("updateAccountBook_baseCountryCodeChanged_callsUpdateBaseCurrency")
	void updateAccountBook_baseCountryCodeChanged_callsUpdateBaseCurrency() {
		// given
		UUID userId = UUID.randomUUID();
		Long accountBookId = 1L;
		LocalDate start = LocalDate.of(2026, 1, 1);
		LocalDate end = LocalDate.of(2026, 12, 31);

		AccountBookQueryResponse current =
				new AccountBookQueryResponse(
						accountBookId, "title", CountryCode.US, CountryCode.KR, start, end);
		when(accountBookQueryService.getAccountBook(accountBookId)).thenReturn(current);

		AccountBookUpdateRequest req =
				new AccountBookUpdateRequest(
						"title2",
						CountryCode.US,
						CountryCode.JP,
						BigDecimal.valueOf(100000),
						start,
						end);

		when(accountBookCommandService.update(any()))
				.thenReturn(
						new AccountBookResult(
								accountBookId,
								"title2",
								CountryCode.US,
								CountryCode.JP,
								start,
								end));

		// when
		accountBookCommandFacade.updateAccountBook(userId, accountBookId, req);

		// then
		verify(expenseCommandService)
				.updateBaseCurrency(accountBookId, CountryCode.JP.getCurrencyCode());
	}

	@Test
	@DisplayName("updateAccountBook_countryCodesUnchanged_doesNotCallUpdateBaseCurrency")
	void updateAccountBook_countryCodesUnchanged_doesNotCallUpdateBaseCurrency() {
		// given
		UUID userId = UUID.randomUUID();
		Long accountBookId = 1L;
		LocalDate start = LocalDate.of(2026, 1, 1);
		LocalDate end = LocalDate.of(2026, 12, 31);

		AccountBookQueryResponse current =
				new AccountBookQueryResponse(
						accountBookId, "title", CountryCode.US, CountryCode.KR, start, end);
		when(accountBookQueryService.getAccountBook(accountBookId)).thenReturn(current);

		AccountBookUpdateRequest req =
				new AccountBookUpdateRequest(
						"title2",
						CountryCode.US,
						CountryCode.KR,
						BigDecimal.valueOf(100000),
						start,
						end);

		when(accountBookCommandService.update(any()))
				.thenReturn(
						new AccountBookResult(
								accountBookId,
								"title2",
								CountryCode.US,
								CountryCode.KR,
								start,
								end));

		// when
		accountBookCommandFacade.updateAccountBook(userId, accountBookId, req);

		// then
		verify(expenseCommandService, never()).updateBaseCurrency(any(), any());
	}
}
