package com.genesis.unipocket.expense.query.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.genesis.unipocket.expense.query.persistence.repository.ExpenseQueryRepository;
import com.genesis.unipocket.expense.query.port.AccountBookOwnershipValidator;
import com.genesis.unipocket.expense.query.port.TravelInfoReader;
import com.genesis.unipocket.expense.query.port.UserCardReadService;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.media.command.application.MediaObjectStorage;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExpenseQueryServiceMerchantSearchTest {

	@Mock private ExpenseQueryRepository expenseQueryRepository;
	@Mock private AccountBookOwnershipValidator accountBookOwnershipValidator;
	@Mock private UserCardReadService userCardReadService;
	@Mock private MediaObjectStorage mediaObjectStorage;
	@Mock private TravelInfoReader travelInfoReader;

	@InjectMocks private ExpenseQueryService expenseQueryService;

	@Test
	@DisplayName("거래처명 prefix 검색 성공")
	void searchMerchantNames_success() {
		Long accountBookId = 1L;
		UUID userId = UUID.randomUUID();
		when(expenseQueryRepository.findMerchantNameSuggestions(accountBookId, "스타", 10))
				.thenReturn(List.of("스타벅스", "스타필드"));

		List<String> result =
				expenseQueryService.searchMerchantNames(accountBookId, userId, "스타", 10);

		assertThat(result).containsExactly("스타벅스", "스타필드");
		verify(accountBookOwnershipValidator).validateOwnership(accountBookId, userId.toString());
	}

	@Test
	@DisplayName("검색어가 비어있으면 예외")
	void searchMerchantNames_blankQuery_throws() {
		assertThatThrownBy(
						() ->
								expenseQueryService.searchMerchantNames(
										1L, UUID.randomUUID(), " ", 10))
				.isInstanceOf(BusinessException.class)
				.satisfies(
						ex ->
								assertThat(((BusinessException) ex).getCode())
										.isEqualTo(ErrorCode.INVALID_INPUT_VALUE));
	}

	@Test
	@DisplayName("limit 범위를 벗어나면 예외")
	void searchMerchantNames_invalidLimit_throws() {
		assertThatThrownBy(
						() ->
								expenseQueryService.searchMerchantNames(
										1L, UUID.randomUUID(), "스타", 0))
				.isInstanceOf(BusinessException.class)
				.satisfies(
						ex ->
								assertThat(((BusinessException) ex).getCode())
										.isEqualTo(ErrorCode.INVALID_INPUT_VALUE));
		assertThatThrownBy(
						() ->
								expenseQueryService.searchMerchantNames(
										1L, UUID.randomUUID(), "스타", 21))
				.isInstanceOf(BusinessException.class)
				.satisfies(
						ex ->
								assertThat(((BusinessException) ex).getCode())
										.isEqualTo(ErrorCode.INVALID_INPUT_VALUE));
	}
}
