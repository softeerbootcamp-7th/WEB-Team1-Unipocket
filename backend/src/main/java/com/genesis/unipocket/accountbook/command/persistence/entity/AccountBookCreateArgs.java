package com.genesis.unipocket.accountbook.command.persistence.entity;

import com.genesis.unipocket.global.common.enums.CountryCode;
import com.genesis.unipocket.user.command.persistence.entity.UserEntity;
import java.time.LocalDate;
import java.math.BigDecimal;

public record AccountBookCreateArgs(
		UserEntity user,
		String title,
		CountryCode localCountryCode,
		CountryCode baseCountryCode,
		Integer bucketOrder,
		BigDecimal budget,
		LocalDate startDate,
		LocalDate endDate) {}
