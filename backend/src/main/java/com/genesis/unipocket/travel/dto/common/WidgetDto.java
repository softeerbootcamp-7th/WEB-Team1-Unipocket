package com.genesis.unipocket.travel.dto.common;

import com.genesis.unipocket.travel.persistence.entity.TravelWidget;
import com.genesis.unipocket.travel.persistence.entity.WidgetType;
import jakarta.validation.constraints.NotNull;

public record WidgetDto(@NotNull WidgetType type, @NotNull Integer order) {

	public static WidgetDto from(TravelWidget widget) {
		return new WidgetDto(widget.getWidgetType(), widget.getWidgetOrder());
	}
}
