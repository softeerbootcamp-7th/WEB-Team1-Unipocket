package com.genesis.unipocket.travel.command.presentation.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.genesis.unipocket.global.common.json.FixedScaleDecimalStringSerializer;
import com.genesis.unipocket.travel.command.application.result.TravelBudgetUpdateResult;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TravelBudgetUpdateResponse(
		Long travelId,
		@JsonSerialize(using = FixedScaleDecimalStringSerializer.class) BigDecimal budget,
		@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
				LocalDateTime budgetCreatedAt) {

	public static TravelBudgetUpdateResponse from(TravelBudgetUpdateResult result) {
		return new TravelBudgetUpdateResponse(
				result.travelId(), result.budget(), result.budgetCreatedAt());
	}
}
