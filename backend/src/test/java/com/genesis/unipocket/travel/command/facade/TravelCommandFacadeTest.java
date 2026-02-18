package com.genesis.unipocket.travel.command.facade;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.genesis.unipocket.travel.command.application.TravelCommandService;
import com.genesis.unipocket.travel.command.facade.port.TravelDefaultWidgetPort;
import com.genesis.unipocket.travel.command.persistence.entity.Travel;
import com.genesis.unipocket.travel.command.presentation.request.TravelUpdateRequest;
import com.genesis.unipocket.travel.common.validate.UserAccountBookValidator;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("TravelCommandFacade 단위 테스트")
class TravelCommandFacadeTest {

	@Mock private TravelCommandService travelCommandService;
	@Mock private UserAccountBookValidator userAccountBookValidator;
	@Mock private TravelDefaultWidgetPort travelDefaultWidgetPort;

	@InjectMocks private TravelCommandFacade travelCommandFacade;

	@Test
	@DisplayName("patchTravel은 travelId로 accountBookId를 조회해 소유권을 검증한다")
	void patchTravel_validatesOwnershipByResolvedAccountBook() {
		UUID userId = UUID.randomUUID();
		Long travelId = 10L;
		Long accountBookId = 100L;
		Travel travel = org.mockito.Mockito.mock(Travel.class);

		when(travelCommandService.getTravel(travelId)).thenReturn(travel);
		when(travel.getAccountBookId()).thenReturn(accountBookId);

		travelCommandFacade.patchTravel(
				travelId,
				new TravelUpdateRequest("Seoul", LocalDate.of(2026, 2, 1), null, null),
				userId);

		verify(travelCommandService).getTravel(travelId);
		verify(userAccountBookValidator).validateUserAccountBook(userId.toString(), accountBookId);
	}

	@Test
	@DisplayName("deleteTravel은 travelId로 accountBookId를 조회해 소유권을 검증한다")
	void deleteTravel_validatesOwnershipByResolvedAccountBook() {
		UUID userId = UUID.randomUUID();
		Long travelId = 11L;
		Long accountBookId = 101L;
		Travel travel = org.mockito.Mockito.mock(Travel.class);

		when(travelCommandService.getTravel(travelId)).thenReturn(travel);
		when(travel.getAccountBookId()).thenReturn(accountBookId);

		travelCommandFacade.deleteTravel(travelId, userId);

		verify(travelCommandService).getTravel(travelId);
		verify(userAccountBookValidator).validateUserAccountBook(userId.toString(), accountBookId);
		verify(travelCommandService).deleteTravel(travelId);
	}
}
