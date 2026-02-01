package com.genesis.unipocket.global.common.enums;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;

/**
 * <b>국가 코드 Enum</b>
 * <p>
 * 네이버에서 지원되는 화폐를 사용하는 모든 국가들의 국가 코드를 가지고 있습니다.
 * <br>
 * 해당 국가에서 사용하는 화폐 코드 정보도 담고 있습니다.
 * </p>
 * @author bluefishez
 * @since 2026-01-30
 */
@Getter
public enum CountryCode {
	KR(CurrencyCode.KRW),
	ZA(CurrencyCode.ZAR),
	NP(CurrencyCode.NPR),
	NO(CurrencyCode.NOK),
	NZ(CurrencyCode.NZD),
	TW(CurrencyCode.TWD),
	DK(CurrencyCode.DKK),
	RU(CurrencyCode.RUB),
	MO(CurrencyCode.MOP),
	MY(CurrencyCode.MYR),
	MX(CurrencyCode.MXN),
	MN(CurrencyCode.MNT),
	US(CurrencyCode.USD),
	BH(CurrencyCode.BHD),
	BD(CurrencyCode.BDT),
	VN(CurrencyCode.VND),
	BR(CurrencyCode.BRL),
	BN(CurrencyCode.BND),
	SA(CurrencyCode.SAR),
	SE(CurrencyCode.SEK),
	CH(CurrencyCode.CHF),
	SG(CurrencyCode.SGD),
	AE(CurrencyCode.AED),
	GB(CurrencyCode.GBP),
	OM(CurrencyCode.OMR),
	JO(CurrencyCode.JOD),
	DE(CurrencyCode.EUR),
	FR(CurrencyCode.EUR),
	IT(CurrencyCode.EUR),
	ES(CurrencyCode.EUR),
	NL(CurrencyCode.EUR),
	BE(CurrencyCode.EUR),
	AT(CurrencyCode.EUR),
	IE(CurrencyCode.EUR),
	PT(CurrencyCode.EUR),
	FI(CurrencyCode.EUR),
	GR(CurrencyCode.EUR),
	LU(CurrencyCode.EUR),
	IL(CurrencyCode.ILS),
	EG(CurrencyCode.EGP),
	IN(CurrencyCode.INR),
	ID(CurrencyCode.IDR),
	JP(CurrencyCode.JPY),
	CN(CurrencyCode.CNY),
	CZ(CurrencyCode.CZK),
	CL(CurrencyCode.CLP),
	KZ(CurrencyCode.KZT),
	QA(CurrencyCode.QAR),
	CA(CurrencyCode.CAD),
	KW(CurrencyCode.KWD),
	TH(CurrencyCode.THB),
	TR(CurrencyCode.TRY),
	PK(CurrencyCode.PKR),
	PL(CurrencyCode.PLN),
	PH(CurrencyCode.PHP),
	HU(CurrencyCode.HUF),
	AU(CurrencyCode.AUD),
	HK(CurrencyCode.HKD);

	private final CurrencyCode currencyCode;

	CountryCode(CurrencyCode currencyCode) {
		this.currencyCode = currencyCode;
	}

	private static final Map<String, CountryCode> BY_NAME =
			Stream.of(values()).collect(Collectors.toMap(Enum::name, Function.identity()));

	public static Optional<CountryCode> fromCode(String code) {
		return Optional.ofNullable(code).map(String::toUpperCase).map(BY_NAME::get);
	}
}
