package com.genesis.unipocket.analysis.query.persistence.response;

import java.util.List;

public record CompareWithLastMonthRes(
		String diff,
		String thisMonth,
		int thisMonthCount,
		String lastMonth,
		int lastMonthCount,
		String thisMonthSpent,
		List<DailyItem> thisMonthItem,
		List<DailyItem> prevMonthItem) {

	public record DailyItem(String date, String cumulatedAmount) {}
}
