import { type CountryCode } from '@/data/countryCode';

export interface CreateAccountBookRequest {
  localCountryCode: CountryCode;
  startDate: string;
  endDate: string;
}

export interface AccountBookSummary {
  id: number;
  title: string;
  isMain: boolean;
}

export interface AccountBookDetail {
  id: number;
  title: string;
  localCountryCode: CountryCode;
  baseCountryCode: CountryCode;
  budget: number | null;
  startDate: string;
  endDate: string;
  isMain?: boolean;
}

export interface UpdateAccountBookRequest {
  title?: string;
  localCountryCode?: CountryCode;
  baseCountryCode?: CountryCode;
  budget?: number | null;
  startDate?: string;
  endDate?: string;
}

export interface AccountBookResponse {
  id: number;
  title: string;
  localCountryCode: CountryCode;
  baseCountryCode: CountryCode;
  budget: number | null;
  startDate: string;
  endDate: string;
  isMain?: boolean;
}
