package com.genesis.unipocket.analysis.query.persistence.response;

import com.genesis.unipocket.analysis.command.persistence.entity.AnalysisQualityType;
import com.genesis.unipocket.global.common.enums.CountryCode;
import java.util.List;

public record CountryMonthlyAggregateRes(
		CountryCode countryCode,
		int year,
		int month,
		AnalysisQualityType qualityType,
		String totalAmount,
		long expenseCount,
		List<DailyItem> dailyItems,
		List<CategoryItem> categoryItems) {

	public record DailyItem(String date, String totalAmount, long expenseCount) {}

	public record CategoryItem(int categoryIndex, String totalAmount, long expenseCount) {}
}
