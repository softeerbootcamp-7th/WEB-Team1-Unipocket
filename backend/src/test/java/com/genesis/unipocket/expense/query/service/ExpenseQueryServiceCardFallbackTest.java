package com.genesis.unipocket.expense.query.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.genesis.unipocket.expense.common.facade.port.UserCardFetchService;
import com.genesis.unipocket.expense.query.persistence.repository.ExpenseQueryRepository;
import com.genesis.unipocket.expense.query.persistence.response.ExpenseQueryRow;
import com.genesis.unipocket.expense.query.port.AccountBookOwnershipValidator;
import com.genesis.unipocket.expense.query.port.TravelInfoReader;
import com.genesis.unipocket.expense.query.service.dto.ExpenseQueryResult;
import com.genesis.unipocket.global.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.global.common.enums.ExpenseSource;
import com.genesis.unipocket.media.command.application.MediaObjectStorage;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExpenseQueryServiceCardFallbackTest {

	@Mock private ExpenseQueryRepository expenseQueryRepository;
	@Mock private AccountBookOwnershipValidator accountBookOwnershipValidator;
	@Mock private UserCardFetchService userCardFetchService;
	@Mock private MediaObjectStorage mediaObjectStorage;
	@Mock private TravelInfoReader travelInfoReader;

	@InjectMocks private ExpenseQueryService expenseQueryService;

	@Test
	@DisplayName("지출내역의 카드 ID와 실제 카드 엔티티가 어긋나면 카드 정보는 null로 반환한다")
	void getExpense_cardIdMismatch_returnsNullCardInfo() {
		Long accountBookId = 1L;
		Long expenseId = 10L;
		Long mismatchedUserCardId = 99L;
		UUID userId = UUID.randomUUID();

		ExpenseQueryRow row =
				new ExpenseQueryRow(
						expenseId,
						accountBookId,
						null,
						Category.FOOD,
						CurrencyCode.KRW,
						new BigDecimal("10000"),
						BigDecimal.ONE,
						CurrencyCode.KRW,
						new BigDecimal("10000"),
						OffsetDateTime.parse("2026-02-01T00:00:00Z"),
						LocalDateTime.of(2026, 2, 1, 0, 0),
						"스타벅스",
						null,
						mismatchedUserCardId,
						ExpenseSource.MANUAL,
						null,
						null,
						null);

		when(expenseQueryRepository.findExpense(accountBookId, expenseId))
				.thenReturn(Optional.of(row));
		when(userCardFetchService.getUserCard(mismatchedUserCardId)).thenReturn(Optional.empty());

		ExpenseQueryResult result =
				expenseQueryService.getExpense(expenseId, accountBookId, userId);

		assertThat(result.userCardId()).isNull();
		assertThat(result.cardCompany()).isNull();
		assertThat(result.cardLabel()).isNull();
		assertThat(result.cardLastDigits()).isNull();
		verify(accountBookOwnershipValidator).validateOwnership(accountBookId, userId.toString());
	}
}
