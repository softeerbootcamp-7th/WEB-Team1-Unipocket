package com.genesis.unipocket.widget.command.facade.provide;

import com.genesis.unipocket.accountbook.command.facade.port.AccountBookDefaultWidgetPort;
import com.genesis.unipocket.global.common.enums.WidgetType;
import com.genesis.unipocket.widget.command.application.WidgetCommandService;
import com.genesis.unipocket.widget.command.application.command.UpdateAccountBookWidgetsCommand;
import com.genesis.unipocket.widget.common.WidgetItem;
import com.genesis.unipocket.widget.common.enums.CurrencyType;
import com.genesis.unipocket.widget.common.enums.Period;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountBookDefaultWidgetSetter implements AccountBookDefaultWidgetPort {

	private final WidgetCommandService widgetCommandService;

	@Override
	public void setDefaultWidget(Long accountBookId) {
		List<WidgetItem> items =
				List.of(
						new WidgetItem(0, WidgetType.BUDGET, CurrencyType.BASE, Period.MONTHLY),
						new WidgetItem(1, WidgetType.PERIOD, CurrencyType.BASE, Period.MONTHLY),
						new WidgetItem(2, WidgetType.CATEGORY, CurrencyType.BASE, Period.MONTHLY));

		widgetCommandService.updateAccountBookWidgets(
				new UpdateAccountBookWidgetsCommand(accountBookId, items));
	}
}
