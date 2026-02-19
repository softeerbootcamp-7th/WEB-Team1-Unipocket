package com.genesis.unipocket.widget.command.persistence.converter;

import static org.assertj.core.api.Assertions.assertThat;

import com.genesis.unipocket.global.common.enums.WidgetType;
import com.genesis.unipocket.widget.command.persistence.entity.TravelWidgetEntity;
import com.genesis.unipocket.widget.common.WidgetItem;
import com.genesis.unipocket.widget.common.enums.CurrencyType;
import com.genesis.unipocket.widget.common.enums.Period;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("TravelWidgetCommandConverter 단위 테스트")
class TravelWidgetCommandConverterTest {

	@Test
	@DisplayName("WidgetItem 목록을 TravelWidgetEntity 목록으로 변환한다")
	void toEntities_mapsCorrectly() {
		// given
		Long travelId = 10L;
		List<WidgetItem> items =
				List.of(
						new WidgetItem(0, WidgetType.CURRENCY, CurrencyType.LOCAL, Period.DAILY),
						new WidgetItem(1, WidgetType.CATEGORY, CurrencyType.BASE, Period.ALL));

		// when
		List<TravelWidgetEntity> entities =
				TravelWidgetCommandConverter.toEntities(travelId, items);

		// then
		assertThat(entities).hasSize(2);

		TravelWidgetEntity first = entities.get(0);
		assertThat(first.getTravelId()).isEqualTo(travelId);
		assertThat(first.getDisplayOrder()).isEqualTo(0);
		assertThat(first.getWidgetType()).isEqualTo(WidgetType.CURRENCY);
		assertThat(first.getSize()).isEqualTo(1);

		TravelWidgetEntity second = entities.get(1);
		assertThat(second.getWidgetType()).isEqualTo(WidgetType.CATEGORY);
		assertThat(second.getSize()).isEqualTo(2);
	}

	@Test
	@DisplayName("TravelWidgetEntity 목록을 WidgetItem 목록으로 변환한다")
	void toWidgetItems_mapsCorrectly() {
		// given
		List<TravelWidgetEntity> entities =
				List.of(
						TravelWidgetEntity.builder()
								.travelId(10L)
								.displayOrder(0)
								.widgetType(WidgetType.CURRENCY)
								.currencyType(CurrencyType.LOCAL)
								.period(Period.DAILY)
								.size(1)
								.build());

		// when
		List<WidgetItem> items = TravelWidgetCommandConverter.toWidgetItems(entities);

		// then
		assertThat(items).hasSize(1);
		assertThat(items.get(0))
				.isEqualTo(
						new WidgetItem(0, WidgetType.CURRENCY, CurrencyType.LOCAL, Period.DAILY));
	}
}
