import { type CountryCode } from '@/data/countryCode';

export interface CreateAccountBookRequest {
  localCountryCode: CountryCode;
  startDate: string;
  endDate: string;
}

export interface AccountBookResponse {
  id: number;
  title: string;
  localCountryCode: CountryCode;
  baseCountryCode: CountryCode;
  budget: number | null;
  startDate: string;
  endDate: string;
}
