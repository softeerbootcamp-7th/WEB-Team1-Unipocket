package com.genesis.unipocket.analysis.query.persistence.response;

import java.util.List;

public record CompareByCategoryRes(
		int maxDiffCategoryIndex, boolean isOverSpent, String maxLabel, List<CategoryItem> items) {

	public record CategoryItem(
			int categoryIndex, String mySpentAmount, String averageSpentAmount) {}
}
