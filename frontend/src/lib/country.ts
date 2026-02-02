import type { CountryCode } from '@/data/countryCode';
import countryData from '@/data/countryData.json';

export interface CountryInfo {
  code: CountryCode;
  imageUrl: string;
  countryName: string;
  currencySign: string;
  currencyName: string;
  currencyNameKor: string;
}

export const getCountryInfo = (code: CountryCode): CountryInfo | null => {
  const data = countryData[code as keyof typeof countryData];
  if (!data) return null;

  return {
    code,
    ...data,
  };
};
