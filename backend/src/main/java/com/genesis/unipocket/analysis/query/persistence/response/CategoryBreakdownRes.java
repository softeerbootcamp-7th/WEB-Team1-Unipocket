package com.genesis.unipocket.analysis.query.persistence.response;

import com.genesis.unipocket.analysis.common.enums.CurrencyType;
import com.genesis.unipocket.global.common.enums.Category;
import java.util.List;

public record CategoryBreakdownRes(
		Long accountBookId,
		String year,
		String month,
		CurrencyType currencyType,
		List<CategoryItem> categories,
		String myTotal,
		String avgTotal,
		boolean peerAvailable) {

	public record CategoryItem(Category category, String mySpend, String avgSpend, String diff) {}
}
