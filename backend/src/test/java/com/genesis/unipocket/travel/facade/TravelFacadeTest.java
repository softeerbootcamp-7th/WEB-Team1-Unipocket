package com.genesis.unipocket.travel.facade;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.genesis.unipocket.accountbook.service.AccountBookService;
import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.travel.dto.common.WidgetDto;
import com.genesis.unipocket.travel.dto.request.TravelRequest;
import com.genesis.unipocket.travel.dto.request.TravelUpdateRequest;
import com.genesis.unipocket.travel.dto.response.TravelDetailResponse;
import com.genesis.unipocket.travel.dto.response.TravelResponse;
import com.genesis.unipocket.travel.persistence.entity.Travel;
import com.genesis.unipocket.travel.persistence.entity.WidgetType;
import com.genesis.unipocket.travel.service.TravelService;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TravelFacadeTest {

	@Mock private TravelService travelService;

	@Mock private AccountBookService accountBookService;

	@InjectMocks private TravelFacade travelFacade;

	private UUID userId;
	private Long accountBookId;
	private Long travelId;

	@BeforeEach
	void setUp() {
		userId = UUID.randomUUID();
		accountBookId = 1L;
		travelId = 100L;
	}

	@Test
	@DisplayName("createTravel - 소유자가 여행 폴더 생성 성공")
	void createTravel_Success() {
		// Given
		TravelRequest request =
				new TravelRequest(
						accountBookId,
						"도쿄 여행",
						LocalDate.of(2024, 3, 1),
						LocalDate.of(2024, 3, 7),
						null);

		when(accountBookService.getAccountBook(accountBookId, userId.toString()))
				.thenReturn(any()); // Validation passes
		when(travelService.createTravel(request)).thenReturn(travelId);

		// When
		Long result = travelFacade.createTravel(request, userId);

		// Then
		assertThat(result).isEqualTo(travelId);
		verify(accountBookService).getAccountBook(accountBookId, userId.toString());
		verify(travelService).createTravel(request);
	}

	@Test
	@DisplayName("createTravel - 비소유자가 여행 폴더 생성 실패 (403)")
	void createTravel_Forbidden() {
		// Given
		TravelRequest request =
				new TravelRequest(
						accountBookId,
						"도쿄 여행",
						LocalDate.of(2024, 3, 1),
						LocalDate.of(2024, 3, 7),
						null);

		when(accountBookService.getAccountBook(accountBookId, userId.toString()))
				.thenThrow(new RuntimeException("Not found"));

		// When & Then
		assertThatThrownBy(() -> travelFacade.createTravel(request, userId))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN)
				.hasMessage("가계부에 대한 권한이 없습니다.");

		verify(travelService, never()).createTravel(any());
	}

	@Test
	@DisplayName("getTravels - 소유자가 여행 목록 조회 성공")
	void getTravels_Success() {
		// Given
		List<TravelResponse> travels =
				List.of(
						new TravelResponse(
								1L,
								accountBookId,
								"도쿄",
								LocalDate.now(),
								LocalDate.now().plusDays(7),
								null),
						new TravelResponse(
								2L,
								accountBookId,
								"오사카",
								LocalDate.now(),
								LocalDate.now().plusDays(5),
								null));

		when(accountBookService.getAccountBook(accountBookId, userId.toString())).thenReturn(any());
		when(travelService.getTravels(accountBookId)).thenReturn(travels);

		// When
		var result = travelFacade.getTravels(accountBookId, userId);

		// Then
		assertThat(result).hasSize(2);
		verify(accountBookService).getAccountBook(accountBookId, userId.toString());
	}

	@Test
	@DisplayName("getTravels - 비소유자가 여행 목록 조회 실패 (403)")
	void getTravels_Forbidden() {
		// Given
		when(accountBookService.getAccountBook(accountBookId, userId.toString()))
				.thenThrow(new RuntimeException("Not found"));

		// When & Then
		assertThatThrownBy(() -> travelFacade.getTravels(accountBookId, userId))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN)
				.hasMessage("가계부에 대한 권한이 없습니다.");

		verify(travelService, never()).getTravels(any());
	}

	@Test
	@DisplayName("getTravelDetail - 소유자가 여행 상세 조회 성공")
	void getTravelDetail_Success() {
		// Given
		TravelDetailResponse detail =
				new TravelDetailResponse(
						travelId,
						accountBookId,
						"도쿄",
						LocalDate.now(),
						LocalDate.now().plusDays(7),
						null,
						List.of());

		when(travelService.getTravelDetail(travelId)).thenReturn(detail);
		when(accountBookService.getAccountBook(accountBookId, userId.toString())).thenReturn(any());

		// When
		var result = travelFacade.getTravelDetail(travelId, userId);

		// Then
		assertThat(result.travelId()).isEqualTo(travelId);
		verify(accountBookService).getAccountBook(accountBookId, userId.toString());
	}

	@Test
	@DisplayName("getTravelDetail - 비소유자가 여행 상세 조회 실패 (403)")
	void getTravelDetail_Forbidden() {
		// Given
		TravelDetailResponse detail =
				new TravelDetailResponse(
						travelId,
						accountBookId,
						"도쿄",
						LocalDate.now(),
						LocalDate.now().plusDays(7),
						null,
						List.of());

		when(travelService.getTravelDetail(travelId)).thenReturn(detail);
		when(accountBookService.getAccountBook(accountBookId, userId.toString()))
				.thenThrow(new RuntimeException("Not found"));

		// When & Then
		assertThatThrownBy(() -> travelFacade.getTravelDetail(travelId, userId))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN);
	}

	@Test
	@DisplayName("updateTravel - 소유자가 여행 수정 성공")
	void updateTravel_Success() {
		// Given
		TravelRequest request =
				new TravelRequest(
						accountBookId,
						"도쿄 업데이트",
						LocalDate.of(2024, 3, 1),
						LocalDate.of(2024, 3, 10),
						null);

		Travel travel = Travel.builder().accountBookId(accountBookId).build();

		when(travelService.getTravel(travelId)).thenReturn(travel);
		when(accountBookService.getAccountBook(accountBookId, userId.toString())).thenReturn(any());

		// When
		travelFacade.updateTravel(travelId, request, userId);

		// Then
		verify(accountBookService).getAccountBook(accountBookId, userId.toString());
		verify(travelService).updateTravel(travelId, request);
	}

	@Test
	@DisplayName("updateTravel - 비소유자가 여행 수정 실패 (403)")
	void updateTravel_Forbidden() {
		// Given
		TravelRequest request =
				new TravelRequest(
						accountBookId,
						"도쿄 업데이트",
						LocalDate.of(2024, 3, 1),
						LocalDate.of(2024, 3, 10),
						null);

		Travel travel = Travel.builder().accountBookId(accountBookId).build();

		when(travelService.getTravel(travelId)).thenReturn(travel);
		when(accountBookService.getAccountBook(accountBookId, userId.toString()))
				.thenThrow(new RuntimeException("Not found"));

		// When & Then
		assertThatThrownBy(() -> travelFacade.updateTravel(travelId, request, userId))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN);

		verify(travelService, never()).updateTravel(any(), any());
	}

	@Test
	@DisplayName("patchTravel - 소유자가 여행 부분 수정 성공")
	void patchTravel_Success() {
		// Given
		TravelUpdateRequest request =
				new TravelUpdateRequest(
						"도쿄 업데이트", LocalDate.of(2024, 3, 1), LocalDate.of(2024, 3, 10), null);

		Travel travel = Travel.builder().accountBookId(accountBookId).build();

		when(travelService.getTravel(travelId)).thenReturn(travel);
		when(accountBookService.getAccountBook(accountBookId, userId.toString())).thenReturn(any());

		// When
		travelFacade.patchTravel(travelId, request, userId);

		// Then
		verify(accountBookService).getAccountBook(accountBookId, userId.toString());
		verify(travelService).patchTravel(travelId, request);
	}

	@Test
	@DisplayName("patchTravel - 비소유자가 여행 부분 수정 실패 (403)")
	void patchTravel_Forbidden() {
		// Given
		TravelUpdateRequest request =
				new TravelUpdateRequest(
						"도쿄 업데이트", LocalDate.of(2024, 3, 1), LocalDate.of(2024, 3, 10), null);

		Travel travel = Travel.builder().accountBookId(accountBookId).build();

		when(travelService.getTravel(travelId)).thenReturn(travel);
		when(accountBookService.getAccountBook(accountBookId, userId.toString()))
				.thenThrow(new RuntimeException("Not found"));

		// When & Then
		assertThatThrownBy(() -> travelFacade.patchTravel(travelId, request, userId))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN);

		verify(travelService, never()).patchTravel(any(), any());
	}

	@Test
	@DisplayName("deleteTravel - 소유자가 여행 삭제 성공")
	void deleteTravel_Success() {
		// Given
		Travel travel = Travel.builder().accountBookId(accountBookId).build();

		when(travelService.getTravel(travelId)).thenReturn(travel);
		when(accountBookService.getAccountBook(accountBookId, userId.toString())).thenReturn(any());

		// When
		travelFacade.deleteTravel(travelId, userId);

		// Then
		verify(accountBookService).getAccountBook(accountBookId, userId.toString());
		verify(travelService).deleteTravel(travelId);
	}

	@Test
	@DisplayName("deleteTravel - 비소유자가 여행 삭제 실패 (403)")
	void deleteTravel_Forbidden() {
		// Given
		Travel travel = Travel.builder().accountBookId(accountBookId).build();

		when(travelService.getTravel(travelId)).thenReturn(travel);
		when(accountBookService.getAccountBook(accountBookId, userId.toString()))
				.thenThrow(new RuntimeException("Not found"));

		// When & Then
		assertThatThrownBy(() -> travelFacade.deleteTravel(travelId, userId))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN);

		verify(travelService, never()).deleteTravel(any());
	}

	@Test
	@DisplayName("updateWidgets - 소유자가 위젯 업데이트 성공")
	void updateWidgets_Success() {
		// Given
		List<WidgetDto> widgets =
				List.of(
						new WidgetDto(WidgetType.SUMMARY_CARD, 1),
						new WidgetDto(WidgetType.GRAPH_DAILY, 2));

		Travel travel = Travel.builder().accountBookId(accountBookId).build();

		when(travelService.getTravel(travelId)).thenReturn(travel);
		when(accountBookService.getAccountBook(accountBookId, userId.toString())).thenReturn(any());

		// When
		travelFacade.updateWidgets(travelId, widgets, userId);

		// Then
		verify(accountBookService).getAccountBook(accountBookId, userId.toString());
		verify(travelService).updateWidgets(travelId, widgets);
	}

	@Test
	@DisplayName("updateWidgets - 비소유자가 위젯 업데이트 실패 (403)")
	void updateWidgets_Forbidden() {
		// Given
		List<WidgetDto> widgets =
				List.of(
						new WidgetDto(WidgetType.SUMMARY_CARD, 1),
						new WidgetDto(WidgetType.GRAPH_DAILY, 2));

		Travel travel = Travel.builder().accountBookId(accountBookId).build();

		when(travelService.getTravel(travelId)).thenReturn(travel);
		when(accountBookService.getAccountBook(accountBookId, userId.toString()))
				.thenThrow(new RuntimeException("Not found"));

		// When & Then
		assertThatThrownBy(() -> travelFacade.updateWidgets(travelId, widgets, userId))
				.isInstanceOf(BusinessException.class)
				.hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN);

		verify(travelService, never()).updateWidgets(any(), any());
	}
}
