package com.genesis.unipocket.analysis.common.util;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public final class OffsetDateTimeConverter {

	private OffsetDateTimeConverter() {}

	public static OffsetDateTime from(Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof OffsetDateTime offsetDateTime) {
			return offsetDateTime;
		}
		if (value instanceof LocalDateTime localDateTime) {
			return localDateTime.atOffset(ZoneOffset.UTC);
		}
		if (value instanceof java.sql.Timestamp timestamp) {
			return timestamp.toLocalDateTime().atOffset(ZoneOffset.UTC);
		}
		if (value instanceof java.util.Date date) {
			return date.toInstant().atOffset(ZoneOffset.UTC);
		}
		if (value instanceof String stringValue) {
			return OffsetDateTime.parse(stringValue);
		}
		throw new IllegalStateException(
				"Unsupported type for date-time conversion: " + value.getClass().getName());
	}
}
