import { type CountryCode } from '@/data/country/countryCode';

interface AccountBook {
  id: number;
  title: string;
  localCountryCode: CountryCode;
  baseCountryCode: CountryCode;
  startDate: string;
  endDate: string;
  budget: number | null;
  budgetCreatedAt: string;
  exchangeRate: number;
  tempExpenseBatchIds: string[];
  isMain: boolean;
}

type AccountBookResponseBase = Pick<
  AccountBook,
  | 'id'
  | 'title'
  | 'localCountryCode'
  | 'baseCountryCode'
  | 'startDate'
  | 'endDate'
>;

type GetAccountBooksResponse = Pick<AccountBook, 'id' | 'title' | 'isMain'>[];

type CreateAccountBookRequest = Required<
  Pick<AccountBook, 'localCountryCode' | 'startDate' | 'endDate'>
>;

type CreateAccountBookResponse = Required<AccountBookResponseBase>;
type GetAccountBookDetailResponse = Required<AccountBookResponseBase>;

type UpdateAccountBookRequest = Partial<
  Pick<
    AccountBook,
    | 'title'
    | 'localCountryCode'
    | 'baseCountryCode'
    | 'budget'
    | 'startDate'
    | 'endDate'
    | 'isMain'
  >
>;

type UpdateAccountBookResponse = Required<AccountBookResponseBase>;

type UpdateAccountBookBudgetResponse = Required<
  Pick<
    AccountBook,
    | 'baseCountryCode'
    | 'localCountryCode'
    | 'budget'
    | 'budgetCreatedAt'
    | 'exchangeRate'
  > & { accountBookId: number }
>;

type UpdateAccountBookExchangeRateResponse = Required<
  Pick<
    AccountBook,
    'baseCountryCode' | 'localCountryCode' | 'exchangeRate' | 'budgetCreatedAt'
  >
>;

export type {
  AccountBook,
  CreateAccountBookRequest,
  CreateAccountBookResponse,
  GetAccountBookDetailResponse,
  GetAccountBooksResponse,
  UpdateAccountBookBudgetResponse,
  UpdateAccountBookExchangeRateResponse,
  UpdateAccountBookRequest,
  UpdateAccountBookResponse,
};
