package com.genesis.unipocket.global.util;

import com.genesis.unipocket.global.common.enums.CountryCode;
import java.time.ZoneId;
import java.util.EnumMap;
import java.util.Map;

public final class CountryCodeTimezoneMapper {

	private static final Map<CountryCode, ZoneId> ZONE_MAP = new EnumMap<>(CountryCode.class);

	static {
		ZONE_MAP.put(CountryCode.KR, ZoneId.of("Asia/Seoul"));
		ZONE_MAP.put(CountryCode.ZA, ZoneId.of("Africa/Johannesburg"));
		ZONE_MAP.put(CountryCode.NP, ZoneId.of("Asia/Kathmandu"));
		ZONE_MAP.put(CountryCode.NO, ZoneId.of("Europe/Oslo"));
		ZONE_MAP.put(CountryCode.NZ, ZoneId.of("Pacific/Auckland"));
		ZONE_MAP.put(CountryCode.TW, ZoneId.of("Asia/Taipei"));
		ZONE_MAP.put(CountryCode.DK, ZoneId.of("Europe/Copenhagen"));
		ZONE_MAP.put(CountryCode.RU, ZoneId.of("Europe/Moscow"));
		ZONE_MAP.put(CountryCode.MO, ZoneId.of("Asia/Macau"));
		ZONE_MAP.put(CountryCode.MY, ZoneId.of("Asia/Kuala_Lumpur"));
		ZONE_MAP.put(CountryCode.MX, ZoneId.of("America/Mexico_City"));
		ZONE_MAP.put(CountryCode.MN, ZoneId.of("Asia/Ulaanbaatar"));
		ZONE_MAP.put(CountryCode.US, ZoneId.of("America/New_York"));
		ZONE_MAP.put(CountryCode.BH, ZoneId.of("Asia/Bahrain"));
		ZONE_MAP.put(CountryCode.BD, ZoneId.of("Asia/Dhaka"));
		ZONE_MAP.put(CountryCode.VN, ZoneId.of("Asia/Ho_Chi_Minh"));
		ZONE_MAP.put(CountryCode.BR, ZoneId.of("America/Sao_Paulo"));
		ZONE_MAP.put(CountryCode.BN, ZoneId.of("Asia/Brunei"));
		ZONE_MAP.put(CountryCode.SA, ZoneId.of("Asia/Riyadh"));
		ZONE_MAP.put(CountryCode.SE, ZoneId.of("Europe/Stockholm"));
		ZONE_MAP.put(CountryCode.CH, ZoneId.of("Europe/Zurich"));
		ZONE_MAP.put(CountryCode.SG, ZoneId.of("Asia/Singapore"));
		ZONE_MAP.put(CountryCode.AE, ZoneId.of("Asia/Dubai"));
		ZONE_MAP.put(CountryCode.GB, ZoneId.of("Europe/London"));
		ZONE_MAP.put(CountryCode.OM, ZoneId.of("Asia/Muscat"));
		ZONE_MAP.put(CountryCode.JO, ZoneId.of("Asia/Amman"));
		ZONE_MAP.put(CountryCode.DE, ZoneId.of("Europe/Berlin"));
		ZONE_MAP.put(CountryCode.FR, ZoneId.of("Europe/Paris"));
		ZONE_MAP.put(CountryCode.IT, ZoneId.of("Europe/Rome"));
		ZONE_MAP.put(CountryCode.ES, ZoneId.of("Europe/Madrid"));
		ZONE_MAP.put(CountryCode.NL, ZoneId.of("Europe/Amsterdam"));
		ZONE_MAP.put(CountryCode.BE, ZoneId.of("Europe/Brussels"));
		ZONE_MAP.put(CountryCode.AT, ZoneId.of("Europe/Vienna"));
		ZONE_MAP.put(CountryCode.IE, ZoneId.of("Europe/Dublin"));
		ZONE_MAP.put(CountryCode.PT, ZoneId.of("Europe/Lisbon"));
		ZONE_MAP.put(CountryCode.FI, ZoneId.of("Europe/Helsinki"));
		ZONE_MAP.put(CountryCode.GR, ZoneId.of("Europe/Athens"));
		ZONE_MAP.put(CountryCode.LU, ZoneId.of("Europe/Luxembourg"));
		ZONE_MAP.put(CountryCode.IL, ZoneId.of("Asia/Jerusalem"));
		ZONE_MAP.put(CountryCode.EG, ZoneId.of("Africa/Cairo"));
		ZONE_MAP.put(CountryCode.IN, ZoneId.of("Asia/Kolkata"));
		ZONE_MAP.put(CountryCode.ID, ZoneId.of("Asia/Jakarta"));
		ZONE_MAP.put(CountryCode.JP, ZoneId.of("Asia/Tokyo"));
		ZONE_MAP.put(CountryCode.CN, ZoneId.of("Asia/Shanghai"));
		ZONE_MAP.put(CountryCode.CZ, ZoneId.of("Europe/Prague"));
		ZONE_MAP.put(CountryCode.CL, ZoneId.of("America/Santiago"));
		ZONE_MAP.put(CountryCode.KZ, ZoneId.of("Asia/Almaty"));
		ZONE_MAP.put(CountryCode.QA, ZoneId.of("Asia/Qatar"));
		ZONE_MAP.put(CountryCode.CA, ZoneId.of("America/Toronto"));
		ZONE_MAP.put(CountryCode.KW, ZoneId.of("Asia/Kuwait"));
		ZONE_MAP.put(CountryCode.TH, ZoneId.of("Asia/Bangkok"));
		ZONE_MAP.put(CountryCode.TR, ZoneId.of("Europe/Istanbul"));
		ZONE_MAP.put(CountryCode.PK, ZoneId.of("Asia/Karachi"));
		ZONE_MAP.put(CountryCode.PL, ZoneId.of("Europe/Warsaw"));
		ZONE_MAP.put(CountryCode.PH, ZoneId.of("Asia/Manila"));
		ZONE_MAP.put(CountryCode.HU, ZoneId.of("Europe/Budapest"));
		ZONE_MAP.put(CountryCode.AU, ZoneId.of("Australia/Sydney"));
		ZONE_MAP.put(CountryCode.HK, ZoneId.of("Asia/Hong_Kong"));
	}

	private CountryCodeTimezoneMapper() {}

	public static ZoneId getZoneId(CountryCode countryCode) {
		if (countryCode == null) {
			return ZoneId.of("UTC");
		}
		return ZONE_MAP.getOrDefault(countryCode, ZoneId.of("UTC"));
	}
}
