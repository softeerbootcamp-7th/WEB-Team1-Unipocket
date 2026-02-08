package com.genesis.unipocket.travel.facade;

import com.genesis.unipocket.accountbook.service.AccountBookService;
import com.genesis.unipocket.travel.dto.common.WidgetDto;
import com.genesis.unipocket.travel.dto.request.TravelRequest;
import com.genesis.unipocket.travel.dto.request.TravelUpdateRequest;
import com.genesis.unipocket.travel.dto.response.TravelDetailResponse;
import com.genesis.unipocket.travel.dto.response.TravelResponse;
import com.genesis.unipocket.travel.persistence.entity.Travel;
import com.genesis.unipocket.travel.service.TravelService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TravelFacade {

	private final TravelService travelService;
	private final AccountBookService accountBookService;

	/**
	 * Create new travel with ownership validation
	 */
	public Long createTravel(TravelRequest request, UUID userId) {
		// Validate user owns the account book
		accountBookService.getAccountBook(request.accountBookId(), userId.toString());

		return travelService.createTravel(request);
	}

	/**
	 * Get travels for account book with ownership validation
	 */
	public List<TravelResponse> getTravels(Long accountBookId, UUID userId) {
		// Validate user owns the account book
		accountBookService.getAccountBook(accountBookId, userId.toString());

		return travelService.getTravels(accountBookId);
	}

	/**
	 * Get travel detail with ownership validation
	 */
	public TravelDetailResponse getTravelDetail(Long travelId, UUID userId) {
		// Get travel first
		TravelDetailResponse travelDetail = travelService.getTravelDetail(travelId);

		// Validate user owns the associated account book
		accountBookService.getAccountBook(travelDetail.accountBookId(), userId.toString());

		return travelDetail;
	}

	/**
	 * Update travel with ownership validation (full update)
	 */
	public void updateTravel(Long travelId, TravelRequest request, UUID userId) {
		// Get travel to check ownership
		Travel travel = travelService.getTravel(travelId);
		accountBookService.getAccountBook(travel.getAccountBookId(), userId.toString());

		travelService.updateTravel(travelId, request);
	}

	/**
	 * Update travel with ownership validation (partial update/patch)
	 */
	public void patchTravel(Long travelId, TravelUpdateRequest request, UUID userId) {
		// Get travel to check ownership
		Travel travel = travelService.getTravel(travelId);
		accountBookService.getAccountBook(travel.getAccountBookId(), userId.toString());

		travelService.patchTravel(travelId, request);
	}

	/**
	 * Delete travel with ownership validation
	 */
	public void deleteTravel(Long travelId, UUID userId) {
		// Get travel to check ownership
		Travel travel = travelService.getTravel(travelId);
		accountBookService.getAccountBook(travel.getAccountBookId(), userId.toString());

		travelService.deleteTravel(travelId);
	}

	/**
	 * Update widgets with ownership validation
	 */
	public void updateWidgets(Long travelId, List<WidgetDto> widgets, UUID userId) {
		// Get travel to check ownership
		Travel travel = travelService.getTravel(travelId);
		accountBookService.getAccountBook(travel.getAccountBookId(), userId.toString());

		travelService.updateWidgets(travelId, widgets);
	}
}
