package com.genesis.unipocket.accountbook.query.persistence.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.genesis.unipocket.global.common.enums.CountryCode;
import com.genesis.unipocket.global.common.json.FixedScaleDecimalStringSerializer;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record AccountBookDetailResponse(
		Long id,
		String title,
		CountryCode localCountryCode,
		CountryCode baseCountryCode,
		@JsonSerialize(using = FixedScaleDecimalStringSerializer.class) BigDecimal budget,
		@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
				LocalDateTime budgetCreatedAt,
		List<?> tempExpenseBatchIds,
		@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") LocalDate startDate,
		@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") LocalDate endDate) {}
