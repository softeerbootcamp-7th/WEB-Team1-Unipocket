package com.genesis.unipocket.accountbook.query.persistence.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.genesis.unipocket.global.common.enums.CountryCode;
import com.genesis.unipocket.global.common.json.FixedScaleDecimalStringSerializer;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AccountBookExchangeRateResponse(
		CountryCode baseCountryCode,
		CountryCode localCountryCode,
		@JsonSerialize(using = FixedScaleDecimalStringSerializer.class) BigDecimal exchangeRate,
		@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
				LocalDateTime quotedAt) {}
