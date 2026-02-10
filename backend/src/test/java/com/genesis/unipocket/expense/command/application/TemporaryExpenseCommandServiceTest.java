package com.genesis.unipocket.expense.command.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.genesis.unipocket.expense.command.application.command.TemporaryExpenseUpdateCommand;
import com.genesis.unipocket.expense.command.application.result.TemporaryExpenseResult;
import com.genesis.unipocket.expense.command.persistence.entity.expense.TemporaryExpense;
import com.genesis.unipocket.expense.command.persistence.entity.expense.TemporaryExpense.TemporaryExpenseStatus;
import com.genesis.unipocket.expense.command.persistence.repository.TemporaryExpenseRepository;
import com.genesis.unipocket.expense.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * <b>TemporaryExpenseService 단위 테스트</b>
 *
 * @author bluefishsez
 * @since 2026-02-08
 */
@ExtendWith(MockitoExtension.class)
class TemporaryExpenseCommandServiceTest {

	@Mock private TemporaryExpenseRepository repository;

	@InjectMocks private TemporaryExpenseCommandService service;

	private TemporaryExpense testExpense;

	@BeforeEach
	void setUp() {
		testExpense =
				TemporaryExpense.builder()
						.tempExpenseId(1L)
						.fileId(10L)
						.merchantName("스타벅스")
						.category(Category.FOOD)
						.localCountryCode(CurrencyCode.KRW)
						.localCurrencyAmount(BigDecimal.valueOf(5000))
						.baseCountryCode(CurrencyCode.KRW)
						.baseCurrencyAmount(BigDecimal.valueOf(5000))
						.paymentsMethod("CARD")
						.occurredAt(LocalDateTime.now())
						.status(TemporaryExpenseStatus.NORMAL)
						.build();
	}

	@Test
	@DisplayName("임시지출내역 단건 조회 성공")
	void findById_Success() {
		// given
		Long tempExpenseId = 1L;
		when(repository.findById(tempExpenseId)).thenReturn(Optional.of(testExpense));

		// when
		TemporaryExpense result = service.findById(tempExpenseId);

		// then
		assertThat(result).isNotNull();
		assertThat(result.getTempExpenseId()).isEqualTo(1L);
		verify(repository).findById(tempExpenseId);
	}

	@Test
	@DisplayName("존재하지 않는 임시지출내역 조회 시 예외 발생")
	void findById_NotFound_ThrowsException() {
		// given
		Long tempExpenseId = 999L;
		when(repository.findById(tempExpenseId)).thenReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> service.findById(tempExpenseId))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("임시지출내역을 찾을 수 없습니다");
	}

	@Test
	@DisplayName("임시지출내역 수정 성공")
	void updateTemporaryExpense_Success() {
		// given
		Long tempExpenseId = 1L;
		TemporaryExpenseUpdateCommand command =
				new TemporaryExpenseUpdateCommand(
						"이디야커피", // merchantName
						Category.FOOD,
						CurrencyCode.KRW,
						BigDecimal.valueOf(3000),
						CurrencyCode.KRW,
						BigDecimal.valueOf(3000),
						"CASH",
						"커피",
						LocalDateTime.now(),
						null);

		when(repository.findById(tempExpenseId)).thenReturn(Optional.of(testExpense));
		when(repository.save(any(TemporaryExpense.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		// when
		TemporaryExpenseResult result = service.updateTemporaryExpense(tempExpenseId, command);

		// then
		assertThat(result.merchantName()).isEqualTo("이디야커피");
		assertThat(result.localCurrencyAmount()).isEqualTo(BigDecimal.valueOf(3000));
		assertThat(result.paymentsMethod()).isEqualTo("CASH");
		verify(repository).save(any(TemporaryExpense.class));
	}

	@Test
	@DisplayName("임시지출내역 삭제 성공")
	void deleteTemporaryExpense_Success() {
		// given
		Long tempExpenseId = 1L;
		when(repository.findById(tempExpenseId)).thenReturn(Optional.of(testExpense));

		// when
		service.deleteTemporaryExpense(tempExpenseId);

		// then
		verify(repository).delete(testExpense);
	}

	@Test
	@DisplayName("존재하지 않는 임시지출내역 삭제 시 예외 발생")
	void deleteTemporaryExpense_NotFound_ThrowsException() {
		// given
		Long tempExpenseId = 999L;
		when(repository.findById(tempExpenseId)).thenReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> service.deleteTemporaryExpense(tempExpenseId))
				.isInstanceOf(IllegalArgumentException.class);
	}
}
