package com.genesis.unipocket.widget.query.persistence.response;

import com.genesis.unipocket.global.common.enums.CountryCode;
import lombok.Builder;

@Builder
public record BudgetWidgetResponse(
		String budget,
		CountryCode baseCountryCode,
		CountryCode localCountryCode,
		String baseSpentAmount,
		String localSpentAmount) {}
