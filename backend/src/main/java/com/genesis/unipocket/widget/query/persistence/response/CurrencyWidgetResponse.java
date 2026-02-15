package com.genesis.unipocket.widget.query.persistence.response;

import com.genesis.unipocket.global.common.enums.CurrencyCode;
import java.util.List;

public record CurrencyWidgetResponse(int currencyCount, List<CurrencyItem> items) {

	public record CurrencyItem(CurrencyCode currencyCode, int percent) {}
}
