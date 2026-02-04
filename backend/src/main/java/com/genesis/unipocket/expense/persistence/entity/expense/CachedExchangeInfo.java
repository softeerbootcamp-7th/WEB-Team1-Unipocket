package com.genesis.unipocket.expense.persistence.entity.expense;

import com.genesis.unipocket.global.common.enums.CurrencyCode;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * <b>노출용 기준화폐 & 환율 정보</b>
 * <p>유저 노출용 정보
 * <p>기준 화폐 변경에 따라 달라지는 정보들
 *
 *
 * @author codingbaraGo
 * @since 2026-02-03
 */
@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class CachedExchangeInfo {

	private CurrencyCode standardCurrencyCode;

	private BigDecimal standardCurrencyAmount;

	private BigDecimal cachedExchangeRate;
}
