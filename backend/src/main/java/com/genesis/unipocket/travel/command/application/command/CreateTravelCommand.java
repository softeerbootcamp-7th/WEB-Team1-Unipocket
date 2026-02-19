package com.genesis.unipocket.travel.command.application.command;

import com.genesis.unipocket.travel.command.presentation.request.TravelRequest;
import java.time.LocalDate;

public record CreateTravelCommand(
		Long accountBookId,
		String travelPlaceName,
		LocalDate startDate,
		LocalDate endDate,
		String imageKey) {

	public static CreateTravelCommand from(Long accountBookId, TravelRequest request) {
		return new CreateTravelCommand(
				accountBookId,
				request.travelPlaceName(),
				request.startDate(),
				request.endDate(),
				request.imageKey());
	}
}
