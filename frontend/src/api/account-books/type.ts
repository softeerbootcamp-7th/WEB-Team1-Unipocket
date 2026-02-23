import { type CategoryId } from '@/types/category';

import { type CountryCode } from '@/data/country/countryCode';

interface AccountBook {
  accountBookId: number;
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
  | 'accountBookId'
  | 'title'
  | 'localCountryCode'
  | 'baseCountryCode'
  | 'startDate'
  | 'endDate'
>;

type GetAccountBooksResponse = Pick<
  AccountBook,
  'accountBookId' | 'title' | 'isMain'
>[];

export interface AccountBookResponse {
  accountBookId: number;
  title: string;
  localCountryCode: CountryCode;
  baseCountryCode: CountryCode;
  budget: number | null;
  startDate: string;
  endDate: string;
  isMain?: boolean;
}

export interface AnalysisChartItem {
  date: string;
  cumulatedAmount: string;
}

export interface AnalysisCategoryItem {
  categoryIndex: CategoryId;
  mySpentAmount: string;
  averageSpentAmount: string;
}

export interface GetAnalysisResponse {
  countryCode: string;
  compareWithAverage: {
    month: number;
    mySpentAmount: string;
    averageSpentAmount: string;
    spentAmountDiff: string;
  };
  compareWithLastMonth: {
    diff: string;
    thisMonth: string;
    thisMonthCount: number;
    lastMonth: string;
    lastMonthCount: number;
    totalSpent: {
      thisMonthToDate: string;
      lastMonthTotal: string;
    };
    thisMonthSpent: string;
    thisMonthItem: AnalysisChartItem[];
    prevMonthItem: AnalysisChartItem[];
  };
  compareByCategory: {
    maxDiffCategoryIndex: CategoryId;
    isOverSpent: boolean;
    maxLabel: string;
    items: AnalysisCategoryItem[];
  };
}

type CreateAccountBookRequest = Required<
  Pick<AccountBook, 'localCountryCode' | 'startDate' | 'endDate'>
>;

type CreateAccountBookResponse = Required<AccountBookResponseBase>;
type GetAccountBookDetailResponse = Required<AccountBookResponseBase>;

type UpdateAccountBookRequest = {
  titlePresent?: boolean;
  localCountryCodePresent?: boolean;
  baseCountryCodePresent?: boolean;
  startDatePresent?: boolean;
  endDatePresent?: boolean;
} & Partial<
  Pick<
    AccountBook,
    | 'title'
    | 'localCountryCode'
    | 'baseCountryCode'
    | 'startDate'
    | 'endDate'
    | 'isMain'
  >
>;

type UpdateAccountBookResponse = Required<AccountBookResponseBase>;

type UpdateAccountBookBudgetResponse = Required<
  Pick<
    AccountBook,
    | 'accountBookId'
    | 'baseCountryCode'
    | 'localCountryCode'
    | 'budget'
    | 'budgetCreatedAt'
    | 'exchangeRate'
  >
>;

type GetAccountBookExchangeRateResponse = Required<
  Pick<
    AccountBook,
    'baseCountryCode' | 'localCountryCode' | 'exchangeRate' | 'budgetCreatedAt'
  >
>;

type GetExchangeRateResponse = Required<
  Pick<AccountBook, 'baseCountryCode' | 'localCountryCode' | 'exchangeRate'>
> & {
  occurredAt: string;
};

export type {
  AccountBook,
  CreateAccountBookRequest,
  CreateAccountBookResponse,
  GetAccountBookDetailResponse,
  GetAccountBookExchangeRateResponse,
  GetAccountBooksResponse,
  GetExchangeRateResponse,
  UpdateAccountBookBudgetResponse,
  UpdateAccountBookRequest,
  UpdateAccountBookResponse,
};
