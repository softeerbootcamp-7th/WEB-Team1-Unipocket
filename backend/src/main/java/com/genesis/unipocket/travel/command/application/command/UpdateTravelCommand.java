package com.genesis.unipocket.travel.command.application.command;

import com.genesis.unipocket.travel.command.presentation.request.TravelRequest;
import java.time.LocalDate;

public record UpdateTravelCommand(
		Long travelId,
		String travelPlaceName,
		LocalDate startDate,
		LocalDate endDate,
		String imageKey) {

	public static UpdateTravelCommand of(Long travelId, TravelRequest request) {
		return new UpdateTravelCommand(
				travelId,
				request.travelPlaceName(),
				request.startDate(),
				request.endDate(),
				request.imageKey());
	}
}
