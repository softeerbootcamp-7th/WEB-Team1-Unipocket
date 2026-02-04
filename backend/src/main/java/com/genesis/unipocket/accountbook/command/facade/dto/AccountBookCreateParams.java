package com.genesis.unipocket.accountbook.command.facade.dto;

import com.genesis.unipocket.global.common.enums.CountryCode;
import java.time.LocalDate;

public record AccountBookCreateParams(
		CountryCode localCountryCode, LocalDate startDate, LocalDate endDate) {}
