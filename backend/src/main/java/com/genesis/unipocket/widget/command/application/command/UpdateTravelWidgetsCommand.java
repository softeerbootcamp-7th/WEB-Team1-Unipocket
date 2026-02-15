package com.genesis.unipocket.widget.command.application.command;

import com.genesis.unipocket.widget.common.WidgetItem;
import java.util.List;

public record UpdateTravelWidgetsCommand(Long travelId, List<WidgetItem> items) {}
