package com.genesis.unipocket.travel.dto;

import com.genesis.unipocket.travel.domain.TravelWidget;
import com.genesis.unipocket.travel.domain.WidgetType;
import jakarta.validation.constraints.NotNull;

public record WidgetDto(@NotNull WidgetType type, @NotNull Integer order) {

	public static WidgetDto from(TravelWidget widget) {
		return new WidgetDto(widget.getWidgetType(), widget.getWidgetOrder());
	}
}
