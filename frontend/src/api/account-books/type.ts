import { type CountryCode } from '@/data/country/countryCode';

interface CreateAccountBookRequest {
  localCountryCode: CountryCode;
  startDate: string;
  endDate: string;
}

interface AccountBookSummary {
  id: number;
  title: string;
  isMain: boolean;
}

interface AccountBookDetail {
  id: number;
  title: string;
  localCountryCode: CountryCode;
  baseCountryCode: CountryCode;
  budget: number | null;
  startDate: string;
  endDate: string;
  isMain?: boolean;
}

interface UpdateAccountBookRequest {
  title?: string;
  localCountryCode?: CountryCode;
  baseCountryCode?: CountryCode;
  budget?: number | null;
  startDate?: string;
  endDate?: string;
}

interface AccountBookResponse {
  id: number;
  title: string;
  localCountryCode: CountryCode;
  baseCountryCode: CountryCode;
  budget?: number | null;
  budgetCreatedAt?: string;
  tempExpenseBatchIds?: string[];
  startDate: string;
  endDate: string;
  isMain?: boolean;
}

interface AccountBook {
  id: number;
  title: string;
  isMain: boolean;
}

type GetAccountBooksResponse = AccountBook[];

export type {
  AccountBookDetail,
  AccountBookResponse,
  AccountBookSummary,
  CreateAccountBookRequest,
  GetAccountBooksResponse,
  UpdateAccountBookRequest,
};
