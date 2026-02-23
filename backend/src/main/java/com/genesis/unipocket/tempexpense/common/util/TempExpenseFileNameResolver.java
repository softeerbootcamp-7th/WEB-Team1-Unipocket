package com.genesis.unipocket.tempexpense.common.util;

import java.util.Locale;

public final class TempExpenseFileNameResolver {

	private static final String UNKNOWN_FILE_PREFIX = "unknown_file";

	private TempExpenseFileNameResolver() {}

	public static String normalize(String fileName) {
		if (fileName == null) {
			return null;
		}
		String trimmed = fileName.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

	public static String resolveOrFallback(String fileName, String s3Key) {
		String normalized = normalize(fileName);
		if (normalized != null) {
			return normalized;
		}
		String extension = extractExtension(s3Key);
		if (extension.isEmpty()) {
			return UNKNOWN_FILE_PREFIX;
		}
		return UNKNOWN_FILE_PREFIX + "." + extension;
	}

	private static String extractExtension(String s3Key) {
		if (s3Key == null || s3Key.isBlank()) {
			return "";
		}

		String withoutQuery = s3Key.contains("?") ? s3Key.substring(0, s3Key.indexOf('?')) : s3Key;
		String fileSegment =
				withoutQuery.contains("/")
						? withoutQuery.substring(withoutQuery.lastIndexOf('/') + 1)
						: withoutQuery;
		int dotIndex = fileSegment.lastIndexOf('.');
		if (dotIndex < 0 || dotIndex == fileSegment.length() - 1) {
			return "";
		}
		return fileSegment.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
	}
}
