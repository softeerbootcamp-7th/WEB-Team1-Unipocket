package com.genesis.unipocket.expense.query.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.genesis.unipocket.expense.command.facade.port.UserCardFetchService;
import com.genesis.unipocket.expense.command.persistence.repository.ExpenseRepository;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.media.command.application.MediaObjectStorage;
import com.genesis.unipocket.tempexpense.command.facade.port.AccountBookOwnershipValidator;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class ExpenseQueryServiceMerchantSearchTest {

	@Mock private ExpenseRepository expenseRepository;
	@Mock private AccountBookOwnershipValidator accountBookOwnershipValidator;
	@Mock private UserCardFetchService userCardFetchService;
	@Mock private ExpenseMerchantSearchRateLimitService expenseMerchantSearchRateLimitService;
	@Mock private MediaObjectStorage mediaObjectStorage;
	@Mock private TravelInfoReader travelInfoReader;

	@InjectMocks private ExpenseQueryService expenseQueryService;

	@Test
	@DisplayName("거래처명 prefix 검색 성공")
	void searchMerchantNames_success() {
		Long accountBookId = 1L;
		UUID userId = UUID.randomUUID();
		when(expenseRepository.findMerchantNameSuggestions(
						eq(accountBookId), eq("스타"), eq(PageRequest.of(0, 10))))
				.thenReturn(List.of("스타벅스", "스타필드"));

		List<String> result =
				expenseQueryService.searchMerchantNames(accountBookId, userId, "스타", 10);

		assertThat(result).containsExactly("스타벅스", "스타필드");
		verify(accountBookOwnershipValidator).validateOwnership(accountBookId, userId.toString());
		verify(expenseMerchantSearchRateLimitService).validate(userId);
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
