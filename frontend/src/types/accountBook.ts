import type { CountryCode } from '@/data/countryCode';

export interface AccountBookMeta {
  id: number;
  title: string;
  localCountryCode: CountryCode;
  baseCountryCode: CountryCode;
  startDate: string;
  endDate: string;
}
