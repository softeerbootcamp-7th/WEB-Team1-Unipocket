package com.genesis.unipocket.travel.query.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.genesis.unipocket.global.common.enums.WidgetType;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.travel.common.validate.UserAccountBookValidator;
import com.genesis.unipocket.travel.query.persistence.repository.TravelQueryRepository;
import com.genesis.unipocket.travel.query.persistence.response.TravelDetailQueryResponse;
import com.genesis.unipocket.travel.query.persistence.response.TravelQueryResponse;
import com.genesis.unipocket.travel.query.persistence.response.WidgetOrderDto;
import java.time.LocalDate;
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
@DisplayName("TravelQueryService 단위 테스트")
class TravelQueryServiceTest {

	@Mock private TravelQueryRepository travelQueryRepository;
	@Mock private UserAccountBookValidator userAccountBookValidator;

	@InjectMocks private TravelQueryService travelQueryService;

	@Test
	@DisplayName("getTravelDetail은 travel/accountBook 소속이 다르면 TRAVEL_NOT_FOUND를 던진다")
	void getTravelDetail_scopeMismatch_throwsNotFound() {
		UUID userId = UUID.randomUUID();
		Long requestAccountBookId = 1L;
		Long travelId = 77L;

		when(travelQueryRepository.findById(travelId))
				.thenReturn(
						Optional.of(
								new TravelQueryResponse(
										travelId,
										999L,
										"Tokyo",
										LocalDate.of(2026, 2, 1),
										LocalDate.of(2026, 2, 10),
										null)));

		assertThatThrownBy(
						() ->
								travelQueryService.getTravelDetail(
										requestAccountBookId, travelId, userId))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("code", ErrorCode.TRAVEL_NOT_FOUND);

		verify(userAccountBookValidator)
				.validateUserAccountBook(userId.toString(), requestAccountBookId);
	}

	@Test
	@DisplayName("getTravelDetail은 소속이 일치하면 상세와 위젯을 반환한다")
	void getTravelDetail_success() {
		UUID userId = UUID.randomUUID();
		Long accountBookId = 1L;
		Long travelId = 88L;
		TravelQueryResponse travel =
				new TravelQueryResponse(
						travelId,
						accountBookId,
						"Busan",
						LocalDate.of(2026, 3, 1),
						LocalDate.of(2026, 3, 20),
						"img");

		when(travelQueryRepository.findById(travelId)).thenReturn(Optional.of(travel));
		when(travelQueryRepository.findAllByTravelId(travelId))
				.thenReturn(List.of(new WidgetOrderDto(WidgetType.BUDGET, 0)));

		TravelDetailQueryResponse result =
				travelQueryService.getTravelDetail(accountBookId, travelId, userId);

		assertThat(result.travelId()).isEqualTo(travelId);
		assertThat(result.accountBookId()).isEqualTo(accountBookId);
		assertThat(result.widgets()).hasSize(1);
	}
}
