package com.genesis.unipocket.travel.command.application.command;

import java.time.LocalDate;

public record CreateTravelCommand(
		Long accountBookId,
		String travelPlaceName,
		LocalDate startDate,
		LocalDate endDate,
		String imageKey) {

	public static CreateTravelCommand of(
			Long accountBookId,
			String travelPlaceName,
			LocalDate startDate,
			LocalDate endDate,
			String imageKey) {
		return new CreateTravelCommand(
				accountBookId,
				travelPlaceName,
				startDate,
				endDate,
				imageKey);
	}
}
