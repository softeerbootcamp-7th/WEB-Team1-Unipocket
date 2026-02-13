package com.genesis.unipocket.widget.common;

import com.genesis.unipocket.global.common.enums.WidgetType;
import com.genesis.unipocket.widget.common.enums.CurrencyType;
import com.genesis.unipocket.widget.common.enums.Period;

public record WidgetItem(
		int order, WidgetType widgetType, CurrencyType currencyType, Period period) {}
