package com.genesis.unipocket.widget.query.persistence.response;

import com.genesis.unipocket.global.common.enums.CountryCode;
import java.util.List;

public record CategoryWidgetResponse(
		String totalAmount, CountryCode countryCode, List<CategoryItem> items) {

	public record CategoryItem(String categoryName, String amount, int percent) {}
}
