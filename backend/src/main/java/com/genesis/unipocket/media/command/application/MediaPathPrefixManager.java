package com.genesis.unipocket.media.command.application;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MediaPathPrefixManager {

	@Value("${app.media.global-prefix:backend}")
	private String globalPrefix;

	@Value("${app.media.temp-expense-prefix:temp-expenses}")
	private String tempExpensePrefix;

	@Value("${app.media.travel-image-prefix:travels}")
	private String travelImagePrefix;

	public String getTempExpensePrefix(Long accountBookId) {
		return append(withGlobalPrefix(tempExpensePrefix), String.valueOf(accountBookId));
	}

	public String getTravelImagePrefix() {
		return withGlobalPrefix(travelImagePrefix);
	}

	public boolean isTravelImageKey(String key) {
		String prefix = ensureTrailingSlash(getTravelImagePrefix());
		return key != null && key.startsWith(prefix);
	}

	public boolean isManagedKey(String key) {
		String prefix = ensureTrailingSlash(trimSlashes(globalPrefix));
		if (prefix.isEmpty()) {
			return true;
		}
		return key != null && key.startsWith(prefix);
	}

	private String withGlobalPrefix(String prefix) {
		String base = trimSlashes(globalPrefix);
		String sub = trimSlashes(prefix);
		if (base.isEmpty()) {
			return sub;
		}
		if (sub.isEmpty()) {
			return base;
		}
		return base + "/" + sub;
	}

	private String append(String prefix, String tail) {
		String normalizedPrefix = trimSlashes(prefix);
		String normalizedTail = trimSlashes(tail);
		if (normalizedPrefix.isEmpty()) {
			return normalizedTail;
		}
		if (normalizedTail.isEmpty()) {
			return normalizedPrefix;
		}
		return normalizedPrefix + "/" + normalizedTail;
	}

	private String ensureTrailingSlash(String value) {
		if (value == null || value.isBlank()) {
			return "";
		}
		return trimSlashes(value) + "/";
	}

	private String trimSlashes(String value) {
		String trimmed = value == null ? "" : value.trim();
		while (trimmed.startsWith("/")) {
			trimmed = trimmed.substring(1);
		}
		while (trimmed.endsWith("/")) {
			trimmed = trimmed.substring(0, trimmed.length() - 1);
		}
		return trimmed;
	}
}
