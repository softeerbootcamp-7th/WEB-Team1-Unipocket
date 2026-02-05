package com.genesis.unipocket.travel.service;

import com.genesis.unipocket.travel.domain.Travel;
import com.genesis.unipocket.travel.domain.TravelWidget;
import com.genesis.unipocket.travel.dto.TravelDetailResponse;
import com.genesis.unipocket.travel.dto.TravelRequest;
import com.genesis.unipocket.travel.dto.TravelResponse;
import com.genesis.unipocket.travel.dto.WidgetDto;
import com.genesis.unipocket.travel.repository.TravelRepository;
import com.genesis.unipocket.travel.repository.TravelWidgetRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TravelService {

	private final TravelRepository travelRepository;
	private final TravelWidgetRepository widgetRepository;

	/**
	 * 여행 폴더 생성
	 */
	@Transactional
	public Long createTravel(TravelRequest request) {
		Travel travel =
				Travel.builder()
						.accountBookId(request.accountBookId())
						.travelPlaceName(request.travelPlaceName())
						.startDate(request.startDate())
						.endDate(request.endDate())
						.imageKey(request.imageKey())
						.build();

		travel.validateDateRange();

		return travelRepository.save(travel).getId();
	}

	/**
	 * 가계부별 여행 목록 조회
	 */
	public List<TravelResponse> getTravels(Long accountBookId) {
		return travelRepository.findAllByAccountBookId(accountBookId).stream()
				.map(TravelResponse::from)
				.collect(Collectors.toList());
	}

	/**
	 * 여행 상세 조회 (위젯 포함)
	 */
	public TravelDetailResponse getTravelDetail(Long travelId) {
		Travel travel =
				travelRepository
						.findById(travelId)
						// TODO: Define TRAVEL_NOT_FOUND in ErrorCode
						.orElseThrow(() -> new IllegalArgumentException("Travel not found"));

		List<WidgetDto> widgets =
				widgetRepository.findAllByTravelIdOrderByWidgetOrderAsc(travelId).stream()
						.map(WidgetDto::from)
						.collect(Collectors.toList());

		return TravelDetailResponse.of(travel, widgets);
	}

	/**
	 * 여행 정보 수정
	 */
	@Transactional
	public void updateTravel(Long travelId, TravelRequest request) {
		Travel travel =
				travelRepository
						.findById(travelId)
						.orElseThrow(() -> new IllegalArgumentException("Travel not found"));

		travel.update(
				request.travelPlaceName(),
				request.startDate(),
				request.endDate(),
				request.imageKey());
	}

	/**
	 * 여행 삭제
	 */
	@Transactional
	public void deleteTravel(Long travelId) {
		Travel travel =
				travelRepository
						.findById(travelId)
						.orElseThrow(() -> new IllegalArgumentException("Travel not found"));

		// Cascade delete widgets? or manual delete?
		// Assuming JPA Cascade or DB Cascade Not set yet.
		// Ideally should delete widgets first.

		// widgetRepository.deleteAllByTravelId(travelId); // Need to implement this in
		// repo if needed
		travelRepository.delete(travel);
	}

	/**
	 * 위젯 배치 수정
	 */
	@Transactional
	public void updateWidgets(Long travelId, List<WidgetDto> newWidgets) {
		Travel travel =
				travelRepository
						.findById(travelId)
						.orElseThrow(() -> new IllegalArgumentException("Travel not found"));

		// Simple strategy: Delete all existing and re-create
		// Optimization: Handle diffs if performance issue arises

		List<TravelWidget> currentWidgets =
				widgetRepository.findAllByTravelIdOrderByWidgetOrderAsc(travelId);
		widgetRepository.deleteAll(currentWidgets);

		List<TravelWidget> widgetsToSave =
				newWidgets.stream()
						.map(
								dto ->
										TravelWidget.builder()
												.travel(travel)
												.widgetType(dto.type())
												.widgetOrder(dto.order())
												.build())
						.collect(Collectors.toList());

		widgetRepository.saveAll(widgetsToSave);
	}
}
