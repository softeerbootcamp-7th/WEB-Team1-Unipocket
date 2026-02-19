package com.genesis.unipocket.travel.command.presentation.request;

import java.time.LocalDate;

public record TravelUpdateRequest(
		String travelPlaceName, LocalDate startDate, LocalDate endDate, String imageKey) {}
