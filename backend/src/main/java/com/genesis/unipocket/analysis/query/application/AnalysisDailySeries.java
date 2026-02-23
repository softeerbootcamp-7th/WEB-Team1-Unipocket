package com.genesis.unipocket.analysis.query.application;

import java.math.BigDecimal;
import java.util.List;

record AnalysisDailySeries(List<AnalysisDailyRow> items, BigDecimal total) {
	BigDecimal cumulativeAtSameDay(int daysElapsed) {
		if (items.isEmpty() || daysElapsed <= 0) {
			return BigDecimal.ZERO;
		}
		int targetIndex = Math.min(daysElapsed, items.size()) - 1;
		return items.get(targetIndex).cumulativeSpend();
	}
}
