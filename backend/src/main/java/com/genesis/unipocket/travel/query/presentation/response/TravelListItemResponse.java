package com.genesis.unipocket.travel.query.presentation.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.genesis.unipocket.global.common.enums.CountryCode;
import com.genesis.unipocket.global.common.json.FixedScaleDecimalStringSerializer;
import java.math.BigDecimal;
import java.time.LocalDate;

public record TravelListItemResponse(
		Long travelId,
		Long accountBookId,
		String travelPlaceName,
		LocalDate startDate,
		LocalDate endDate,
		String imageKey,
		CountryCode localCountryCode,
		@JsonSerialize(using = FixedScaleDecimalStringSerializer.class)
				BigDecimal localCurrencyAmount,
		CountryCode baseCountryCode,
		@JsonSerialize(using = FixedScaleDecimalStringSerializer.class)
				BigDecimal baseCurrencyAmount) {}
