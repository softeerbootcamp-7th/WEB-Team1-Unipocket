import type { CountryCode } from '@/data/countryCode';
import countryData from '@/data/countryData.json';
import { COUNTRY_LOCALE_MAP } from '@/data/countryLocale';

export interface CountryInfo {
  code: CountryCode;
  imageUrl: string;
  countryName: string;
  currencySign: string;
  currencyName: string;
  currencyNameKor: string;
  currencyUnitKor: string;
}

export const getCountryInfo = (code: CountryCode): CountryInfo | null => {
  const data = countryData[code as keyof typeof countryData];
  if (!data) return null;

  return {
    code,
    ...data,
  };
};

/**
 * 국가 코드에 맞는 locale로 금액을 포맷팅합니다.
 * @param amount - 포맷팅할 금액
 * @param countryCode - 국가 코드
 * @param fractionDigits - 소수점 자릿수 (기본값: 2)
 * @returns 포맷팅된 문자열
 */
export const formatCurrencyAmount = (
  amount: number,
  countryCode: CountryCode,
  fractionDigits: number = 2,
): string => {
  const locale = COUNTRY_LOCALE_MAP[countryCode] || 'en-US';

  return amount.toLocaleString(locale, {
    minimumFractionDigits: fractionDigits,
    maximumFractionDigits: fractionDigits,
  });
};
