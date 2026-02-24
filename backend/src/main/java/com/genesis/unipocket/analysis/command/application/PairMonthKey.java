package com.genesis.unipocket.analysis.command.application;

import com.genesis.unipocket.global.common.enums.CountryCode;
import java.time.LocalDate;

public record PairMonthKey(
		CountryCode localCountryCode, CountryCode baseCountryCode, LocalDate targetYearMonth) {}
