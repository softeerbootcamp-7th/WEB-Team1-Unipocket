package com.genesis.unipocket.expense.common.port.dto;

import com.genesis.unipocket.global.common.enums.CountryCode;

/**
 * <b>가계부 정보 DTO</b>
 *
 * @author bluefishez
 * @since 2026-02-10
 */
public record AccountBookInfo(
		Long accountBookId,
		String userId,
		CountryCode baseCountryCode,
		CountryCode localCountryCode) {}
