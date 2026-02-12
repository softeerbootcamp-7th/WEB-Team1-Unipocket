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
		when(exchangeInfo.getBaseCurrencyCode()).thenReturn(CurrencyCode.KRW);
		when(exchangeInfo.getBaseCurrencyAmount()).thenReturn(BigDecimal.valueOf(10000));
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
		when(expenseEntity.getPaymentMethod()).thenReturn("CARD");
		when(expenseEntity.getMemo()).thenReturn("메모");
		when(expenseEntity.getCardNumber()).thenReturn(null);

		ExpenseUpdateCommand command =
				new ExpenseUpdateCommand(
						expenseId,
						accountBookId,
						"스타벅스",
						Category.FOOD,
						"CARD",
						"메모",
						LocalDateTime.now(),
						BigDecimal.valueOf(1500),
						CurrencyCode.JPY,
						null,
						CurrencyCode.KRW);

		when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(expenseEntity));
		when(exchangeRateService.convertAmount(any(), any(), any(), any()))
				.thenReturn(BigDecimal.valueOf(15000));

		// when
		expenseService.updateExpense(command);

		// then
		verify(exchangeRateService, times(1))
				.convertAmount(any(), any(), any(), any()); // 환율 재계산 호출 확인
		verify(expenseEntity, times(1))
				.updateExchangeInfo(any(), any(), any(), any()); // 환율 정보 업데이트 확인
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
		when(exchangeInfo.getBaseCurrencyCode()).thenReturn(CurrencyCode.KRW);
		when(exchangeInfo.getBaseCurrencyAmount()).thenReturn(BigDecimal.valueOf(15000));
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
		when(expenseEntity.getPaymentMethod()).thenReturn("CARD");
		when(expenseEntity.getMemo()).thenReturn("메모");
		when(expenseEntity.getCardNumber()).thenReturn(null);

		ExpenseUpdateCommand command =
				new ExpenseUpdateCommand(
						expenseId,
						accountBookId,
						"스타벅스",
						Category.FOOD,
						"CARD",
						"메모",
						LocalDateTime.now(),
						BigDecimal.valueOf(15000),
						CurrencyCode.KRW,
						null,
						CurrencyCode.KRW);

		when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(expenseEntity));
		when(exchangeRateService.convertAmount(any(), any(), any(), any()))
				.thenReturn(BigDecimal.valueOf(15000));

		// when
		expenseService.updateExpense(command);

		// then
		verify(exchangeRateService, times(1)).convertAmount(any(), any(), any(), any());
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
}
