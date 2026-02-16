package com.genesis.unipocket.tempexpense.command.application.parsing;

import com.genesis.unipocket.global.common.enums.Category;
import com.genesis.unipocket.global.common.enums.CurrencyCode;
import java.util.Locale;
import org.springframework.stereotype.Component;

@Component
public class TemporaryExpenseFieldParser {

	public Category parseCategory(String categoryStr) {
		if (categoryStr == null || categoryStr.isBlank()) {
			return Category.UNCLASSIFIED;
		}
		String trimmed = categoryStr.trim();
		if (trimmed.matches("\\d+")) {
			try {
				return Category.fromOrdinal(Integer.parseInt(trimmed));
			} catch (IllegalArgumentException e) {
				return Category.UNCLASSIFIED;
			}
		}

		String normalized = trimmed.replaceAll("[\\s_-]", "").toUpperCase(Locale.ROOT);
		switch (normalized) {
			case "TRANSPORTATION":
				return Category.TRANSPORT;
			case "ACCOMMODATION":
			case "HOUSING":
			case "RESIDENCE":
				return Category.RESIDENCE;
			case "ENTERTAINMENT":
				return Category.LEISURE;
			case "EDUCATION":
			case "SCHOOL":
				return Category.ACADEMIC;
			default:
				break;
		}

		try {
			return Category.valueOf(normalized);
		} catch (IllegalArgumentException e) {
			return Category.UNCLASSIFIED;
		}
	}

	public CurrencyCode parseCurrencyCode(String currencyStr, CurrencyCode defaultCurrency) {
		if (currencyStr == null || currencyStr.isBlank()) {
			return defaultCurrency;
		}
		try {
			String clean = currencyStr.replaceAll("[^A-Za-z]", "").toUpperCase();
			if (clean.isBlank()) {
				return defaultCurrency;
			}
			return CurrencyCode.valueOf(clean);
		} catch (IllegalArgumentException e) {
			return defaultCurrency;
		}
	}
}
