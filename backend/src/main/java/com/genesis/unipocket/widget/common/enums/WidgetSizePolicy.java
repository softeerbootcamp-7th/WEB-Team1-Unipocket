package com.genesis.unipocket.widget.common.enums;

import com.genesis.unipocket.global.common.enums.WidgetType;

public final class WidgetSizePolicy {

	private WidgetSizePolicy() {}

	public static int getSize(WidgetType widgetType) {
		return widgetType == WidgetType.CATEGORY ? 2 : 1;
	}
}
