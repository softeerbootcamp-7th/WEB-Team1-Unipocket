import type { CountryCode } from '@/data/country/countryCode';

export interface AccountBookMeta {
  id: number;
  title: string;
  localCountryCode?: CountryCode;
  baseCountryCode?: CountryCode;
  startDate?: string;
  endDate?: string;
}
