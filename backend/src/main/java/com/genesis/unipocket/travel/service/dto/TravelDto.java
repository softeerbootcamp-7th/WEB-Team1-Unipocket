package com.genesis.unipocket.travel.service.dto;

import com.genesis.unipocket.travel.domain.Travel;
import java.time.LocalDate;

/**
 * Service 계층용 Travel DTO
 */
public record TravelDto(
		Long travelId,
		Long accountBookId,
		String travelPlaceName,
		LocalDate startDate,
		LocalDate endDate,
		String imageKey) {

	public static TravelDto from(Travel travel) {
		return new TravelDto(
				travel.getId(),
				travel.getAccountBookId(),
				travel.getTravelPlaceName(),
				travel.getStartDate(),
				travel.getEndDate(),
				travel.getImageKey());
	}
}
