import type { CountryCode } from '@/data/country/countryCode';
import countryData from '@/data/country/countryData.json';
import { COUNTRY_LOCALE_MAP } from '@/data/country/countryLocale';
import type { CurrencyCode } from '@/data/country/currencyCode';

export interface CountryInfo {
  code: CountryCode;
  imageUrl: string;
  countryName: string;
  currencySign: string;
  currencyName: string;
  currencyNameKor: string;
  currencyUnitKor: string;
}

const CDN_URL = import.meta.env.VITE_CDN_URL;

export const getCountryInfo = (code: CountryCode): CountryInfo | null => {
  const data = countryData[code as keyof typeof countryData];
  if (!data) return null;

  return {
    code,
    ...data,
    imageUrl: `${CDN_URL}${data.imageUrl}`,
  };
};

/**
 * 국가 코드에 맞는 locale로 금액을 포맷팅합니다.
 * @param amount - 포맷팅할 금액
 * @param countryCode - 국가 코드
 * @param fractionDigits - 소수점 자릿수 (기본값: 2)
 * @returns 포맷팅된 문자열
 */
export const formatAmountByCountry = (
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

export const formatCurrency = (
  amount: number,
  currencyCode: CurrencyCode,
  fractionDigits: number = 2,
): string => {
  return new Intl.NumberFormat('ko-KR', {
    style: 'currency',
    currency: currencyCode,
    currencyDisplay: 'narrowSymbol',
    minimumFractionDigits: fractionDigits,
    maximumFractionDigits: fractionDigits,
  }).format(amount);
};
