package com.genesis.unipocket.analysis.command.persistence.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.genesis.unipocket.global.common.enums.CountryCode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CountryBatchScheduleEntityTest {

	@Test
	@DisplayName("스케줄 생성 시 로컬 03:00 실행 시간이 nextRunAtUtc에 반영된다")
	void create_setsNextRunAtLocalThreeAm() {
		ZoneId zoneId = ZoneId.of("Asia/Seoul");
		LocalDateTime nowUtc = LocalDateTime.of(2026, 1, 10, 17, 0, 0);

		CountryBatchScheduleEntity schedule =
				CountryBatchScheduleEntity.create(CountryCode.KR, zoneId, 3, 0, nowUtc);

		ZonedDateTime nextLocal =
				schedule.getNextRunAtUtc().atZone(ZoneOffset.UTC).withZoneSameInstant(zoneId);
		assertThat(nextLocal.getHour()).isEqualTo(3);
		assertThat(nextLocal.getMinute()).isEqualTo(0);
	}

	@Test
	@DisplayName("moveNextRun은 다음 날 로컬 03:00으로 이동한다")
	void moveNextRun_movesToNextDayThreeAm() {
		ZoneId zoneId = ZoneId.of("America/New_York");
		LocalDateTime nowUtc = LocalDateTime.of(2026, 1, 10, 9, 0, 0);
		CountryBatchScheduleEntity schedule =
				CountryBatchScheduleEntity.create(CountryCode.US, zoneId, 3, 0, nowUtc);
		LocalDateTime before = schedule.getNextRunAtUtc();

		schedule.moveNextRun(3, 0);

		ZonedDateTime beforeLocal = before.atZone(ZoneOffset.UTC).withZoneSameInstant(zoneId);
		ZonedDateTime afterLocal =
				schedule.getNextRunAtUtc().atZone(ZoneOffset.UTC).withZoneSameInstant(zoneId);
		assertThat(afterLocal.toLocalDate()).isEqualTo(beforeLocal.toLocalDate().plusDays(1));
		assertThat(afterLocal.getHour()).isEqualTo(3);
		assertThat(afterLocal.getMinute()).isEqualTo(0);
	}
}
