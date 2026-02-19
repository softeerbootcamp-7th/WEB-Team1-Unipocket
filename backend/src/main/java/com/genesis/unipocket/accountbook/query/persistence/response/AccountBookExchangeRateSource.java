package com.genesis.unipocket.accountbook.query.persistence.response;

import com.genesis.unipocket.global.common.enums.CountryCode;
import java.time.LocalDateTime;

public record AccountBookExchangeRateSource(
		CountryCode localCountryCode, CountryCode baseCountryCode, LocalDateTime budgetCreatedAt) {}
