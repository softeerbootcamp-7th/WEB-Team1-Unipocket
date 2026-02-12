package com.genesis.unipocket.widget.query.persistence.response;

import com.genesis.unipocket.global.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CountryCode;
import java.math.BigDecimal;
import java.util.List;

public record CategoryWidgetResponse(
		BigDecimal totalAmount, CountryCode countryCode, List<CategoryItem> items) {

	public record CategoryItem(Category categoryName, BigDecimal amount, int percent) {}
}
