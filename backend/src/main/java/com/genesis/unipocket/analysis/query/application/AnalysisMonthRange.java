package com.genesis.unipocket.analysis.query.application;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

record AnalysisMonthRange(
		YearMonth yearMonth,
		LocalDate startLocalDate,
		LocalDate endLocalDateInclusive,
		LocalDateTime startUtc,
		LocalDateTime endUtcExclusive) {}
