package com.genesis.unipocket.widget.command.facade;

import com.genesis.unipocket.widget.command.application.WidgetCommandService;
import com.genesis.unipocket.widget.command.application.command.UpdateAccountBookWidgetsCommand;
import com.genesis.unipocket.widget.command.application.command.UpdateTravelWidgetsCommand;
import com.genesis.unipocket.widget.command.application.result.UpdateAccountBookWidgetsResult;
import com.genesis.unipocket.widget.command.application.result.UpdateTravelWidgetsResult;
import com.genesis.unipocket.widget.command.facade.response.AccountBookWidgetsRes;
import com.genesis.unipocket.widget.command.facade.response.TravelWidgetsRes;
import com.genesis.unipocket.widget.command.presentation.request.WidgetItemRequest;
import com.genesis.unipocket.widget.common.WidgetItem;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WidgetCommandOrchestrator {

	private final WidgetCommandService widgetCommandService;

	public AccountBookWidgetsRes updateAccountBookWidgets(
			Long accountBookId, List<WidgetItemRequest> requests) {
		List<WidgetItem> items = requests.stream().map(WidgetItemRequest::toWidgetItem).toList();

		UpdateAccountBookWidgetsCommand command =
				new UpdateAccountBookWidgetsCommand(accountBookId, items);

		UpdateAccountBookWidgetsResult result =
				widgetCommandService.updateAccountBookWidgets(command);

		return AccountBookWidgetsRes.from(result);
	}

	public TravelWidgetsRes updateTravelWidgets(Long travelId, List<WidgetItemRequest> requests) {
		List<WidgetItem> items = requests.stream().map(WidgetItemRequest::toWidgetItem).toList();

		UpdateTravelWidgetsCommand command = new UpdateTravelWidgetsCommand(travelId, items);

		UpdateTravelWidgetsResult result = widgetCommandService.updateTravelWidgets(command);

		return TravelWidgetsRes.from(result);
	}
}
