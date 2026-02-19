package com.genesis.unipocket.travel.command.application.command;

import java.time.LocalDate;

public record UpdateTravelCommand(
		Long travelId,
		String travelPlaceName,
		LocalDate startDate,
		LocalDate endDate,
		String imageKey) {

	public static UpdateTravelCommand of(
			Long travelId,
			String travelPlaceName,
			LocalDate startDate,
			LocalDate endDate,
			String imageKey) {
		return new UpdateTravelCommand(
				travelId,
				travelPlaceName,
				startDate,
				endDate,
				imageKey);
	}
}
