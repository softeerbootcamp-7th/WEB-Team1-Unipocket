package com.genesis.unipocket.travel.query.persistence.response;

import java.time.LocalDate;

public record TravelQueryResponse(
		Long travelId,
		Long accountBookId,
		String travelPlaceName,
		LocalDate startDate,
		LocalDate endDate,
		String imageKey) {}
