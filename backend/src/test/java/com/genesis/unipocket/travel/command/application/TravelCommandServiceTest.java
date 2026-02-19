package com.genesis.unipocket.travel.command.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.travel.command.persistence.entity.Travel;
import com.genesis.unipocket.travel.command.persistence.repository.TravelCommandRepository;
import com.genesis.unipocket.widget.command.persistence.repository.TravelWidgetJpaRepository;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("TravelCommandService 단위 테스트")
class TravelCommandServiceTest {

	@Mock private TravelCommandRepository travelRepository;
	@Mock private TravelWidgetJpaRepository travelWidgetJpaRepository;

	@InjectMocks private TravelCommandService travelCommandService;

	@Test
	@DisplayName("deleteTravel은 travel_widgets를 먼저 정리하고 travel을 삭제한다")
	void deleteTravel_deletesWidgetsAndTravel() {
		Long travelId = 5L;
		Travel travel =
				Travel.builder()
						.accountBookId(1L)
						.travelPlaceName("Paris")
						.startDate(LocalDate.of(2026, 1, 1))
						.endDate(LocalDate.of(2026, 1, 10))
						.build();

		when(travelRepository.findById(travelId)).thenReturn(Optional.of(travel));

		travelCommandService.deleteTravel(travelId);

		verify(travelWidgetJpaRepository).deleteAllByTravelId(travelId);
		verify(travelRepository).delete(travel);
	}

	@Test
	@DisplayName("deleteTravel은 travel이 없으면 TRAVEL_NOT_FOUND를 던진다")
	void deleteTravel_notFound_throws() {
		Long travelId = 6L;
		when(travelRepository.findById(travelId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> travelCommandService.deleteTravel(travelId))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("code", ErrorCode.TRAVEL_NOT_FOUND);
	}
}
