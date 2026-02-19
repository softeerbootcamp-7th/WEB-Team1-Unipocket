package com.genesis.unipocket.widget.common.validator;

import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import com.genesis.unipocket.widget.common.WidgetItem;
import com.genesis.unipocket.widget.common.enums.CurrencyType;
import com.genesis.unipocket.widget.common.enums.Period;
import com.genesis.unipocket.widget.common.enums.WidgetSizePolicy;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class WidgetLayoutValidator {

	private static final int MAX_TOTAL_SIZE = 5;

	private WidgetLayoutValidator() {}

	public static List<WidgetItem> validateAndNormalize(List<WidgetItem> items) {
		List<WidgetItem> normalized =
				items.stream()
						.map(WidgetLayoutValidator::applyDefaults)
						.sorted(Comparator.comparingInt(WidgetItem::order))
						.toList();

		validateOrderUniqueness(normalized);
		validateTotalSize(normalized);

		return normalized;
	}

	private static WidgetItem applyDefaults(WidgetItem item) {
		CurrencyType currencyType =
				item.currencyType() != null ? item.currencyType() : CurrencyType.BASE;
		Period period = item.period() != null ? item.period() : Period.ALL;
		return new WidgetItem(item.order(), item.widgetType(), currencyType, period);
	}

	private static void validateOrderUniqueness(List<WidgetItem> items) {
		Set<Integer> seen = new HashSet<>();
		for (WidgetItem item : items) {
			if (!seen.add(item.order())) {
				throw new BusinessException(ErrorCode.WIDGET_ORDER_DUPLICATED);
			}
		}
	}

	private static void validateTotalSize(List<WidgetItem> items) {
		int totalSize =
				items.stream().mapToInt(item -> WidgetSizePolicy.getSize(item.widgetType())).sum();
		if (totalSize > MAX_TOTAL_SIZE) {
			throw new BusinessException(ErrorCode.WIDGET_SIZE_EXCEEDED);
		}
	}
}
