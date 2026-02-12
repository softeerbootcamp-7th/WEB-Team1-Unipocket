package com.genesis.unipocket.travel.command.application.command;

import com.genesis.unipocket.global.common.enums.WidgetType;
import com.genesis.unipocket.travel.command.presentation.request.WidgetRequest;
import java.util.List;

public record UpdateWidgetsCommand(Long travelId, List<Widget> widgets) {

	public record Widget(WidgetType type, Integer order) {}

	public static UpdateWidgetsCommand of(Long travelId, List<WidgetRequest> requests) {
		List<Widget> widgets = requests.stream().map(r -> new Widget(r.type(), r.order())).toList();
		return new UpdateWidgetsCommand(travelId, widgets);
	}
}
