package com.genesis.unipocket.analysis.query.application;

import com.genesis.unipocket.global.exception.BusinessException;
import com.genesis.unipocket.global.exception.ErrorCode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;

final class AnalysisMonthlySummaryDateSupport {

	private AnalysisMonthlySummaryDateSupport() {}

	static int parseYear(String yearText) {
		if (yearText == null || yearText.isBlank()) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
		}
		String normalized = yearText.trim();
		if (!normalized.matches("\\d{4}")) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
		}
		try {
			return Integer.parseInt(normalized);
		} catch (NumberFormatException e) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
		}
	}

	static int parseMonth(String monthText) {
		if (monthText == null || monthText.isBlank()) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
		}
		String normalized = monthText.trim();
		if (!normalized.matches("\\d{1,2}")) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
		}
		try {
			int month = Integer.parseInt(normalized);
			if (month < 1 || month > 12) {
				throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
			}
			return month;
		} catch (NumberFormatException e) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
		}
	}

	static void validateNotFutureYearMonth(int year, int month, ZoneId zoneId) {
		YearMonth requested = YearMonth.of(year, month);
		YearMonth current = YearMonth.from(LocalDate.now(zoneId));
		if (requested.isAfter(current)) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
		}
	}

	static AnalysisMonthRange buildMonthRange(
			int year, int month, ZoneId zoneId, boolean trimCurrentMonth) {
		YearMonth yearMonth = YearMonth.of(year, month);
		LocalDate startLocalDate = yearMonth.atDay(1);
		LocalDate endLocalDate = yearMonth.atEndOfMonth();
		LocalDate today = LocalDate.now(zoneId);
		if (trimCurrentMonth && year == today.getYear() && month == today.getMonthValue()) {
			endLocalDate = today;
		}

		LocalDateTime startUtc =
				startLocalDate
						.atStartOfDay(zoneId)
						.withZoneSameInstant(ZoneOffset.UTC)
						.toLocalDateTime();
		LocalDateTime endUtcExclusive =
				endLocalDate
						.plusDays(1)
						.atStartOfDay(zoneId)
						.withZoneSameInstant(ZoneOffset.UTC)
						.toLocalDateTime();

		return new AnalysisMonthRange(
				yearMonth, startLocalDate, endLocalDate, startUtc, endUtcExclusive);
	}
}
