package com.genesis.unipocket.widget.command.presentation.request;

import com.genesis.unipocket.global.common.enums.WidgetType;
import com.genesis.unipocket.widget.common.WidgetItem;
import com.genesis.unipocket.widget.common.enums.CurrencyType;
import com.genesis.unipocket.widget.common.enums.Period;
import jakarta.validation.constraints.NotNull;

public record WidgetItemRequest(
		@NotNull Integer order,
		@NotNull WidgetType widgetType,
		CurrencyType currencyType,
		Period period) {

	public WidgetItem toWidgetItem() {
		return new WidgetItem(order, widgetType, currencyType, period);
	}
}
