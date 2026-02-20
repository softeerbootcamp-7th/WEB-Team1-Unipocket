package com.genesis.unipocket.tempexpense.command.facade.port.dto;

import com.genesis.unipocket.global.common.enums.CountryCode;

public record AccountBookInfo(
		Long accountBookId,
		String userId,
		CountryCode baseCountryCode,
		CountryCode localCountryCode) {}
