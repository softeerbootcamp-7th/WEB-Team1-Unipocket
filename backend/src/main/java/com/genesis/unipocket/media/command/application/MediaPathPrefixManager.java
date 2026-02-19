package com.genesis.unipocket.media.command.application;

import java.util.Arrays;
import java.util.stream.Collectors;
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
			return false;
		}
		return key != null && key.startsWith(prefix);
	}

	private String withGlobalPrefix(String prefix) {
		return joinPath(globalPrefix, prefix);
	}

	private String append(String prefix, String tail) {
		return joinPath(prefix, tail);
	}

	private String joinPath(String... parts) {
		return Arrays.stream(parts)
				.map(this::trimSlashes)
				.filter(part -> !part.isEmpty())
				.collect(Collectors.joining("/"));
	}

	private String ensureTrailingSlash(String value) {
		if (value == null || value.isBlank()) {
			return "";
		}
		return trimSlashes(value) + "/";
	}

	private String trimSlashes(String value) {
		if (value == null) {
			return "";
		}
		String trimmed = value.trim();
		int start = 0;
		int end = trimmed.length();
		while (start < end && trimmed.charAt(start) == '/') {
			start++;
		}
		while (end > start && trimmed.charAt(end - 1) == '/') {
			end--;
		}
		return trimmed.substring(start, end);
	}
}
