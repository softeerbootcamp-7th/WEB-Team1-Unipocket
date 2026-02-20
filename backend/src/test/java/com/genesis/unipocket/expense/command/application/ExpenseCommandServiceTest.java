package com.genesis.unipocket.expense.command.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.genesis.unipocket.analysis.command.application.AnalysisMonthlyDirtyMarkerService;
import com.genesis.unipocket.exchange.query.application.ExchangeRateService;
import com.genesis.unipocket.expense.command.application.command.ExpenseCreateCommand;
import com.genesis.unipocket.expense.command.application.command.ExpenseUpdateCommand;
import com.genesis.unipocket.expense.command.persistence.entity.ExpenseEntity;
import com.genesis.unipocket.expense.command.persistence.repository.ExpenseRepository;
import com.genesis.unipocket.global.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionOperations;

/**
 * <b>지출내역 서비스 단위 테스트</b>
 *
 * @author codingbaraGo
 * @since 2026-02-07
 */
@ExtendWith(MockitoExtension.class)
class ExpenseCommandServiceTest {

	@Mock private ExpenseRepository expenseRepository;
	@Mock private ExchangeRateService exchangeRateService;
	@Mock private AnalysisMonthlyDirtyMarkerService analysisMonthlyDirtyMarkerService;
	@Mock private TransactionOperations transactionOperations;

	@InjectMocks private ExpenseCommandService expenseService;

