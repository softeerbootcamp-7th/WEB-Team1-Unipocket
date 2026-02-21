package com.genesis.unipocket.tempexpense.command.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.genesis.unipocket.global.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.tempexpense.command.application.command.TemporaryExpenseUpdateCommand;
import com.genesis.unipocket.tempexpense.command.facade.port.AccountBookRateInfoProvider;
import com.genesis.unipocket.tempexpense.command.facade.port.ExchangeRateProvider;
import com.genesis.unipocket.tempexpense.command.facade.port.dto.AccountBookRateInfo;
import com.genesis.unipocket.tempexpense.command.persistence.entity.TemporaryExpense;
import com.genesis.unipocket.tempexpense.command.persistence.repository.TemporaryExpenseRepository;
import com.genesis.unipocket.tempexpense.common.enums.TemporaryExpenseStatus;
import com.genesis.unipocket.tempexpense.common.validation.TemporaryExpenseValidator;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TemporaryExpenseCommandServiceTest {

	@Mock private TemporaryExpenseRepository temporaryExpenseRepository;
	@Mock private AccountBookRateInfoProvider accountBookRateInfoProvider;
	@Mock private ExchangeRateProvider exchangeRateProvider;

	private TemporaryExpenseCommandService service;

	@BeforeEach
	void setUp() {
		service =
				new TemporaryExpenseCommandService(
						temporaryExpenseRepository,
						accountBookRateInfoProvider,
						exchangeRateProvider,
						new TemporaryExpenseValidator());
	}

	@Test
	@DisplayName("PATCH에서 baseAmount만 들어오면 가계부 baseCurrency를 자동 채운다")
	void updateTemporaryExpense_fillsBaseCurrencyFromAccountBookWhenOnlyBaseAmountProvided() {
		Long tempExpenseId = 1L;
		Long metaId = 10L;
		Long accountBookId = 100L;

		TemporaryExpense existing =
				TemporaryExpense.builder()
						.tempExpenseId(tempExpenseId)
						.tempExpenseMetaId(metaId)
						.fileId(200L)
						.merchantName("기존 가맹점")
						.category(Category.FOOD)
						.localCountryCode(CurrencyCode.USD)
						.localCurrencyAmount(new BigDecimal("10.00"))
						.baseCountryCode(null)
						.baseCurrencyAmount(null)
						.memo("memo")
						.occurredAt(LocalDateTime.of(2026, 2, 17, 10, 0))
						.status(TemporaryExpenseStatus.INCOMPLETE)
						.build();

		TemporaryExpenseUpdateCommand command =
				new TemporaryExpenseUpdateCommand(
						null,
						null,
						null,
						null,
						null,
						new BigDecimal("13000.00"),
						null,
						null,
						null,
						null);

		when(accountBookRateInfoProvider.getRateInfo(accountBookId))
				.thenReturn(new AccountBookRateInfo(CurrencyCode.KRW, CurrencyCode.USD));
		when(temporaryExpenseRepository.save(any(TemporaryExpense.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		service.updateTemporaryExpense(accountBookId, existing, command);

		ArgumentCaptor<TemporaryExpense> captor = ArgumentCaptor.forClass(TemporaryExpense.class);
		verify(temporaryExpenseRepository).save(captor.capture());
		TemporaryExpense saved = captor.getValue();

		assertThat(saved.getBaseCountryCode()).isEqualTo(CurrencyCode.KRW);
		assertThat(saved.getBaseCurrencyAmount()).isEqualByComparingTo("13000.00");
	}
}
