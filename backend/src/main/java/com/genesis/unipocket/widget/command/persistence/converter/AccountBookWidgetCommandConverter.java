package com.genesis.unipocket.widget.command.persistence.converter;

import com.genesis.unipocket.widget.command.persistence.entity.AccountBookWidgetEntity;
import com.genesis.unipocket.widget.common.WidgetItem;
import com.genesis.unipocket.widget.common.enums.WidgetSizePolicy;
import java.util.List;

public final class AccountBookWidgetCommandConverter {

	private AccountBookWidgetCommandConverter() {}

	public static List<AccountBookWidgetEntity> toEntities(
			Long accountBookId, List<WidgetItem> items) {
		return items.stream()
				.map(
						item ->
								AccountBookWidgetEntity.builder()
										.accountBookId(accountBookId)
										.displayOrder(item.order())
										.widgetType(item.widgetType())
										.currencyType(item.currencyType())
										.period(item.period())
										.size(WidgetSizePolicy.getSize(item.widgetType()))
										.build())
				.toList();
	}

	public static List<WidgetItem> toWidgetItems(List<AccountBookWidgetEntity> entities) {
		return entities.stream()
				.map(
						entity ->
								new WidgetItem(
										entity.getDisplayOrder(),
										entity.getWidgetType(),
										entity.getCurrencyType(),
										entity.getPeriod()))
				.toList();
	}
}
