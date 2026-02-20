package com.genesis.unipocket.travel.command.application.command;

import java.time.LocalDate;

public record PatchTravelCommand(
		Long travelId,
		String travelPlaceName,
		LocalDate startDate,
		LocalDate endDate,
		String imageKey) {

	public static PatchTravelCommand of(
			Long travelId,
			String travelPlaceName,
			LocalDate startDate,
			LocalDate endDate,
			String imageKey) {
		return new PatchTravelCommand(travelId, travelPlaceName, startDate, endDate, imageKey);
	}
}
