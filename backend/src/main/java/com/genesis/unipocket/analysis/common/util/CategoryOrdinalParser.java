package com.genesis.unipocket.analysis.common.util;

import com.genesis.unipocket.global.common.enums.Category;

public final class CategoryOrdinalParser {

	private CategoryOrdinalParser() {}

	public static Integer parse(Object value) {
		if (value == null) {
			return null;
		}

		int ordinal;
		if (value instanceof Number number) {
			ordinal = number.intValue();
		} else {
			String raw = value.toString();
			try {
				ordinal = Integer.parseInt(raw);
			} catch (NumberFormatException ignored) {
				try {
					ordinal = Category.valueOf(raw).ordinal();
				} catch (IllegalArgumentException e) {
					return null;
				}
			}
		}

		return ordinal < 0 || ordinal >= Category.values().length ? null : ordinal;
	}
}
