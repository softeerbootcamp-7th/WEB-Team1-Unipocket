package com.genesis.unipocket.widget.query.presentation.request;

import com.genesis.unipocket.global.common.enums.WidgetType;
import com.genesis.unipocket.widget.common.enums.CurrencyType;
import com.genesis.unipocket.widget.common.enums.Period;
import jakarta.validation.constraints.NotNull;

public record WidgetQueryRequest(
		@NotNull(message = "widgetType은 필수입니다.") WidgetType widgetType,
		CurrencyType currencyType,
		Period period) {}
