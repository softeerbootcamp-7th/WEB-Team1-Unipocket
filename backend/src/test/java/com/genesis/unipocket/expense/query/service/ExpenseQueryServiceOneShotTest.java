package com.genesis.unipocket.expense.query.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.genesis.unipocket.expense.query.persistence.repository.ExpenseQueryDslRepository;
import com.genesis.unipocket.expense.query.persistence.response.ExpenseOneShotRow;
import com.genesis.unipocket.global.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.global.common.enums.ExpenseSource;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExpenseQueryServiceOneShotTest {

	@Mock private ExpenseQueryDslRepository expenseQueryRepository;

	@InjectMocks private ExpenseQueryService expenseQueryService;

	@Test
	@DisplayName("one-shot 상세 조회에서 지출이 없으면 예외를 반환한다")
	void getExpenseOneShot_notFound_throws() {
		Long accountBookId = 1L;
		Long expenseId = 10L;
		when(expenseQueryRepository.findExpenseOneShot(accountBookId, expenseId))
				.thenReturn(Optional.empty());

		assertThatThrownBy(() -> expenseQueryService.getExpenseOneShot(expenseId, accountBookId))
				.isInstanceOf(BusinessException.class)
				.satisfies(
						ex ->
								assertThat(((BusinessException) ex).getCode())
										.isEqualTo(ErrorCode.EXPENSE_NOT_FOUND));
	}

	@Test
	@DisplayName("one-shot 상세 조회 성공 시 row를 그대로 반환한다")
	void getExpenseOneShot_success() {
		Long accountBookId = 1L;
		Long expenseId = 10L;
		ExpenseOneShotRow row = createOneShotRow(expenseId, accountBookId, "expense/file-key");
		when(expenseQueryRepository.findExpenseOneShot(accountBookId, expenseId))
				.thenReturn(Optional.of(row));
		ExpenseOneShotRow result = expenseQueryService.getExpenseOneShot(expenseId, accountBookId);

		assertThat(result).isEqualTo(row);
	}

	private ExpenseOneShotRow createOneShotRow(
			Long expenseId, Long accountBookId, String fileLink) {
		return new ExpenseOneShotRow(
				expenseId,
				accountBookId,
				null,
				null,
				null,
				"스타벅스",
				BigDecimal.ONE,
				Category.FOOD,
				OffsetDateTime.parse("2026-02-01T00:00:00Z"),
				LocalDateTime.of(2026, 2, 1, 0, 0),
				new BigDecimal("10000"),
				CurrencyCode.KRW,
				new BigDecimal("10000"),
				CurrencyCode.KRW,
				null,
				ExpenseSource.MANUAL,
				null,
				null,
				fileLink,
				null,
				null,
				null,
				null);
	}
}
