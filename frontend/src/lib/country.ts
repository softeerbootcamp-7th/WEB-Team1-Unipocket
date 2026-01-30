import countryData from '@/datas/country_data.json';
import type { CountryCode } from '@/datas/countryCode';

export interface CountryInfo {
  code: CountryCode;
  imageUrl: string;
  countryName: string;
  currencySign: string;
  currencyName: string;
}

export const getCountryInfo = (code: CountryCode): CountryInfo | null => {
  const data = countryData[code as keyof typeof countryData];
  if (!data) return null;

  return {
    code,
    ...data,
  };
};
