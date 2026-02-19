package com.genesis.unipocket.accountbook.command.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.genesis.unipocket.accountbook.command.application.AccountBookCommandService;
import com.genesis.unipocket.accountbook.command.application.result.AccountBookResult;
import com.genesis.unipocket.accountbook.command.facade.port.AccountBookDefaultWidgetPort;
import com.genesis.unipocket.accountbook.command.facade.port.ExpenseCurrencySyncService;
import com.genesis.unipocket.accountbook.command.facade.port.UserInfoReader;
import com.genesis.unipocket.accountbook.command.presentation.request.AccountBookUpdateRequest;
import com.genesis.unipocket.global.common.enums.CountryCode;
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
	@Mock private UserInfoReader userInfoReader;
	@Mock private AccountBookDefaultWidgetPort accountBookDefaultWidgetPort;
	@Mock private ExpenseCurrencySyncService expenseCurrencySyncService;

	@InjectMocks private AccountBookCommandFacade accountBookCommandFacade;

	@Test
	@DisplayName("updateAccountBook_localCountryCodeChanged_callsUpdateBaseCurrency")
	void updateAccountBook_localCountryCodeChanged_callsUpdateBaseCurrency() {
		// given
		UUID userId = UUID.randomUUID();
		Long accountBookId = 1L;
		LocalDate start = LocalDate.of(2026, 1, 1);
		LocalDate end = LocalDate.of(2026, 12, 31);

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
								end,
								true));

		// when
		var response = accountBookCommandFacade.updateAccountBook(userId, accountBookId, req);

		// then
		verify(expenseCurrencySyncService)
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
								end,
								true));

		// when
		accountBookCommandFacade.updateAccountBook(userId, accountBookId, req);

		// then
		verify(expenseCurrencySyncService)
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
								end,
								false));

		// when
		accountBookCommandFacade.updateAccountBook(userId, accountBookId, req);

		// then
		verify(expenseCurrencySyncService, never()).updateBaseCurrency(any(), any());
	}
}
