package com.genesis.unipocket.tempexpense.command.facade.port.dto;

import com.genesis.unipocket.global.common.enums.CurrencyCode;

/**
 * <b>임시지출 환율 계산용 가계부 통화 정보 DTO</b>
 */
public record AccountBookRateInfo(CurrencyCode baseCurrencyCode, CurrencyCode localCurrencyCode) {}
