package com.genesis.unipocket.expense.query.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.genesis.unipocket.expense.common.validation.ExpenseOwnershipValidator;
import com.genesis.unipocket.expense.query.facade.port.ExpenseMediaAccessService;
import com.genesis.unipocket.expense.query.persistence.response.ExpenseOneShotRow;
import com.genesis.unipocket.expense.query.presentation.response.ExpenseFileUrlResponse;
import com.genesis.unipocket.expense.query.service.ExpenseQueryService;
import com.genesis.unipocket.global.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.global.common.enums.ExpenseSource;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ExpenseQueryFacadeTest {

	@Mock private ExpenseQueryService expenseQueryService;
	@Mock private ExpenseOwnershipValidator expenseOwnershipValidator;
	@Mock private ExpenseMediaAccessService expenseMediaAccessService;

	@InjectMocks private ExpenseQueryFacade expenseQueryFacade;

	@Test
	@DisplayName("Facade에서 presigned URL을 발급한다")
	void getExpenseFileUrl_success() {
		Long accountBookId = 1L;
		Long expenseId = 10L;
		UUID userId = UUID.randomUUID();
		ReflectionTestUtils.setField(expenseQueryFacade, "presignedGetExpirationSeconds", 600);

		when(expenseQueryService.getExpenseOneShot(expenseId, accountBookId))
				.thenReturn(createRow(expenseId, accountBookId, "expense/file-key"));
		when(expenseMediaAccessService.issueGetPath("expense/file-key", Duration.ofSeconds(600)))
				.thenReturn("signed-url");

		ExpenseFileUrlResponse response =
				expenseQueryFacade.getExpenseFileUrl(expenseId, accountBookId, userId);

		assertThat(response.presignedUrl()).isEqualTo("signed-url");
		assertThat(response.expiresInSeconds()).isEqualTo(600);
		verify(expenseOwnershipValidator).validateOwnership(accountBookId, userId.toString());
		verify(expenseQueryService).getExpenseOneShot(expenseId, accountBookId);
		verify(expenseMediaAccessService).issueGetPath("expense/file-key", Duration.ofSeconds(600));
	}

	@Test
	@DisplayName("파일 링크가 비어있으면 presigned URL 발급 예외를 반환한다")
	void getExpenseFileUrl_blankFileLink_throws() {
		Long accountBookId = 1L;
		Long expenseId = 10L;
		UUID userId = UUID.randomUUID();
		ReflectionTestUtils.setField(expenseQueryFacade, "presignedGetExpirationSeconds", 600);

		when(expenseQueryService.getExpenseOneShot(expenseId, accountBookId))
				.thenReturn(createRow(expenseId, accountBookId, " "));
		when(expenseMediaAccessService.issueGetPath(" ", Duration.ofSeconds(600)))
				.thenThrow(new BusinessException(ErrorCode.EXPENSE_FILE_LINK_NOT_FOUND));

		assertThatThrownBy(
						() ->
								expenseQueryFacade.getExpenseFileUrl(
										expenseId, accountBookId, userId))
				.isInstanceOf(BusinessException.class)
				.satisfies(
						ex ->
								assertThat(((BusinessException) ex).getCode())
										.isEqualTo(ErrorCode.EXPENSE_FILE_LINK_NOT_FOUND));
		verify(expenseOwnershipValidator).validateOwnership(accountBookId, userId.toString());
		verify(expenseQueryService).getExpenseOneShot(expenseId, accountBookId);
		verify(expenseMediaAccessService).issueGetPath(" ", Duration.ofSeconds(600));
	}

	private ExpenseOneShotRow createRow(Long expenseId, Long accountBookId, String fileLink) {
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
