import { type CountryCode, countryCode } from '@/data/countryCode';

type ComparisonData = {
  countryCode: CountryCode;
  average: number;
  me: number;
};

export type ComparisonStatisticsResponse = {
  month: number;
  base: ComparisonData;
  local: ComparisonData;
};

export const mockData: ComparisonStatisticsResponse = {
  month: 12,
  base: {
    countryCode: countryCode[0] as CountryCode, // 'KR'
    average: 2311465,
    me: 2002876,
  },
  local: {
    countryCode: countryCode[12] as CountryCode, // 'US'
    average: 2234,
    me: 2000,
  },
};
