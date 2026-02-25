package com.genesis.unipocket.travel.query.presentation.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.genesis.unipocket.global.common.enums.CountryCode;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import com.genesis.unipocket.global.common.json.FixedScaleDecimalStringSerializer;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record TravelAmountResponse(
		CountryCode localCountryCode,
		CurrencyCode localCurrencyCode,
		CountryCode baseCountryCode,
		CurrencyCode baseCurrencyCode,
		@JsonSerialize(using = FixedScaleDecimalStringSerializer.class) BigDecimal totalLocalAmount,
		@JsonSerialize(using = FixedScaleDecimalStringSerializer.class) BigDecimal totalBaseAmount,
		@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
				OffsetDateTime oldestExpenseDate,
		@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
				OffsetDateTime newestExpenseDate) {}
