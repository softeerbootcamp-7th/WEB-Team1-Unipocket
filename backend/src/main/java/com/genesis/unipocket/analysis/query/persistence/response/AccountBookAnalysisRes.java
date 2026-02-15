package com.genesis.unipocket.analysis.query.persistence.response;

import com.genesis.unipocket.global.common.enums.CountryCode;

public record AccountBookAnalysisRes(
		CountryCode countryCode,
		CompareWithAverageRes compareWithAverage,
		CompareWithLastMonthRes compareWithLastMonth,
		CompareByCategoryRes compareByCategory) {}
