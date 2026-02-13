package com.genesis.unipocket.widget.command.facade.response;

import com.genesis.unipocket.widget.command.application.result.UpdateAccountBookWidgetsResult;
import com.genesis.unipocket.widget.common.WidgetItem;
import java.util.List;

public record AccountBookWidgetsRes(List<WidgetItem> items) {

	public static AccountBookWidgetsRes from(UpdateAccountBookWidgetsResult result) {
		return new AccountBookWidgetsRes(result.items());
	}
}
