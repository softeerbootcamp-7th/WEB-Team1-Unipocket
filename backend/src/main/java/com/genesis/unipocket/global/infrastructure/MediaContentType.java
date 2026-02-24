package com.genesis.unipocket.global.infrastructure;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import lombok.Getter;

public enum MediaContentType {
	PNG(".png", "image/png"),
	JPEG(".jpeg", "image/jpeg"),
	JPG(".jpg", "image/jpeg"),
	CSV(".csv", "text/csv"),
	XLS(".xls", "application/vnd.ms-excel"),
	XLSX(".xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

	@Getter private final String ext;

	@Getter private final String mimeType;

	MediaContentType(String ext, String mimeType) {
		this.ext = ext;
		this.mimeType = mimeType;
	}

	public static Optional<MediaContentType> fromExtension(String extension) {
		if (extension == null || extension.isBlank()) {
			return Optional.empty();
		}

		String normalized =
				extension.startsWith(".")
						? extension.toLowerCase(Locale.ROOT)
						: ("." + extension).toLowerCase(Locale.ROOT);

		return Arrays.stream(values()).filter(type -> type.ext.equals(normalized)).findFirst();
	}

	public static Optional<MediaContentType> fromMimeType(String mimeType) {
		if (mimeType == null || mimeType.isBlank()) {
			return Optional.empty();
		}
		String normalized = mimeType.trim().toLowerCase(Locale.ROOT);
		int parameterDelimiter = normalized.indexOf(';');
		if (parameterDelimiter >= 0) {
			normalized = normalized.substring(0, parameterDelimiter).trim();
		}
		final String targetMimeType = normalized;
		return Arrays.stream(values())
				.filter(type -> type.mimeType.equals(targetMimeType))
				.findFirst();
	}
}
