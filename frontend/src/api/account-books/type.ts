import { type CountryCode } from '@/data/countryCode';

interface CreateAccountBookRequest {
  localCountryCode: CountryCode;
  startDate: string;
  endDate: string;
}

interface AccountBookResponse {
  id: number;
  title: string;
  localCountryCode: CountryCode;
  baseCountryCode: CountryCode;
  budget: number | null;
  startDate: string;
  endDate: string;
}

interface GetAccountBooksResponse {
  id: number;
  title: string;
  isMain: boolean;
}

export type {
  AccountBookResponse,
  CreateAccountBookRequest,
  GetAccountBooksResponse,
};
