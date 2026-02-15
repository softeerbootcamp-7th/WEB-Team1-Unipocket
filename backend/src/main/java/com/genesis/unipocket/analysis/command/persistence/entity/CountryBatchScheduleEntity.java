package com.genesis.unipocket.analysis.command.persistence.entity;

import com.genesis.unipocket.global.common.entity.BaseEntity;
import com.genesis.unipocket.global.common.enums.CountryCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Builder
@Table(name = "country_batch_schedule")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CountryBatchScheduleEntity extends BaseEntity {

	@Id
	@Column(nullable = false, name = "country_code", length = 8)
	@Enumerated(EnumType.STRING)
	private CountryCode countryCode;

	@Column(nullable = false, length = 64)
	private String timezone;

	@Column(name = "next_run_at_utc", nullable = false)
	private LocalDateTime nextRunAtUtc;

	@Column(name = "last_success_local_date")
	private LocalDate lastSuccessLocalDate;

	public static CountryBatchScheduleEntity create(
			CountryCode countryCode,
			ZoneId zoneId,
			int runHour,
			int runMinute,
			LocalDateTime nowUtc) {
		LocalDateTime nextRunUtc = computeNextRunUtc(zoneId, runHour, runMinute, nowUtc);
		return CountryBatchScheduleEntity.builder()
				.countryCode(countryCode)
				.timezone(zoneId.getId())
				.nextRunAtUtc(nextRunUtc)
				.build();
	}

	public void markSuccess(LocalDate runLocalDate) {
		this.lastSuccessLocalDate = runLocalDate;
	}

	public void moveNextRun(int runHour, int runMinute, LocalDateTime nowUtc) {
		ZoneId zoneId = ZoneId.of(timezone);
		this.nextRunAtUtc = computeNextRunUtc(zoneId, runHour, runMinute, nowUtc);
	}

	private static LocalDateTime computeNextRunUtc(
			ZoneId zoneId, int runHour, int runMinute, LocalDateTime nowUtc) {
		ZonedDateTime nowZoned = nowUtc.atZone(ZoneOffset.UTC).withZoneSameInstant(zoneId);
		ZonedDateTime candidate = nowZoned.toLocalDate().atTime(runHour, runMinute).atZone(zoneId);
		if (!candidate.isAfter(nowZoned)) {
			candidate = candidate.plusDays(1);
		}
		return candidate.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
	}
}
