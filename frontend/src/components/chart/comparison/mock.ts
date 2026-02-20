import { COUNTRY_CODE, type CountryCode } from '@/data/country/countryCode';

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
    countryCode: COUNTRY_CODE.KR,
    average: 2311465,
    me: 2002876,
  },
  local: {
    countryCode: COUNTRY_CODE.US,
    average: 2234,
    me: 2000,
  },
};
