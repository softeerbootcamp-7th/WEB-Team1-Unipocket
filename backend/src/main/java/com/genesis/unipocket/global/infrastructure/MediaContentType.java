package com.genesis.unipocket.global.infrastructure;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import lombok.Getter;

public enum MediaContentType {
	PNG(".png", "image/png"),
	JPEG(".jpeg", "image/jpeg"),
	JPG(".jpg", "image/jpeg"),
	CSV(".csv", "text/csv");

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
}
