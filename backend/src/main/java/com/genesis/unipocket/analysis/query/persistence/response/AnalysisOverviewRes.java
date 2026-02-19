package com.genesis.unipocket.analysis.query.persistence.response;

import java.util.List;

public record AnalysisOverviewRes(
		String countryCode,
		CompareWithAverage compareWithAverage,
		CompareWithLastMonth compareWithLastMonth,
		CompareByCategory compareByCategory) {

	public record CompareWithAverage(
			int month, String mySpentAmount, String averageSpentAmount, String spentAmountDiff) {}

	public record CompareWithLastMonth(
			String diff,
			String thisMonth,
			int thisMonthCount,
			String lastMonth,
			int lastMonthCount,
			TotalSpent totalSpent,
			String thisMonthSpent,
			List<DailySpentItem> thisMonthItem,
			List<DailySpentItem> prevMonthItem) {

		public record TotalSpent(String thisMonthToDate, String lastMonthTotal) {}
	}

	public record DailySpentItem(String date, String cumulatedAmount) {}

	public record CompareByCategory(
			int maxDiffCategoryIndex,
			boolean isOverSpent,
			String maxLabel,
			List<CategoryItem> items) {

		public record CategoryItem(
				int categoryIndex, String mySpentAmount, String averageSpentAmount) {}
	}
}
