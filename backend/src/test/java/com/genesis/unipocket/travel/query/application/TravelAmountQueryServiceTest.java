package com.genesis.unipocket.travel.query.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.genesis.unipocket.accountbook.query.persistence.repository.AccountBookQueryRepository;
import com.genesis.unipocket.accountbook.query.persistence.response.AccountBookDetailResponse;
import com.genesis.unipocket.analysis.command.persistence.repository.AnalysisBatchAggregationRepository;
import com.genesis.unipocket.exchange.common.service.ExchangeRateService;
import com.genesis.unipocket.expense.command.persistence.repository.ExpenseRepository;
import com.genesis.unipocket.global.common.enums.CountryCode;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.travel.query.persistence.repository.TravelQueryRepository;
import com.genesis.unipocket.travel.query.persistence.response.TravelQueryResponse;
import com.genesis.unipocket.travel.query.presentation.response.TravelAmountResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("TravelAmountQueryService 단위 테스트")
class TravelAmountQueryServiceTest {

	@Mock private AccountBookQueryRepository accountBookQueryRepository;
	@Mock private TravelQueryRepository travelQueryRepository;
	@Mock private AnalysisBatchAggregationRepository analysisBatchAggregationRepository;
	@Mock private ExchangeRateService exchangeRateService;
	@Mock private ExpenseRepository expenseRepository;

	@InjectMocks private TravelAmountQueryService service;

	@Test
	@DisplayName("여행 총액 조회 성공 - 혼합 로컬 통화는 환산 합산")
	void getTravelAmount_success_mixedLocalCurrencies() {
		String userId = UUID.randomUUID().toString();
		Long accountBookId = 1L;
		Long travelId = 2L;
		LocalDate travelStartDate = LocalDate.of(2026, 2, 1);
		LocalDateTime oldestRaw = LocalDateTime.of(2026, 2, 1, 9, 10);
		LocalDateTime newestRaw = LocalDateTime.of(2026, 2, 15, 20, 30);

		given(accountBookQueryRepository.findDetailById(UUID.fromString(userId), accountBookId))
				.willReturn(
						Optional.of(
								new AccountBookDetailResponse(
										accountBookId,
										"ab",
										CountryCode.JP,
										CountryCode.KR,
										LocalDate.of(2026, 1, 1),
										LocalDate.of(2026, 12, 31))));
		given(travelQueryRepository.findById(travelId))
				.willReturn(
						Optional.of(
								new TravelQueryResponse(
										travelId,
										accountBookId,
										"Tokyo",
										travelStartDate,
										null,
										null)));
		given(analysisBatchAggregationRepository.aggregateTravelRaw(accountBookId, travelId))
				.willReturn(
						new AnalysisBatchAggregationRepository.AmountPairCount(
								new BigDecimal("999.00"), new BigDecimal("3500.00"), 3L));
		given(
						analysisBatchAggregationRepository
								.aggregateTravelLocalAmountGroupedByCurrency(
										accountBookId, travelId))
				.willReturn(
						List.of(
								new AnalysisBatchAggregationRepository.LocalCurrencyGroupRow(
										"JPY", new BigDecimal("100.00"), 1L),
								new AnalysisBatchAggregationRepository.LocalCurrencyGroupRow(
										"USD", new BigDecimal("10.00"), 1L)));
		given(
						exchangeRateService.convertAmount(
								eq(new BigDecimal("10.00")),
								eq(CurrencyCode.USD),
								eq(CurrencyCode.JPY),
								eq(OffsetDateTime.of(2026, 2, 1, 0, 0, 0, 0, ZoneOffset.UTC))))
				.willReturn(new BigDecimal("1500.00"));
		given(
						expenseRepository.findOccurredAtRangeByAccountBookIdAndTravelId(
								accountBookId, travelId))
				.willReturn(new Object[] {new Object[] {oldestRaw, newestRaw}});

		TravelAmountResponse result = service.getTravelAmount(accountBookId, travelId, userId);

		assertThat(result.localCountryCode()).isEqualTo(CountryCode.JP);
		assertThat(result.localCurrencyCode()).isEqualTo(CurrencyCode.JPY);
		assertThat(result.baseCountryCode()).isEqualTo(CountryCode.KR);
		assertThat(result.baseCurrencyCode()).isEqualTo(CurrencyCode.KRW);
		assertThat(result.totalLocalAmount()).isEqualByComparingTo("1600.00");
		assertThat(result.totalBaseAmount()).isEqualByComparingTo("3500.00");
		assertThat(result.oldestExpenseDate()).isEqualTo(oldestRaw.atOffset(ZoneOffset.UTC));
		assertThat(result.newestExpenseDate()).isEqualTo(newestRaw.atOffset(ZoneOffset.UTC));
		verify(exchangeRateService)
				.convertAmount(
						any(BigDecimal.class), eq(CurrencyCode.USD), eq(CurrencyCode.JPY), any());
	}

	@Test
	@DisplayName("여행이 다른 accountBook 소속이면 TRAVEL_NOT_FOUND")
	void getTravelAmount_travelScopeMismatch() {
		String userId = UUID.randomUUID().toString();
		Long accountBookId = 1L;
		Long travelId = 2L;

		given(accountBookQueryRepository.findDetailById(UUID.fromString(userId), accountBookId))
				.willReturn(
						Optional.of(
								new AccountBookDetailResponse(
										accountBookId,
										"ab",
										CountryCode.JP,
										CountryCode.KR,
										LocalDate.of(2026, 1, 1),
										LocalDate.of(2026, 12, 31))));
		given(travelQueryRepository.findById(travelId))
				.willReturn(
						Optional.of(
								new TravelQueryResponse(
										travelId,
										999L,
										"Tokyo",
										LocalDate.of(2026, 2, 1),
										null,
										null)));

		assertThatThrownBy(() -> service.getTravelAmount(accountBookId, travelId, userId))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("code", ErrorCode.TRAVEL_NOT_FOUND);
	}
}