	@Test
	@DisplayName("존재하지 않는 지출내역 삭제 시 EXPENSE_NOT_FOUND 예외 발생")
	void deleteExpense_notFound_throwsException() {
		// given
		Long expenseId = 1L;
		Long accountBookId = 7L;

		when(expenseRepository.findById(expenseId)).thenReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> expenseService.deleteExpense(expenseId, accountBookId))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("code", ErrorCode.EXPENSE_NOT_FOUND);
	}

	@Test
	@DisplayName("다른 가계부의 지출내역 삭제 시 EXPENSE_UNAUTHORIZED_ACCESS 예외 발생")
	void deleteExpense_wrongAccountBook_throwsException() {
		// given
		Long expenseId = 1L;
		Long accountBookId = 7L;
		Long differentAccountBookId = 8L;

		ExpenseEntity expenseEntity = mock(ExpenseEntity.class);
		when(expenseEntity.getAccountBookId()).thenReturn(differentAccountBookId);
		when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(expenseEntity));

		// when & then
		assertThatThrownBy(() -> expenseService.deleteExpense(expenseId, accountBookId))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("code", ErrorCode.EXPENSE_UNAUTHORIZED_ACCESS);
	}

	@Test
	@DisplayName("지출내역 수정 - 통화 변경 시 환율 재계산 호출")
	void updateExpense_currencyChanged_recalculatesRate() {
		// given
		Long expenseId = 1L;
		Long accountBookId = 7L;

		ExpenseEntity expenseEntity = mock(ExpenseEntity.class);
		when(expenseEntity.getAccountBookId()).thenReturn(accountBookId);
		when(expenseEntity.getExpenseId()).thenReturn(expenseId);

		when(expenseEntity.getOccurredAt()).thenReturn(OffsetDateTime.now());
		when(expenseEntity.getUpdatedAt()).thenReturn(LocalDateTime.now());

		ExpenseUpdateCommand command =
				new ExpenseUpdateCommand(
						expenseId,
						accountBookId,
						"스타벅스",
						Category.FOOD,
						null,
						"메모",
						OffsetDateTime.now(),
						BigDecimal.valueOf(1500),
						null,
						CurrencyCode.JPY,
						null,
						CurrencyCode.KRW);

		when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(expenseEntity));
		when(exchangeRateService.getExchangeRate(any(), any(), any())).thenReturn(BigDecimal.ONE);

		// when
		expenseService.updateExpense(command);

		// then
		verify(exchangeRateService, times(1)).getExchangeRate(any(), any(), any()); // 환율 조회 호출 확인
		verify(expenseEntity, times(1))
				.updateExchangeInfo(
						any(), any(), any(), any(), any(), any(), any()); // 환율 정보 업데이트 확인
	}

	@Test
	@DisplayName("지출내역 생성 - base only 입력 시 base는 null 저장되고 calculated로 이관된다")
	void createExpense_baseOnly_movesBaseToCalculated() {
		// given
		OffsetDateTime occurredAt = OffsetDateTime.now();
		when(exchangeRateService.getExchangeRate(any(), any(), any())).thenReturn(BigDecimal.ONE);
		when(expenseRepository.save(any(ExpenseEntity.class)))
				.thenAnswer(invocation -> withAuditFields(invocation.getArgument(0)));

		ExpenseCreateCommand command =
				new ExpenseCreateCommand(
						7L,
						"기내결제",
						Category.FOOD,
						null,
						occurredAt,
						null,
						new BigDecimal("12000.00"),
						CurrencyCode.KRW,
						CurrencyCode.KRW,
						"memo",
						null);

		// when
		var result = expenseService.createExpenseManual(command);

		// then
		assertThat(result.localCurrencyAmount()).isEqualByComparingTo("12000.00");
		assertThat(result.baseCurrencyAmount()).isEqualByComparingTo("12000.00");
		ArgumentCaptor<ExpenseEntity> expenseCaptor = ArgumentCaptor.forClass(ExpenseEntity.class);
		verify(expenseRepository).save(expenseCaptor.capture());
		assertThat(expenseCaptor.getValue().getBaseAmount()).isNull();
		assertThat(expenseCaptor.getValue().getDisplayBaseAmount())
				.isEqualByComparingTo("12000.00");
	}

	@Test
	@DisplayName("지출내역 생성 - local+base 입력 시 base는 보존되고 calculated는 재계산된다")
	void createExpense_localAndBase_preservesBaseAndRecalculatesCalculated() {
		// given
		OffsetDateTime occurredAt = OffsetDateTime.now();
		when(exchangeRateService.getExchangeRate(any(), any(), any()))
				.thenReturn(new BigDecimal("1.1000"));
		when(expenseRepository.save(any(ExpenseEntity.class)))
				.thenAnswer(invocation -> withAuditFields(invocation.getArgument(0)));

		ExpenseCreateCommand command =
				new ExpenseCreateCommand(
						7L,
						"스타벅스",
						Category.FOOD,
						null,
						occurredAt,
						new BigDecimal("10000.00"),
						new BigDecimal("9000.00"),
						CurrencyCode.USD,
						CurrencyCode.KRW,
						"memo",
						null);

		// when
		expenseService.createExpenseManual(command);

		// then
		ArgumentCaptor<ExpenseEntity> expenseCaptor = ArgumentCaptor.forClass(ExpenseEntity.class);
		verify(expenseRepository).save(expenseCaptor.capture());
		ExpenseEntity savedEntity = expenseCaptor.getValue();
		assertThat(savedEntity.getBaseAmount()).isEqualByComparingTo("9000.00");
		assertThat(savedEntity.getExchangeInfo().getCalculatedBaseCurrencyAmount())
				.isEqualByComparingTo("11000.00");
		assertThat(savedEntity.getDisplayBaseAmount()).isEqualByComparingTo("9000.00");
	}

	@Test
	@DisplayName("지출내역 생성 - localAmount가 0이면 EXPENSE_INVALID_AMOUNT 예외")
	void createExpense_zeroLocalAmount_throwsInvalidInput() {
		// given
		ExpenseCreateCommand command =
				new ExpenseCreateCommand(
						7L,
						"스타벅스",
						Category.FOOD,
						null,
						OffsetDateTime.now(),
						BigDecimal.ZERO,
						null,
						CurrencyCode.KRW,
						CurrencyCode.KRW,
						"memo",
						null);

		// when & then
		assertThatThrownBy(() -> expenseService.createExpenseManual(command))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("code", ErrorCode.EXPENSE_INVALID_AMOUNT);
	}

	@Test
	@DisplayName("지출내역 수정 - baseAmount가 0이면 EXPENSE_INVALID_AMOUNT 예외")
	void updateExpense_zeroBaseAmount_throwsInvalidInput() {
		// given
		Long expenseId = 1L;
		Long accountBookId = 7L;
		ExpenseEntity expenseEntity = mock(ExpenseEntity.class);
		when(expenseEntity.getAccountBookId()).thenReturn(accountBookId);
		when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(expenseEntity));

		ExpenseUpdateCommand command =
				new ExpenseUpdateCommand(
						expenseId,
						accountBookId,
						"스타벅스",
						Category.FOOD,
						null,
						"메모",
						OffsetDateTime.now(),
						null,
						BigDecimal.ZERO,
						CurrencyCode.KRW,
						null,
						CurrencyCode.KRW);

		// when & then
		assertThatThrownBy(() -> expenseService.updateExpense(command))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("code", ErrorCode.EXPENSE_INVALID_AMOUNT);
	}

	@Test
	@DisplayName("지출내역 생성 - merchantName 공백이면 EXPENSE_INVALID_MERCHANT_NAME 예외")
	void createExpense_blankMerchant_throwsInvalidInput() {
		ExpenseCreateCommand command =
				new ExpenseCreateCommand(
						7L,
						"  ",
						Category.FOOD,
						null,
						OffsetDateTime.now(),
						new BigDecimal("1000.00"),
						null,
						CurrencyCode.KRW,
						CurrencyCode.KRW,
						"memo",
						null);

		assertThatThrownBy(() -> expenseService.createExpenseManual(command))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("code", ErrorCode.EXPENSE_INVALID_MERCHANT_NAME);
	}

	@Test
	@DisplayName("지출내역 수정 - 동일 통화에서 local/base 불일치면 EXPENSE_SAME_CURRENCY_AMOUNT_MISMATCH 예외")
	void updateExpense_sameCurrencyAmountMismatch_throwsInvalidInput() {
		Long expenseId = 1L;
		Long accountBookId = 7L;
		ExpenseEntity expenseEntity = mock(ExpenseEntity.class);
		when(expenseEntity.getAccountBookId()).thenReturn(accountBookId);
		when(expenseEntity.getOccurredAt()).thenReturn(OffsetDateTime.now());
		when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(expenseEntity));

		ExpenseUpdateCommand command =
				new ExpenseUpdateCommand(
						expenseId,
						accountBookId,
						"스타벅스",
						Category.FOOD,
						null,
						"메모",
						OffsetDateTime.now(),
						new BigDecimal("10000.00"),
						new BigDecimal("9000.00"),
						CurrencyCode.KRW,
						null,
						CurrencyCode.KRW);

		assertThatThrownBy(() -> expenseService.updateExpense(command))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue(
						"code", ErrorCode.EXPENSE_SAME_CURRENCY_AMOUNT_MISMATCH);
	}

	@Test
	@DisplayName("지출내역 수정 - 금액만 변경 시에도 환율 재계산")
	void updateExpense_amountChanged_recalculatesRate() {
		// given
		Long expenseId = 1L;
		Long accountBookId = 7L;

		ExpenseEntity expenseEntity = mock(ExpenseEntity.class);
		when(expenseEntity.getAccountBookId()).thenReturn(accountBookId);
		when(expenseEntity.getExpenseId()).thenReturn(expenseId);

		when(expenseEntity.getOccurredAt()).thenReturn(OffsetDateTime.now());
		when(expenseEntity.getUpdatedAt()).thenReturn(LocalDateTime.now());

		ExpenseUpdateCommand command =
				new ExpenseUpdateCommand(
						expenseId,
						accountBookId,
						"스타벅스",
						Category.FOOD,
						null,
						"메모",
						OffsetDateTime.now(),
						BigDecimal.valueOf(15000),
						null,
						CurrencyCode.KRW,
						null,
						CurrencyCode.KRW);

		when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(expenseEntity));
		when(exchangeRateService.getExchangeRate(any(), any(), any())).thenReturn(BigDecimal.ONE);

		// when
		expenseService.updateExpense(command);

		// then
		verify(exchangeRateService, times(1)).getExchangeRate(any(), any(), any());
	}

	@Test
	@DisplayName("지출내역 수정 - base only 입력 시 base는 null 저장되고 calculated로 이관된다")
	void updateExpense_baseOnly_movesBaseToCalculated() {
		Long expenseId = 1L;
		Long accountBookId = 7L;
		ExpenseEntity expenseEntity = mock(ExpenseEntity.class);
		when(expenseEntity.getAccountBookId()).thenReturn(accountBookId);
		when(expenseEntity.getOccurredAt()).thenReturn(OffsetDateTime.now());
		when(expenseEntity.getUpdatedAt()).thenReturn(LocalDateTime.now());
		when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(expenseEntity));
		when(exchangeRateService.getExchangeRate(any(), any(), any())).thenReturn(BigDecimal.ONE);

		ExpenseUpdateCommand command =
				new ExpenseUpdateCommand(
						expenseId,
						accountBookId,
						"스타벅스",
						Category.FOOD,
						null,
						"메모",
						OffsetDateTime.now(),
						null,
						new BigDecimal("12000.00"),
						CurrencyCode.KRW,
						null,
						CurrencyCode.KRW);

		expenseService.updateExpense(command);

		ArgumentCaptor<BigDecimal> localCaptor = ArgumentCaptor.forClass(BigDecimal.class);
		ArgumentCaptor<BigDecimal> baseCaptor = ArgumentCaptor.forClass(BigDecimal.class);
		ArgumentCaptor<BigDecimal> calculatedCaptor = ArgumentCaptor.forClass(BigDecimal.class);
		verify(expenseEntity)
				.updateExchangeInfo(
						eq(CurrencyCode.KRW),
						localCaptor.capture(),
						eq(CurrencyCode.KRW),
						baseCaptor.capture(),
						calculatedCaptor.capture(),
						eq(CurrencyCode.KRW),
						eq(BigDecimal.ONE));

		assertThat(localCaptor.getValue()).isEqualByComparingTo("12000.00");
		assertThat(baseCaptor.getValue()).isNull();
		assertThat(calculatedCaptor.getValue()).isEqualByComparingTo("12000.00");
	}

	@Test
	@DisplayName("지출내역 수정 - local+base 입력 시 base는 보존되고 calculated는 재계산된다")
	void updateExpense_localAndBase_preservesBaseAndRecalculatesCalculated() {
		Long expenseId = 1L;
		Long accountBookId = 7L;
		ExpenseEntity expenseEntity = mock(ExpenseEntity.class);
		when(expenseEntity.getAccountBookId()).thenReturn(accountBookId);
		when(expenseEntity.getOccurredAt()).thenReturn(OffsetDateTime.now());
		when(expenseEntity.getUpdatedAt()).thenReturn(LocalDateTime.now());
		when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(expenseEntity));
		when(exchangeRateService.getExchangeRate(any(), any(), any()))
				.thenReturn(new BigDecimal("1.1000"));

		ExpenseUpdateCommand command =
				new ExpenseUpdateCommand(
						expenseId,
						accountBookId,
						"스타벅스",
						Category.FOOD,
						null,
						"메모",
						OffsetDateTime.now(),
						new BigDecimal("10000.00"),
						new BigDecimal("9000.00"),
						CurrencyCode.USD,
						null,
						CurrencyCode.KRW);

		expenseService.updateExpense(command);

		ArgumentCaptor<BigDecimal> baseCaptor = ArgumentCaptor.forClass(BigDecimal.class);
		ArgumentCaptor<BigDecimal> calculatedCaptor = ArgumentCaptor.forClass(BigDecimal.class);
		verify(expenseEntity)
				.updateExchangeInfo(
						eq(CurrencyCode.USD),
						eq(new BigDecimal("10000.00")),
						eq(CurrencyCode.KRW),
						baseCaptor.capture(),
						calculatedCaptor.capture(),
						eq(CurrencyCode.KRW),
						eq(new BigDecimal("1.1000")));

		assertThat(baseCaptor.getValue()).isEqualByComparingTo("9000.00");
		assertThat(calculatedCaptor.getValue()).isEqualByComparingTo("11000.00");
	}

	@Test
	@DisplayName("지출내역 삭제 - 성공")
	void deleteExpense_success() {
		// given
		Long expenseId = 1L;
		Long accountBookId = 7L;

		ExpenseEntity expenseEntity = mock(ExpenseEntity.class);
		when(expenseEntity.getAccountBookId()).thenReturn(accountBookId);
		when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(expenseEntity));

		// when
		expenseService.deleteExpense(expenseId, accountBookId);

		// then
		verify(expenseRepository, times(1)).delete(expenseEntity);
	}

	@Test
	@DisplayName("가계부 기준 통화 변경 시 모든 지출 내역의 기준 금액 재계산")
	void updateBaseCurrency_recalculatesAllExpenses() {
		// given
		Long accountBookId = 7L;
		CurrencyCode newBaseCurrency = CurrencyCode.USD;
		LocalDate day1 = LocalDate.of(2026, 2, 1);
		LocalDate day2 = LocalDate.of(2026, 2, 2);
		when(expenseRepository.findDistinctLocalCurrencyDatePairsByAccountBookId(accountBookId))
				.thenReturn(
						List.of(
								new Object[] {CurrencyCode.JPY, day1},
								new Object[] {CurrencyCode.KRW, day2}));

		// Mock exchange rate service
		when(exchangeRateService.getExchangeRate(eq(CurrencyCode.JPY), eq(newBaseCurrency), any()))
				.thenReturn(BigDecimal.valueOf(0.0075));

		when(exchangeRateService.getExchangeRate(eq(CurrencyCode.KRW), eq(newBaseCurrency), any()))
				.thenReturn(BigDecimal.valueOf(0.0008));
		when(transactionOperations.execute(any()))
				.thenAnswer(
						invocation -> {
							@SuppressWarnings("unchecked")
							TransactionCallback<Boolean> callback =
									(TransactionCallback<Boolean>) invocation.getArgument(0);
							return callback.doInTransaction(new SimpleTransactionStatus());
						});

		// when
		expenseService.updateBaseCurrency(accountBookId, newBaseCurrency);

		// then
		verify(expenseRepository)
				.bulkUpdateBaseCurrencyByLocalCurrencyAndOccurredAtRange(
						eq(accountBookId),
						eq(CurrencyCode.JPY),
						eq(newBaseCurrency),
						eq(BigDecimal.valueOf(0.0075)),
						eq(day1.atStartOfDay().atOffset(java.time.ZoneOffset.UTC)),
						eq(day1.plusDays(1).atStartOfDay().atOffset(java.time.ZoneOffset.UTC)));
		verify(expenseRepository)
				.bulkUpdateBaseCurrencyByLocalCurrencyAndOccurredAtRange(
						eq(accountBookId),
						eq(CurrencyCode.KRW),
						eq(newBaseCurrency),
						eq(BigDecimal.valueOf(0.0008)),
						eq(day2.atStartOfDay().atOffset(java.time.ZoneOffset.UTC)),
						eq(day2.plusDays(1).atStartOfDay().atOffset(java.time.ZoneOffset.UTC)));
	}

	private ExpenseEntity withAuditFields(ExpenseEntity entity) {
		ReflectionTestUtils.setField(entity, "updatedAt", LocalDateTime.now());
		return entity;
	}
}
