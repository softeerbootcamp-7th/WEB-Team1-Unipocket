package com.genesis.unipocket.widget.query.service;

import com.genesis.unipocket.global.common.enums.CountryCode;
import com.genesis.unipocket.widget.common.enums.CurrencyType;
import com.genesis.unipocket.widget.common.enums.Period;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * <b>Widget 조회시 위젯별 조회에 필요한 context를 담는 헬퍼 객체</b>
 */
public record WidgetQueryContext(
		Long accountBookId,
		Long travelId,
		CurrencyType currencyType,
		Period period,
		ZoneId zoneId,
		LocalDateTime periodStart,
		LocalDateTime periodEnd,
		CountryCode baseCountryCode,
		CountryCode localCountryCode) {}
