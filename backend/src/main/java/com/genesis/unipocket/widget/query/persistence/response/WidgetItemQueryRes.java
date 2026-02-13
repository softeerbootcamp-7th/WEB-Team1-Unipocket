package com.genesis.unipocket.widget.query.persistence.response;

import com.genesis.unipocket.global.common.enums.WidgetType;
import com.genesis.unipocket.widget.common.enums.CurrencyType;
import com.genesis.unipocket.widget.common.enums.Period;

public record WidgetItemQueryRes(
		int order, WidgetType widgetType, CurrencyType currencyType, Period period) {}
