package com.genesis.unipocket.travel.query.persistence.response;

import java.time.LocalDate;
import java.util.List;

public record TravelDetailQueryResponse(
		Long travelId,
		Long accountBookId,
		String travelPlaceName,
		LocalDate startDate,
		LocalDate endDate,
		String imageKey,
		List<WidgetOrderDto> widgets) {

	public static TravelDetailQueryResponse of(
			TravelQueryResponse travel, List<WidgetOrderDto> widgets) {
		return new TravelDetailQueryResponse(
				travel.travelId(),
				travel.accountBookId(),
				travel.travelPlaceName(),
				travel.startDate(),
				travel.endDate(),
				travel.imageKey(),
				widgets);
	}
}
