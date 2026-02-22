package com.genesis.unipocket.travel.query.presentation.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.genesis.unipocket.global.common.enums.CountryCode;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.global.common.json.FixedScaleDecimalStringSerializer;
import java.math.BigDecimal;

public record TravelAmountResponse(
		CountryCode localCountryCode,
		CurrencyCode localCurrencyCode,
		CountryCode baseCountryCode,
		CurrencyCode baseCurrencyCode,
		@JsonSerialize(using = FixedScaleDecimalStringSerializer.class) BigDecimal totalLocalAmount,
		@JsonSerialize(using = FixedScaleDecimalStringSerializer.class)
				BigDecimal totalBaseAmount) {}
