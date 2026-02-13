package com.genesis.unipocket.widget.query.presentation.request;

import com.genesis.unipocket.global.common.enums.WidgetType;
import com.genesis.unipocket.widget.common.enums.CurrencyType;
import com.genesis.unipocket.widget.common.enums.Period;

public record WidgetQueryRequest(WidgetType widgetType, CurrencyType currencyType, Period period) {}
