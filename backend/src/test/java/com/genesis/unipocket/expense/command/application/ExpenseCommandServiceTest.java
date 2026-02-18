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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

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
				.thenAnswer(invocation -> invocation.getArgument(0));

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
				.thenAnswer(invocation -> invocation.getArgument(0));

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
		// Mock existing expenses
		ExpenseEntity expense1 = mock(ExpenseEntity.class);
		when(expense1.getLocalCurrency()).thenReturn(CurrencyCode.JPY);
		when(expense1.getLocalAmount()).thenReturn(BigDecimal.valueOf(1000));
		when(expense1.getOccurredAt()).thenReturn(OffsetDateTime.now());
		when(expense1.getOriginalBaseCurrency()).thenReturn(CurrencyCode.KRW);
		when(expense1.getOriginalBaseAmount()).thenReturn(BigDecimal.valueOf(9500));

		ExpenseEntity expense2 = mock(ExpenseEntity.class);
		when(expense2.getLocalCurrency()).thenReturn(CurrencyCode.KRW);
		when(expense2.getLocalAmount()).thenReturn(BigDecimal.valueOf(10000));
		when(expense2.getOccurredAt()).thenReturn(OffsetDateTime.now().minusDays(1));
		when(expense2.getOriginalBaseCurrency()).thenReturn(CurrencyCode.KRW);
		when(expense2.getOriginalBaseAmount()).thenReturn(BigDecimal.valueOf(10000));

		Page<ExpenseEntity> page = new PageImpl<>(List.of(expense1, expense2));
		when(expenseRepository.findByAccountBookId(eq(accountBookId), any(Pageable.class)))
				.thenReturn(page);

		// Mock exchange rate service
		when(exchangeRateService.getExchangeRate(eq(CurrencyCode.JPY), eq(newBaseCurrency), any()))
				.thenReturn(BigDecimal.valueOf(0.0075)); // 1000 JPY -> 7.5 USD

		when(exchangeRateService.getExchangeRate(eq(CurrencyCode.KRW), eq(newBaseCurrency), any()))
				.thenReturn(BigDecimal.valueOf(0.0008)); // 10000 KRW -> 8.0 USD

		// when
		expenseService.updateBaseCurrency(accountBookId, newBaseCurrency);

		// then
		// Verify expense1 update
		verify(expense1)
				.updateExchangeInfo(
						eq(CurrencyCode.JPY),
						eq(BigDecimal.valueOf(1000)),
						eq(CurrencyCode.KRW),
						eq(BigDecimal.valueOf(9500)),
						eq(BigDecimal.valueOf(7.50).setScale(2)),
						eq(newBaseCurrency),
						eq(BigDecimal.valueOf(0.0075)));

		// Verify expense2 update
		verify(expense2)
				.updateExchangeInfo(
						eq(CurrencyCode.KRW),
						eq(BigDecimal.valueOf(10000)),
						eq(CurrencyCode.KRW),
						eq(BigDecimal.valueOf(10000)),
						eq(BigDecimal.valueOf(8.00).setScale(2)),
						eq(newBaseCurrency),
						eq(BigDecimal.valueOf(0.0008)));

		// Verify saveAll called
		verify(expenseRepository).saveAll(anyList());
	}
}
