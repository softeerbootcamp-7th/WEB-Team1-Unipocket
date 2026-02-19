package com.genesis.unipocket.widget.command.persistence.converter;

import com.genesis.unipocket.widget.command.persistence.entity.TravelWidgetEntity;
import com.genesis.unipocket.widget.common.WidgetItem;
import com.genesis.unipocket.widget.common.enums.WidgetSizePolicy;
import java.util.List;

public final class TravelWidgetCommandConverter {

	private TravelWidgetCommandConverter() {}

	public static List<TravelWidgetEntity> toEntities(Long travelId, List<WidgetItem> items) {
		return items.stream()
				.map(
						item ->
								TravelWidgetEntity.builder()
										.travelId(travelId)
										.displayOrder(item.order())
										.widgetType(item.widgetType())
										.currencyType(item.currencyType())
										.period(item.period())
										.size(WidgetSizePolicy.getSize(item.widgetType()))
										.build())
				.toList();
	}

	public static List<WidgetItem> toWidgetItems(List<TravelWidgetEntity> entities) {
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
