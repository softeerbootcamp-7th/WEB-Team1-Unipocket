package com.genesis.unipocket.analysis.query.persistence.response;

import com.genesis.unipocket.analysis.common.enums.CurrencyType;
import java.util.List;

public record MonthlySpendSummaryRes(
		Long accountBookId,
		int year,
		String month,
		CurrencyType currencyView,
		MonthSection thisMonth,
		PrevMonthSection prevMonth,
		Comparison comparison) {

	public record MonthSection(
			String startDate, String endDate, List<DailyItem> daily, String total) {}

	public record PrevMonthSection(
			int year,
			String month,
			String startDate,
			String endDate,
			List<DailyItem> daily,
			String total) {}

	public record DailyItem(String date, String dailySpend, String cumulativeSpend) {}

	public record Comparison(String avgTotal, String diff, boolean peerAvailable) {}
}
