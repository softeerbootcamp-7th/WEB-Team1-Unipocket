package com.genesis.unipocket.widget.command.persistence.converter;

import static org.assertj.core.api.Assertions.assertThat;

import com.genesis.unipocket.global.common.enums.WidgetType;
import com.genesis.unipocket.widget.command.persistence.entity.AccountBookWidgetEntity;
import com.genesis.unipocket.widget.common.WidgetItem;
import com.genesis.unipocket.widget.common.enums.CurrencyType;
import com.genesis.unipocket.widget.common.enums.Period;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("AccountBookWidgetCommandConverter 단위 테스트")
class AccountBookWidgetCommandConverterTest {

	@Test
	@DisplayName("WidgetItem 목록을 AccountBookWidgetEntity 목록으로 변환한다")
	void toEntities_mapsCorrectly() {
		// given
		Long accountBookId = 1L;
		List<WidgetItem> items =
				List.of(
						new WidgetItem(0, WidgetType.BUDGET, CurrencyType.BASE, Period.ALL),
						new WidgetItem(1, WidgetType.CATEGORY, CurrencyType.LOCAL, Period.MONTHLY));

		// when
		List<AccountBookWidgetEntity> entities =
				AccountBookWidgetCommandConverter.toEntities(accountBookId, items);

		// then
		assertThat(entities).hasSize(2);

		AccountBookWidgetEntity first = entities.get(0);
		assertThat(first.getAccountBookId()).isEqualTo(accountBookId);
		assertThat(first.getDisplayOrder()).isEqualTo(0);
		assertThat(first.getWidgetType()).isEqualTo(WidgetType.BUDGET);
		assertThat(first.getCurrencyType()).isEqualTo(CurrencyType.BASE);
		assertThat(first.getPeriod()).isEqualTo(Period.ALL);
		assertThat(first.getSize()).isEqualTo(1);

		AccountBookWidgetEntity second = entities.get(1);
		assertThat(second.getDisplayOrder()).isEqualTo(1);
		assertThat(second.getWidgetType()).isEqualTo(WidgetType.CATEGORY);
		assertThat(second.getSize()).isEqualTo(2);
	}

	@Test
	@DisplayName("AccountBookWidgetEntity 목록을 WidgetItem 목록으로 변환한다")
	void toWidgetItems_mapsCorrectly() {
		// given
		List<AccountBookWidgetEntity> entities =
				List.of(
						AccountBookWidgetEntity.builder()
								.accountBookId(1L)
								.displayOrder(0)
								.widgetType(WidgetType.BUDGET)
								.currencyType(CurrencyType.BASE)
								.period(Period.ALL)
								.size(1)
								.build(),
						AccountBookWidgetEntity.builder()
								.accountBookId(1L)
								.displayOrder(1)
								.widgetType(WidgetType.CATEGORY)
								.currencyType(CurrencyType.LOCAL)
								.period(Period.MONTHLY)
								.size(2)
								.build());

		// when
		List<WidgetItem> items = AccountBookWidgetCommandConverter.toWidgetItems(entities);

		// then
		assertThat(items).hasSize(2);
		assertThat(items.get(0))
				.isEqualTo(new WidgetItem(0, WidgetType.BUDGET, CurrencyType.BASE, Period.ALL));
		assertThat(items.get(1))
				.isEqualTo(
						new WidgetItem(1, WidgetType.CATEGORY, CurrencyType.LOCAL, Period.MONTHLY));
	}
}
