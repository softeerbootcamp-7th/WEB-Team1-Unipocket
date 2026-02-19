package com.genesis.unipocket.widget.command.application.result;

import com.genesis.unipocket.widget.common.WidgetItem;
import java.util.List;

public record UpdateTravelWidgetsResult(Long travelId, List<WidgetItem> items) {}
