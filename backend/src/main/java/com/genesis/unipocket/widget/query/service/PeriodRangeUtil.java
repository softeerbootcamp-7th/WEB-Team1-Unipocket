package com.genesis.unipocket.widget.query.service;

import com.genesis.unipocket.widget.common.enums.Period;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;

public final class PeriodRangeUtil {

	private static final int MONTHLY_COUNT = 6;
	private static final int WEEKLY_COUNT = 5;
	private static final int DAILY_COUNT = 7;

	private PeriodRangeUtil() {}

	public record PeriodSlot(String label, LocalDateTime start, LocalDateTime end) {}

	public static LocalDateTime[] getCurrentPeriodRange(Period period, ZoneId zoneId) {
		if (period == null || period == Period.ALL) {
			return null;
		}

		ZonedDateTime now = ZonedDateTime.now(zoneId);

		return switch (period) {
			case MONTHLY -> {
				ZonedDateTime start =
						now.with(TemporalAdjusters.firstDayOfMonth())
								.toLocalDate()
								.atStartOfDay(zoneId);
				ZonedDateTime end = start.plusMonths(1);
				yield toUtcRange(start, end);
			}
			case WEEKLY -> {
				ZonedDateTime start =
						now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
								.toLocalDate()
								.atStartOfDay(zoneId);
				ZonedDateTime end = start.plusWeeks(1);
				yield toUtcRange(start, end);
			}
			case DAILY -> {
				ZonedDateTime start = now.toLocalDate().atStartOfDay(zoneId);
				ZonedDateTime end = start.plusDays(1);
				yield toUtcRange(start, end);
			}
			case ALL -> null;
		};
	}

	public static List<PeriodSlot> getRecentPeriodSlots(Period period, ZoneId zoneId) {
		ZonedDateTime now = ZonedDateTime.now(zoneId);

		return switch (period) {
			case MONTHLY -> buildMonthlySlots(now, zoneId);
			case WEEKLY -> buildWeeklySlots(now, zoneId);
			case DAILY -> buildDailySlots(now, zoneId);
			default -> List.of();
		};
	}

	private static List<PeriodSlot> buildMonthlySlots(ZonedDateTime now, ZoneId zoneId) {
		List<PeriodSlot> slots = new ArrayList<>(MONTHLY_COUNT);
		ZonedDateTime monthStart =
				now.with(TemporalAdjusters.firstDayOfMonth()).toLocalDate().atStartOfDay(zoneId);

		for (int i = MONTHLY_COUNT - 1; i >= 0; i--) {
			ZonedDateTime start = monthStart.minusMonths(i);
			ZonedDateTime end = start.plusMonths(1);
			String label = start.format(DateTimeFormatter.ofPattern("yyyy-MM"));
			LocalDateTime[] utc = toUtcRange(start, end);
			slots.add(new PeriodSlot(label, utc[0], utc[1]));
		}
		return slots;
	}

	private static List<PeriodSlot> buildWeeklySlots(ZonedDateTime now, ZoneId zoneId) {
		List<PeriodSlot> slots = new ArrayList<>(WEEKLY_COUNT);
		ZonedDateTime weekStart =
				now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
						.toLocalDate()
						.atStartOfDay(zoneId);

		WeekFields weekFields = WeekFields.ISO;

		for (int i = WEEKLY_COUNT - 1; i >= 0; i--) {
			ZonedDateTime start = weekStart.minusWeeks(i);
			ZonedDateTime end = start.plusWeeks(1);
			int year = start.get(weekFields.weekBasedYear());
			int week = start.get(weekFields.weekOfWeekBasedYear());
			String label = String.format("%d-W%02d", year, week);
			LocalDateTime[] utc = toUtcRange(start, end);
			slots.add(new PeriodSlot(label, utc[0], utc[1]));
		}
		return slots;
	}

	private static List<PeriodSlot> buildDailySlots(ZonedDateTime now, ZoneId zoneId) {
		List<PeriodSlot> slots = new ArrayList<>(DAILY_COUNT);
		ZonedDateTime dayStart = now.toLocalDate().atStartOfDay(zoneId);

		for (int i = DAILY_COUNT - 1; i >= 0; i--) {
			ZonedDateTime start = dayStart.minusDays(i);
			ZonedDateTime end = start.plusDays(1);
			String label = start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
			LocalDateTime[] utc = toUtcRange(start, end);
			slots.add(new PeriodSlot(label, utc[0], utc[1]));
		}
		return slots;
	}

	private static LocalDateTime[] toUtcRange(ZonedDateTime start, ZonedDateTime end) {
		return new LocalDateTime[] {
			start.withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime(),
			end.withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime()
		};
	}
}
