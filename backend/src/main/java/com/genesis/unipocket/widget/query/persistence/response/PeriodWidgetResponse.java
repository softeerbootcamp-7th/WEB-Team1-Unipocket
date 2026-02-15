package com.genesis.unipocket.widget.query.persistence.response;

import com.genesis.unipocket.global.common.enums.CountryCode;
import java.util.List;

public record PeriodWidgetResponse(CountryCode countryCode, int itemCount, List<PeriodItem> items) {

	public record PeriodItem(String period, String amount) {}
}
