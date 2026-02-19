package com.genesis.unipocket.widget.common.enums;

import static org.assertj.core.api.Assertions.assertThat;

import com.genesis.unipocket.global.common.enums.WidgetType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("WidgetSizePolicy 단위 테스트")
class WidgetSizePolicyTest {

	@Test
	@DisplayName("CATEGORY 위젯의 size는 2이다")
	void getSize_category_returnsTwo() {
		assertThat(WidgetSizePolicy.getSize(WidgetType.CATEGORY)).isEqualTo(2);
	}

	@Test
	@DisplayName("CATEGORY 외 위젯의 size는 1이다")
	void getSize_nonCategory_returnsOne() {
		assertThat(WidgetSizePolicy.getSize(WidgetType.BUDGET)).isEqualTo(1);
		assertThat(WidgetSizePolicy.getSize(WidgetType.PERIOD)).isEqualTo(1);
		assertThat(WidgetSizePolicy.getSize(WidgetType.COMPARISON)).isEqualTo(1);
		assertThat(WidgetSizePolicy.getSize(WidgetType.PAYMENT)).isEqualTo(1);
		assertThat(WidgetSizePolicy.getSize(WidgetType.CURRENCY)).isEqualTo(1);
	}
}
