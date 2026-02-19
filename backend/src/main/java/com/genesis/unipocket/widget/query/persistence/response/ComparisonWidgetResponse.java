package com.genesis.unipocket.widget.query.persistence.response;

import com.genesis.unipocket.global.common.enums.CountryCode;

public record ComparisonWidgetResponse(
		CountryCode countryCode,
		int month,
		String mySpentAmount,
		String averageSpentAmount,
		String spentAmountDiff) {}
