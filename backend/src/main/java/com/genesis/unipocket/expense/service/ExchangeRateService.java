package com.genesis.unipocket.expense.service;

import com.genesis.unipocket.global.common.enums.CurrencyCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <b>환율 서비스 인터페이스</b>
 * <p>통화 간 환율 조회 및 금액 변환 기능 제공
 *
 * @author bluefishez
 * @since 2026-02-07
 */
public interface ExchangeRateService {

	/**
	 * 두 통화 간 환율 조회
	 *
	 * @param from 변환 원본 통화
	 * @param to 변환 대상 통화
	 * @param dateTime 환율 기준 시점
	 * @return 환율 (from 1단위당 to 통화 가치)
	 */
	BigDecimal getExchangeRate(CurrencyCode from, CurrencyCode to, LocalDateTime dateTime);

	/**
	 * 금액 환전
	 *
	 * @param amount 변환할 금액
	 * @param from 변환 원본 통화
	 * @param to 변환 대상 통화
	 * @param dateTime 환율 기준 시점
	 * @return 환전된 금액
	 */
	BigDecimal convertAmount(
			BigDecimal amount, CurrencyCode from, CurrencyCode to, LocalDateTime dateTime);
}
