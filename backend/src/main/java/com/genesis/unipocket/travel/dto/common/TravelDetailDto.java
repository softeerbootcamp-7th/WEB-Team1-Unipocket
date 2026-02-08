package com.genesis.unipocket.travel.dto.common;

import com.genesis.unipocket.travel.persistence.entity.Travel;
import java.time.LocalDate;
import java.util.List;

/**
 * Service 계층용 TravelDetail DTO
 */
public record TravelDetailDto(
		Long travelId,
		Long accountBookId,
		String travelPlaceName,
		LocalDate startDate,
		LocalDate endDate,
		String imageKey,
		List<WidgetDto> widgets) {

	public static TravelDetailDto of(Travel travel, List<WidgetDto> widgets) {
		return new TravelDetailDto(
				travel.getId(),
				travel.getAccountBookId(),
				travel.getTravelPlaceName(),
				travel.getStartDate(),
				travel.getEndDate(),
				travel.getImageKey(),
				widgets);
	}
}
