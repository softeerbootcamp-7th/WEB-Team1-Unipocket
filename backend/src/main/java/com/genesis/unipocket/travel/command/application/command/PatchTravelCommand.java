package com.genesis.unipocket.travel.command.application.command;

import com.genesis.unipocket.travel.command.presentation.request.TravelUpdateRequest;
import java.time.LocalDate;

public record PatchTravelCommand(
		Long travelId,
		String travelPlaceName,
		LocalDate startDate,
		LocalDate endDate,
		String imageKey) {

	public static PatchTravelCommand of(Long travelId, TravelUpdateRequest request) {
		return new PatchTravelCommand(
				travelId,
				request.travelPlaceName(),
				request.startDate(),
				request.endDate(),
				request.imageKey());
	}
}
