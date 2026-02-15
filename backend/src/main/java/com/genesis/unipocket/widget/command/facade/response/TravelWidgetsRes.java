package com.genesis.unipocket.widget.command.facade.response;

import com.genesis.unipocket.widget.command.application.result.UpdateTravelWidgetsResult;
import com.genesis.unipocket.widget.common.WidgetItem;
import java.util.List;

public record TravelWidgetsRes(List<WidgetItem> items) {

	public static TravelWidgetsRes from(UpdateTravelWidgetsResult result) {
		return new TravelWidgetsRes(result.items());
	}
}
