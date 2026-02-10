package com.genesis.unipocket.expense.common.dto;

import com.genesis.unipocket.global.common.enums.CountryCode;

/**
 * <b>가계부 정보 포트 DTO</b>
 * <p>타 도메인 의존 최소화용
 *
 * @author 김동균
 * @since 2026-02-10
 */
public record AccountBookInfo(CountryCode baseCountryCode) {}
