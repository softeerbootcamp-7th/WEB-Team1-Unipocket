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
  budget?: number | null;
  budgetCreatedAt?: string;
  tempExpenseBatchIds?: string[];
  startDate: string;
  endDate: string;
}

interface AccountBook {
  id: number;
  title: string;
  isMain: boolean;
}

type GetAccountBooksResponse = AccountBook[];

export type {
  AccountBookResponse,
  CreateAccountBookRequest,
  GetAccountBooksResponse,
};
