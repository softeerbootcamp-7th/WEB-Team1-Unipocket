package com.genesis.unipocket.expense.command.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.genesis.unipocket.exchange.query.application.ExchangeRateService;
import com.genesis.unipocket.expense.command.application.command.ExpenseUpdateCommand;
import com.genesis.unipocket.expense.command.persistence.entity.ExchangeInfo;
import com.genesis.unipocket.expense.command.persistence.entity.ExpenseEntity;
import com.genesis.unipocket.expense.command.persistence.entity.ExpenseSourceInfo;
import com.genesis.unipocket.expense.command.persistence.entity.Merchant;
import com.genesis.unipocket.expense.command.persistence.repository.ExpenseRepository;
import com.genesis.unipocket.global.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.global.common.enums.ExpenseSource;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
		when(expenseEntity.getLocalCurrency()).thenReturn(CurrencyCode.KRW);
		when(expenseEntity.getLocalAmount()).thenReturn(BigDecimal.valueOf(10000));
		when(expenseEntity.getExpenseId()).thenReturn(expenseId);

		// Mock ExchangeInfo for ExpenseDto.from()
		ExchangeInfo exchangeInfo = mock(ExchangeInfo.class);
		when(exchangeInfo.getLocalCurrencyCode()).thenReturn(CurrencyCode.JPY);
		when(exchangeInfo.getLocalCurrencyAmount()).thenReturn(BigDecimal.valueOf(1500));
		when(expenseEntity.getExchangeInfo()).thenReturn(exchangeInfo);

		// Mock Merchant for ExpenseDto.from()
		Merchant merchant = mock(Merchant.class);
		when(merchant.getMerchantName()).thenReturn("스타벅스");
		when(merchant.getDisplayMerchantName()).thenReturn("스타벅스");
		when(expenseEntity.getMerchant()).thenReturn(merchant);

		// Mock ExpenseSourceInfo for ExpenseDto.from()
		ExpenseSourceInfo sourceInfo = mock(ExpenseSourceInfo.class);
		when(sourceInfo.getExpenseSource()).thenReturn(ExpenseSource.MANUAL);
		when(sourceInfo.getFileLink()).thenReturn(null);
		when(expenseEntity.getExpenseSourceInfo()).thenReturn(sourceInfo);

		// Mock other required methods
		when(expenseEntity.getOccurredAt()).thenReturn(LocalDateTime.now());
		when(expenseEntity.getCategory()).thenReturn(Category.FOOD);
		when(expenseEntity.getTravelId()).thenReturn(null);
		when(expenseEntity.getApprovalNumber()).thenReturn(null);
		when(expenseEntity.getUserCardId()).thenReturn(null);
		when(expenseEntity.getMemo()).thenReturn("메모");
		when(expenseEntity.getCardNumber()).thenReturn(null);

		ExpenseUpdateCommand command =
				new ExpenseUpdateCommand(
						expenseId,
						accountBookId,
						"스타벅스",
						Category.FOOD,
						null,
						"메모",
						LocalDateTime.now(),
						BigDecimal.valueOf(1500),
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
	@DisplayName("지출내역 수정 - 금액만 변경 시에도 환율 재계산")
	void updateExpense_amountChanged_recalculatesRate() {
		// given
		Long expenseId = 1L;
		Long accountBookId = 7L;

		ExpenseEntity expenseEntity = mock(ExpenseEntity.class);
		when(expenseEntity.getAccountBookId()).thenReturn(accountBookId);
		when(expenseEntity.getLocalCurrency()).thenReturn(CurrencyCode.KRW);
		when(expenseEntity.getLocalAmount()).thenReturn(BigDecimal.valueOf(10000));
		when(expenseEntity.getExpenseId()).thenReturn(expenseId);

		// Mock ExchangeInfo for ExpenseDto.from()
		ExchangeInfo exchangeInfo = mock(ExchangeInfo.class);
		when(exchangeInfo.getLocalCurrencyCode()).thenReturn(CurrencyCode.KRW);
		when(exchangeInfo.getLocalCurrencyAmount()).thenReturn(BigDecimal.valueOf(15000));
		when(expenseEntity.getExchangeInfo()).thenReturn(exchangeInfo);

		// Mock Merchant for ExpenseDto.from()
		Merchant merchant = mock(Merchant.class);
		when(merchant.getMerchantName()).thenReturn("스타벅스");
		when(merchant.getDisplayMerchantName()).thenReturn("스타벅스");
		when(expenseEntity.getMerchant()).thenReturn(merchant);

		// Mock ExpenseSourceInfo for ExpenseDto.from()
		ExpenseSourceInfo sourceInfo = mock(ExpenseSourceInfo.class);
		when(sourceInfo.getExpenseSource()).thenReturn(ExpenseSource.MANUAL);
		when(sourceInfo.getFileLink()).thenReturn(null);
		when(expenseEntity.getExpenseSourceInfo()).thenReturn(sourceInfo);

		// Mock other required methods
		when(expenseEntity.getOccurredAt()).thenReturn(LocalDateTime.now());
		when(expenseEntity.getCategory()).thenReturn(Category.FOOD);
		when(expenseEntity.getTravelId()).thenReturn(null);
		when(expenseEntity.getApprovalNumber()).thenReturn(null);
		when(expenseEntity.getUserCardId()).thenReturn(null);
		when(expenseEntity.getMemo()).thenReturn("메모");
		when(expenseEntity.getCardNumber()).thenReturn(null);

		ExpenseUpdateCommand command =
				new ExpenseUpdateCommand(
						expenseId,
						accountBookId,
						"스타벅스",
						Category.FOOD,
						null,
						"메모",
						LocalDateTime.now(),
						BigDecimal.valueOf(15000),
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
		when(expense1.getOccurredAt()).thenReturn(LocalDateTime.now());
		when(expense1.getOriginalBaseCurrency()).thenReturn(CurrencyCode.KRW);
		when(expense1.getOriginalBaseAmount()).thenReturn(BigDecimal.valueOf(9500));

		ExpenseEntity expense2 = mock(ExpenseEntity.class);
		when(expense2.getLocalCurrency()).thenReturn(CurrencyCode.KRW);
		when(expense2.getLocalAmount()).thenReturn(BigDecimal.valueOf(10000));
		when(expense2.getOccurredAt()).thenReturn(LocalDateTime.now().minusDays(1));
		when(expense2.getOriginalBaseCurrency()).thenReturn(CurrencyCode.KRW);
		when(expense2.getOriginalBaseAmount()).thenReturn(BigDecimal.valueOf(10000));

		Page<ExpenseEntity> page = new PageImpl<>(java.util.List.of(expense1, expense2));
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
