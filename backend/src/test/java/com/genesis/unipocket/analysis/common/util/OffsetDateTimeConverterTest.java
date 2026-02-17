package com.genesis.unipocket.analysis.common.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import org.junit.jupiter.api.Test;

class OffsetDateTimeConverterTest {

	@Test
	void from_nullValue_returnsNull() {
		assertThat(OffsetDateTimeConverter.from(null)).isNull();
	}

	@Test
	void from_localDateTime_returnsUtcOffsetDateTime() {
		LocalDateTime value = LocalDateTime.of(2026, 2, 17, 13, 5, 0);

		OffsetDateTime result = OffsetDateTimeConverter.from(value);

		assertThat(result).isEqualTo(value.atOffset(ZoneOffset.UTC));
	}

	@Test
	void from_timestamp_returnsUtcOffsetDateTime() {
		Timestamp value = Timestamp.valueOf(LocalDateTime.of(2026, 2, 17, 13, 5, 0));

		OffsetDateTime result = OffsetDateTimeConverter.from(value);

		assertThat(result).isEqualTo(value.toLocalDateTime().atOffset(ZoneOffset.UTC));
	}

	@Test
	void from_date_returnsUtcOffsetDateTime() {
		Date value = Date.from(OffsetDateTime.parse("2026-02-17T13:05:00Z").toInstant());

		OffsetDateTime result = OffsetDateTimeConverter.from(value);

		assertThat(result).isEqualTo(OffsetDateTime.parse("2026-02-17T13:05:00Z"));
	}

	@Test
	void from_stringValue_returnsParsedOffsetDateTime() {
		OffsetDateTime result = OffsetDateTimeConverter.from("2026-02-17T13:05:00+09:00");

		assertThat(result).isEqualTo(OffsetDateTime.parse("2026-02-17T13:05:00+09:00"));
	}

	@Test
	void from_unsupportedType_throwsIllegalStateException() {
		assertThatThrownBy(() -> OffsetDateTimeConverter.from(12345L))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("Unsupported type for date-time conversion");
	}
}
