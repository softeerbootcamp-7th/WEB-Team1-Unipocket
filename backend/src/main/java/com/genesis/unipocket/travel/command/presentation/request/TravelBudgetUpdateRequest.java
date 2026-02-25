package com.genesis.unipocket.travel.command.presentation.request;

import com.genesis.unipocket.global.exception.ErrorCode;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record TravelBudgetUpdateRequest(
		@NotNull(message = CODE) @DecimalMin(value = "0.00", inclusive = true, message = CODE) @Digits(integer = 17, fraction = 2, message = CODE) BigDecimal budget) {

	public static final String CODE = ErrorCode.CodeLiterals.TRAVEL_UPDATE_VALIDATION_FAILED;
}
