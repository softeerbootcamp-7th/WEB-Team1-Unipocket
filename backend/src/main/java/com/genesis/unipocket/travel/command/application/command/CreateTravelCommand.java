package com.genesis.unipocket.travel.command.application.command;

import com.genesis.unipocket.travel.command.presentation.request.TravelRequest;
import java.time.LocalDate;

public record CreateTravelCommand(
		Long accountBookId,
		String travelPlaceName,
		LocalDate startDate,
		LocalDate endDate,
		String imageKey) {

	public static CreateTravelCommand from(TravelRequest request) {
		return new CreateTravelCommand(
				request.accountBookId(),
				request.travelPlaceName(),
				request.startDate(),
				request.endDate(),
				request.imageKey());
	}
}
