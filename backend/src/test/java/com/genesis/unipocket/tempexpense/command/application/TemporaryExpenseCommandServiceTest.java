package com.genesis.unipocket.tempexpense.command.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.genesis.unipocket.global.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CountryCode;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.tempexpense.command.application.command.TemporaryExpenseUpdateCommand;
import com.genesis.unipocket.tempexpense.command.facade.port.AccountBookRateInfoProvider;
import com.genesis.unipocket.tempexpense.command.facade.port.dto.AccountBookRateInfo;
import com.genesis.unipocket.tempexpense.command.persistence.entity.TempExpenseMeta;
import com.genesis.unipocket.tempexpense.command.persistence.entity.TemporaryExpense;
import com.genesis.unipocket.tempexpense.command.persistence.entity.tempexpense.TempExpenseStatusPolicy;
import com.genesis.unipocket.tempexpense.command.persistence.repository.TempExpenseMetaRepository;
import com.genesis.unipocket.tempexpense.command.persistence.repository.TemporaryExpenseRepository;
import com.genesis.unipocket.tempexpense.common.enums.TemporaryExpenseStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
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
	@Mock private TempExpenseMetaRepository tempExpenseMetaRepository;
	@Mock private AccountBookRateInfoProvider accountBookRateInfoProvider;

	private TempExpenseService service;

	@BeforeEach
	void setUp() {
		service =
				new TempExpenseService(
						temporaryExpenseRepository,
						tempExpenseMetaRepository,
						accountBookRateInfoProvider,
						new TempExpenseStatusPolicy());
	}

	@Test
	@DisplayName("PATCH에서 cardLastFourDigits를 보내면 카드가 변경된다")
	void updateTemporaryExpense_updatesCardLastFourDigitsWhenProvided() {
		Long tempExpenseId = 1L;
		Long metaId = 10L;

		TemporaryExpense existing =
				TemporaryExpense.builder()
						.tempExpenseId(tempExpenseId)
						.tempExpenseMetaId(metaId)
						.fileId(200L)
						.merchantName("가맹점")
						.category(Category.FOOD)
						.localCountryCode(CurrencyCode.USD)
						.localCurrencyAmount(new BigDecimal("10.00"))
						.baseCountryCode(CurrencyCode.KRW)
						.baseCurrencyAmount(new BigDecimal("13000.00"))
						.memo("memo")
						.occurredAt(LocalDateTime.of(2026, 2, 17, 10, 0))
						.status(TemporaryExpenseStatus.INCOMPLETE)
						.cardLastFourDigits("1234")
						.build();

		TemporaryExpenseUpdateCommand command =
				new TemporaryExpenseUpdateCommand(
						null, null, null, null, null, null, null, null, Optional.of("5678"));

		when(temporaryExpenseRepository.findById(tempExpenseId)).thenReturn(Optional.of(existing));
		when(temporaryExpenseRepository.save(any(TemporaryExpense.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		service.updateTemporaryExpense(tempExpenseId, command);

		ArgumentCaptor<TemporaryExpense> captor = ArgumentCaptor.forClass(TemporaryExpense.class);
		verify(temporaryExpenseRepository).save(captor.capture());
		TemporaryExpense saved = captor.getValue();

		assertThat(saved.getCardLastFourDigits()).isEqualTo("5678");
	}

	@Test
	@DisplayName("PATCH에서 cardLastFourDigits를 null로 보내면 카드가 초기화된다")
	void updateTemporaryExpense_clearsCardWhenNullSent() {
		Long tempExpenseId = 1L;
		Long metaId = 10L;

		TemporaryExpense existing =
				TemporaryExpense.builder()
						.tempExpenseId(tempExpenseId)
						.tempExpenseMetaId(metaId)
						.fileId(200L)
						.merchantName("가맹점")
						.category(Category.FOOD)
						.localCountryCode(CurrencyCode.USD)
						.localCurrencyAmount(new BigDecimal("10.00"))
						.baseCountryCode(CurrencyCode.KRW)
						.baseCurrencyAmount(new BigDecimal("13000.00"))
						.memo("memo")
						.occurredAt(LocalDateTime.of(2026, 2, 17, 10, 0))
						.status(TemporaryExpenseStatus.INCOMPLETE)
						.cardLastFourDigits("1234")
						.build();

		// Optional.empty() = "cardLastFourDigits": null (명시적 null → 초기화)
		TemporaryExpenseUpdateCommand command =
				new TemporaryExpenseUpdateCommand(
						null, null, null, null, null, null, null, null, Optional.empty());

		when(temporaryExpenseRepository.findById(tempExpenseId)).thenReturn(Optional.of(existing));
		when(temporaryExpenseRepository.save(any(TemporaryExpense.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		service.updateTemporaryExpense(tempExpenseId, command);

		ArgumentCaptor<TemporaryExpense> captor = ArgumentCaptor.forClass(TemporaryExpense.class);
		verify(temporaryExpenseRepository).save(captor.capture());
		TemporaryExpense saved = captor.getValue();

		assertThat(saved.getCardLastFourDigits()).isNull();
	}

	@Test
	@DisplayName("PATCH에서 cardLastFourDigits 필드를 보내지 않으면 기존 카드가 유지된다")
	void updateTemporaryExpense_keepsExistingCardWhenFieldAbsent() {
		Long tempExpenseId = 1L;
		Long metaId = 10L;

		TemporaryExpense existing =
				TemporaryExpense.builder()
						.tempExpenseId(tempExpenseId)
						.tempExpenseMetaId(metaId)
						.fileId(200L)
						.merchantName("가맹점")
						.category(Category.FOOD)
						.localCountryCode(CurrencyCode.USD)
						.localCurrencyAmount(new BigDecimal("10.00"))
						.baseCountryCode(CurrencyCode.KRW)
						.baseCurrencyAmount(new BigDecimal("13000.00"))
						.memo("memo")
						.occurredAt(LocalDateTime.of(2026, 2, 17, 10, 0))
						.status(TemporaryExpenseStatus.INCOMPLETE)
						.cardLastFourDigits("1234")
						.build();

		// null = 필드 미전송 → 기존값 유지
		TemporaryExpenseUpdateCommand command =
				new TemporaryExpenseUpdateCommand(
						null, null, null, null, null, null, null, null, null);

		when(temporaryExpenseRepository.findById(tempExpenseId)).thenReturn(Optional.of(existing));
		when(temporaryExpenseRepository.save(any(TemporaryExpense.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		service.updateTemporaryExpense(tempExpenseId, command);

		ArgumentCaptor<TemporaryExpense> captor = ArgumentCaptor.forClass(TemporaryExpense.class);
		verify(temporaryExpenseRepository).save(captor.capture());
		TemporaryExpense saved = captor.getValue();

		assertThat(saved.getCardLastFourDigits()).isEqualTo("1234");
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
						null, null, null, null, null, new BigDecimal("13000.00"), null, null, null);

		when(temporaryExpenseRepository.findById(tempExpenseId)).thenReturn(Optional.of(existing));
		when(tempExpenseMetaRepository.findById(metaId))
				.thenReturn(
						Optional.of(
								TempExpenseMeta.builder()
										.tempExpenseMetaId(metaId)
										.accountBookId(accountBookId)
										.build()));
		when(accountBookRateInfoProvider.getRateInfo(accountBookId))
				.thenReturn(
						new AccountBookRateInfo(
								CurrencyCode.KRW, CurrencyCode.USD, CountryCode.US));
		when(temporaryExpenseRepository.save(any(TemporaryExpense.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		service.updateTemporaryExpense(tempExpenseId, command);

		ArgumentCaptor<TemporaryExpense> captor = ArgumentCaptor.forClass(TemporaryExpense.class);
		verify(temporaryExpenseRepository).save(captor.capture());
		TemporaryExpense saved = captor.getValue();

		assertThat(saved.getBaseCountryCode()).isEqualTo(CurrencyCode.KRW);
		assertThat(saved.getBaseCurrencyAmount()).isEqualByComparingTo("13000.00");
	}
}
