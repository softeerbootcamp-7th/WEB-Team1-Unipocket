package com.genesis.unipocket.tempexpense.command.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.genesis.unipocket.expense.command.persistence.entity.ExpenseEntity;
import com.genesis.unipocket.global.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.tempexpense.command.application.result.BatchConversionResult;
import com.genesis.unipocket.tempexpense.command.application.result.ConfirmStartResult;
import com.genesis.unipocket.tempexpense.command.facade.port.AccountBookRateInfoProvider;
import com.genesis.unipocket.tempexpense.command.facade.port.dto.AccountBookRateInfo;
import com.genesis.unipocket.tempexpense.command.persistence.entity.TempExpenseMeta;
import com.genesis.unipocket.tempexpense.command.persistence.entity.TemporaryExpense;
import com.genesis.unipocket.tempexpense.command.persistence.repository.TempExpenseMetaRepository;
import com.genesis.unipocket.tempexpense.command.persistence.repository.TemporaryExpenseRepository;
import com.genesis.unipocket.tempexpense.common.infrastructure.ParsingProgressPublisher;
import com.genesis.unipocket.tempexpense.common.validation.TemporaryExpenseValidator;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TemporaryExpenseConversionServiceTest {

	@Mock private TemporaryExpenseRepository tempExpenseRepository;
	@Mock private TempExpenseMetaRepository metaRepository;
	@Mock private AccountBookRateInfoProvider accountBookRateInfoProvider;
	@Mock private ParsingProgressPublisher progressPublisher;
	@Mock private TemporaryExpenseSingleConversionTxService singleConversionTxService;

	private TemporaryExpenseConversionService service;

	private final Executor directExecutor = Runnable::run;

	@BeforeEach
	void setUp() {
		service =
				new TemporaryExpenseConversionService(
						tempExpenseRepository,
						metaRepository,
						accountBookRateInfoProvider,
						progressPublisher,
						singleConversionTxService,
						new TemporaryExpenseValidator(),
						directExecutor);
	}

	@Test
	@DisplayName("startConfirmAsync는 메타 전체를 대상으로 task를 등록하고 비동기 변환을 시작한다")
	void startConfirmAsync_registersTaskAndStartsBatch() {
		Long accountBookId = 1L;
		Long metaId = 10L;
		TemporaryExpense tempExpense =
				TemporaryExpense.builder()
						.tempExpenseId(100L)
						.tempExpenseMetaId(metaId)
						.merchantName("상호")
						.category(Category.FOOD)
						.localCountryCode(CurrencyCode.USD)
						.localCurrencyAmount(new BigDecimal("10.00"))
						.baseCountryCode(CurrencyCode.KRW)
						.occurredAt(LocalDateTime.of(2026, 2, 17, 10, 0))
						.build();

		when(metaRepository.findById(metaId))
				.thenReturn(
						Optional.of(
								TempExpenseMeta.builder()
										.tempExpenseMetaId(metaId)
										.accountBookId(accountBookId)
										.build()));
		when(tempExpenseRepository.findByTempExpenseMetaId(metaId))
				.thenReturn(List.of(tempExpense));
		when(tempExpenseRepository.findAllById(List.of(100L))).thenReturn(List.of(tempExpense));
		when(accountBookRateInfoProvider.getRateInfo(accountBookId))
				.thenReturn(new AccountBookRateInfo(CurrencyCode.KRW, CurrencyCode.USD));
		when(singleConversionTxService.convertToExpense(accountBookId, 100L))
				.thenReturn(ExpenseEntity.builder().expenseId(999L).build());

		ConfirmStartResult result = service.startConfirmAsync(accountBookId, metaId);

		assertThat(result.totalExpenses()).isEqualTo(1);
		verify(progressPublisher).registerTask(anyString(), anyLong());
		verify(singleConversionTxService).convertToExpense(accountBookId, 100L);
		verify(progressPublisher).complete(anyString(), any(BatchConversionResult.class));
	}

	@Test
	@DisplayName("convertBatchAsync는 건별 변환 실패 시 file-error를 발행하고 다음 건을 계속 처리한다")
	void convertBatchAsync_publishFileErrorAndContinue() {
		Long accountBookId = 1L;
		List<Long> ids = List.of(1L, 2L);

		when(singleConversionTxService.convertToExpense(accountBookId, 1L))
				.thenReturn(ExpenseEntity.builder().expenseId(101L).build());
		when(singleConversionTxService.convertToExpense(accountBookId, 2L))
				.thenThrow(new IllegalStateException("fail"));

		BatchConversionResult result =
				service.convertBatchAsync(accountBookId, ids, "task-1").join();

		assertThat(result.totalRequested()).isEqualTo(2);
		assertThat(result.successCount()).isEqualTo(1);
		assertThat(result.failedCount()).isEqualTo(1);
		verify(progressPublisher).publishFileError(anyString(), any());
		verify(progressPublisher).complete(anyString(), any(BatchConversionResult.class));
		verify(progressPublisher, never()).publishError(anyString(), anyString());
	}

	@Test
	@DisplayName("startConfirmAsync는 baseCountryCode가 비어있어도 가계부 기준 통화로 검증 통과한다")
	void startConfirmAsync_validatesWithAccountBookBaseCurrencyFallback() {
		Long accountBookId = 1L;
		Long metaId = 10L;
		TemporaryExpense tempExpense =
				TemporaryExpense.builder()
						.tempExpenseId(100L)
						.tempExpenseMetaId(metaId)
						.merchantName("상호")
						.category(Category.FOOD)
						.localCountryCode(CurrencyCode.USD)
						.localCurrencyAmount(new BigDecimal("10.00"))
						.baseCountryCode(null)
						.occurredAt(LocalDateTime.of(2026, 2, 17, 10, 0))
						.build();

		when(metaRepository.findById(metaId))
				.thenReturn(
						Optional.of(
								TempExpenseMeta.builder()
										.tempExpenseMetaId(metaId)
										.accountBookId(accountBookId)
										.build()));
		when(tempExpenseRepository.findByTempExpenseMetaId(metaId))
				.thenReturn(List.of(tempExpense));
		when(tempExpenseRepository.findAllById(List.of(100L))).thenReturn(List.of(tempExpense));
		when(accountBookRateInfoProvider.getRateInfo(accountBookId))
				.thenReturn(new AccountBookRateInfo(CurrencyCode.KRW, CurrencyCode.USD));
		when(singleConversionTxService.convertToExpense(accountBookId, 100L))
				.thenReturn(ExpenseEntity.builder().expenseId(999L).build());

		ConfirmStartResult result = service.startConfirmAsync(accountBookId, metaId);

		assertThat(result.totalExpenses()).isEqualTo(1);
		verify(accountBookRateInfoProvider).getRateInfo(accountBookId);
	}
}
