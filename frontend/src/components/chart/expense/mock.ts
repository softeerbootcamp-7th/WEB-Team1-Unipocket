import type { CountryCode } from '@/data/country/countryCode';
import { COUNTRY_CODE } from '@/data/country/countryCode';

export const mockDataForMethod: {
  label: string;
  percent: number;
}[] = [
  {
    label: 'Chase Debit Card',
    percent: 35,
  },
  {
    label: '트레블월렛',
    percent: 25,
  },
  {
    label: '하나 비바X',
    percent: 25,
  },
  {
    label: '현금',
    percent: 10,
  },
  {
    label: '기타',
    percent: 5,
  },
];

export const mockDataForCurrency: {
  countryCode: CountryCode;
  percent: number;
}[] = [
  {
    countryCode: COUNTRY_CODE.SA,
    percent: 35,
  },
  {
    countryCode: COUNTRY_CODE.TW,
    percent: 25,
  },
  {
    countryCode: COUNTRY_CODE.JP,
    percent: 25,
  },
  {
    countryCode: COUNTRY_CODE.US,
    percent: 15,
  },
];
