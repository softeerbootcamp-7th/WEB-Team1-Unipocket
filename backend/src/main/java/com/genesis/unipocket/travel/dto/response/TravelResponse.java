package com.genesis.unipocket.travel.dto.response;

import com.genesis.unipocket.travel.persistence.entity.Travel;
import java.time.LocalDate;

public record TravelResponse(
		Long travelId,
		Long accountBookId,
		String travelPlaceName,
		LocalDate startDate,
		LocalDate endDate,
		String imageKey) {

	public static TravelResponse from(Travel travel) {
		return new TravelResponse(
				travel.getId(),
				travel.getAccountBookId(),
				travel.getTravelPlaceName(),
				travel.getStartDate(),
				travel.getEndDate(),
				travel.getImageKey());
	}
}
