package com.genesis.unipocket.tempexpense.command.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.genesis.unipocket.exchange.common.service.ExchangeRateService;
import com.genesis.unipocket.expense.command.persistence.entity.ExpenseEntity;
import com.genesis.unipocket.expense.command.persistence.repository.ExpenseRepository;
import com.genesis.unipocket.global.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.tempexpense.command.application.result.ConfirmStartResult;
import com.genesis.unipocket.tempexpense.command.facade.port.AccountBookRateInfoProvider;
import com.genesis.unipocket.tempexpense.command.facade.port.dto.AccountBookRateInfo;
import com.genesis.unipocket.tempexpense.command.facade.provide.TemporaryExpenseScopeValidationProvider;
import com.genesis.unipocket.tempexpense.command.persistence.entity.File;
import com.genesis.unipocket.tempexpense.command.persistence.entity.TempExpenseMeta;
import com.genesis.unipocket.tempexpense.command.persistence.entity.TemporaryExpense;
import com.genesis.unipocket.tempexpense.command.persistence.repository.FileRepository;
import com.genesis.unipocket.tempexpense.command.persistence.repository.TemporaryExpenseRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TemporaryExpenseConversionServiceTest {

	@Mock private TemporaryExpenseRepository temporaryExpenseRepository;
	@Mock private FileRepository fileRepository;
	@Mock private ExpenseRepository expenseRepository;
	@Mock private ExchangeRateService exchangeRateService;
	@Mock private AccountBookRateInfoProvider accountBookRateInfoProvider;
	@Mock private TemporaryExpenseScopeValidationProvider temporaryExpenseScopeValidationProvider;

	private TemporaryExpenseConversionService service;

	@BeforeEach
	void setUp() {
		service =
				new TemporaryExpenseConversionService(
						temporaryExpenseRepository,
						fileRepository,
						expenseRepository,
						exchangeRateService,
						accountBookRateInfoProvider,
						temporaryExpenseScopeValidationProvider);
	}

	@Test
	@DisplayName("confirm은 메타 범위 임시지출을 동기 변환하고 삭제한다")
	void startConfirmAsync_convertsSynchronously() {
		Long accountBookId = 1L;
		Long metaId = 10L;
		Long fileId = 30L;

		TemporaryExpense tempExpense =
				TemporaryExpense.builder()
						.tempExpenseId(100L)
						.tempExpenseMetaId(metaId)
						.fileId(fileId)
						.merchantName("상호")
						.category(Category.FOOD)
						.localCountryCode(CurrencyCode.USD)
						.localCurrencyAmount(new BigDecimal("10.00"))
						.baseCountryCode(CurrencyCode.KRW)
						.baseCurrencyAmount(new BigDecimal("13000.00"))
						.occurredAt(LocalDateTime.of(2026, 2, 17, 10, 0))
						.build();

		when(temporaryExpenseScopeValidationProvider.validateMetaScope(accountBookId, metaId))
				.thenReturn(
						TempExpenseMeta.builder()
								.tempExpenseMetaId(metaId)
								.accountBookId(accountBookId)
								.build());
		when(temporaryExpenseRepository.findByTempExpenseMetaId(metaId))
				.thenReturn(List.of(tempExpense));
		when(accountBookRateInfoProvider.getRateInfo(accountBookId))
				.thenReturn(new AccountBookRateInfo(CurrencyCode.KRW, CurrencyCode.USD));
		when(fileRepository.findById(fileId))
				.thenReturn(
						Optional.of(
								File.builder()
										.fileId(fileId)
										.tempExpenseMetaId(metaId)
										.fileType(File.FileType.IMAGE)
										.s3Key("temp/a.png")
										.build()));
		when(exchangeRateService.getExchangeRate(any(), any(), any()))
				.thenReturn(new BigDecimal("1300.00"));
		when(expenseRepository.save(any(ExpenseEntity.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		ConfirmStartResult result = service.startConfirmAsync(accountBookId, metaId);

		assertThat(result.totalExpenses()).isEqualTo(1);
		assertThat(result.taskId()).isNull();
		verify(expenseRepository).save(any(ExpenseEntity.class));
		verify(temporaryExpenseRepository).delete(tempExpense);
	}

	@Test
	@DisplayName("confirm은 대상 임시지출이 없으면 예외를 던진다")
	void startConfirmAsync_throwsWhenNoTarget() {
		Long accountBookId = 1L;
		Long metaId = 10L;

		when(temporaryExpenseScopeValidationProvider.validateMetaScope(accountBookId, metaId))
				.thenReturn(
						TempExpenseMeta.builder()
								.tempExpenseMetaId(metaId)
								.accountBookId(accountBookId)
								.build());
		when(temporaryExpenseRepository.findByTempExpenseMetaId(metaId)).thenReturn(List.of());

		assertThatThrownBy(() -> service.startConfirmAsync(accountBookId, metaId))
				.isInstanceOf(BusinessException.class)
				.hasMessageContaining("임시 지출 내역을 찾을 수 없습니다.");
	}

	@Test
	@DisplayName("local/base 통화가 같으면 환율 API를 호출하지 않는다")
	void startConfirmAsync_skipsExchangeLookupWhenSameCurrency() {
		Long accountBookId = 1L;
		Long metaId = 10L;

		TemporaryExpense tempExpense =
				TemporaryExpense.builder()
						.tempExpenseId(100L)
						.tempExpenseMetaId(metaId)
						.fileId(30L)
						.merchantName("상호")
						.category(Category.FOOD)
						.localCountryCode(CurrencyCode.KRW)
						.localCurrencyAmount(new BigDecimal("10000.00"))
						.baseCountryCode(CurrencyCode.KRW)
						.baseCurrencyAmount(new BigDecimal("10000.00"))
						.occurredAt(LocalDateTime.of(2026, 2, 17, 10, 0))
						.build();

		when(temporaryExpenseScopeValidationProvider.validateMetaScope(accountBookId, metaId))
				.thenReturn(
						TempExpenseMeta.builder()
								.tempExpenseMetaId(metaId)
								.accountBookId(accountBookId)
								.build());
		when(temporaryExpenseRepository.findByTempExpenseMetaId(metaId))
				.thenReturn(List.of(tempExpense));
		when(accountBookRateInfoProvider.getRateInfo(accountBookId))
				.thenReturn(new AccountBookRateInfo(CurrencyCode.KRW, CurrencyCode.KRW));
		when(fileRepository.findById(30L))
				.thenReturn(
						Optional.of(
								File.builder()
										.fileId(30L)
										.tempExpenseMetaId(metaId)
										.fileType(File.FileType.CSV)
										.s3Key("temp/a.csv")
										.build()));
		when(expenseRepository.save(any(ExpenseEntity.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		service.startConfirmAsync(accountBookId, metaId);

		verify(exchangeRateService, never()).getExchangeRate(any(), any(), any());
	}
}
