import type { CurrencyType } from '@/types/currency';

import type {
  CreateAccountBookRequest,
  CreateAccountBookResponse,
  GetAccountBookDetailResponse,
  GetAccountBooksResponse,
  GetAnalysisResponse,
  UpdateAccountBookBudgetResponse,
  UpdateAccountBookExchangeRateResponse,
  UpdateAccountBookRequest,
  UpdateAccountBookResponse,
} from '@/api/account-books/type';
import { customFetch } from '@/api/config/client';
import { ENDPOINTS } from '@/api/config/endpoint';

const getAccountBooks = (): Promise<GetAccountBooksResponse> => {
  return customFetch({
    endpoint: ENDPOINTS.ACCOUNT_BOOKS.BASE,
    options: {
      method: 'GET',
    },
  });
};

const createAccountBook = (
  data: CreateAccountBookRequest,
): Promise<CreateAccountBookResponse> => {
  return customFetch({
    endpoint: ENDPOINTS.ACCOUNT_BOOKS.BASE,
    options: {
      method: 'POST',
      body: JSON.stringify(data),
    },
  });
};

const getAccountBookDetail = (
  accountBookId: number,
): Promise<GetAccountBookDetailResponse> => {
  return customFetch({
    endpoint: ENDPOINTS.ACCOUNT_BOOKS.DETAIL(accountBookId),
    options: {
      method: 'GET',
    },
  });
};

const deleteAccountBook = (accountBookId: number): Promise<void> => {
  return customFetch({
    endpoint: ENDPOINTS.ACCOUNT_BOOKS.DETAIL(accountBookId),
    options: {
      method: 'DELETE',
    },
  });
};

const updateAccountBook = (
  accountBookId: number,
  data: UpdateAccountBookRequest,
): Promise<UpdateAccountBookResponse> => {
  return customFetch({
    endpoint: ENDPOINTS.ACCOUNT_BOOKS.DETAIL(accountBookId),
    options: {
      method: 'PATCH',
      body: JSON.stringify(data),
    },
  });
};

const updateAccountBookBudget = (
  accountBookId: number,
  budget: number,
): Promise<UpdateAccountBookBudgetResponse> => {
  return customFetch({
    endpoint: ENDPOINTS.ACCOUNT_BOOKS.BUDGET(accountBookId),
    options: {
      method: 'PATCH',
      body: JSON.stringify({ budget }),
    },
  });
};

const getAccountBookExchangeRate = (
  accountBookId: number,
  occurredAt: string,
): Promise<UpdateAccountBookExchangeRateResponse> => {
  return customFetch({
    endpoint: `${ENDPOINTS.ACCOUNT_BOOKS.EXCHANGE_RATE(accountBookId)}`,
    params: occurredAt ? { occurredAt } : undefined,
    options: {
      method: 'GET',
    },
  });
};

const getAnalysis = (
  accountBookId: number,
  year: number,
  month: number,
  currencyType: CurrencyType,
): Promise<GetAnalysisResponse> => {
  return customFetch({
    endpoint: ENDPOINTS.ACCOUNT_BOOKS.ANALYSIS(accountBookId),
    params: {
      year: String(year),
      month: String(month),
      currencyType,
    },
    options: {
      method: 'GET',
    },
  });
};

export {
  createAccountBook,
  deleteAccountBook,
  getAccountBookDetail,
  getAccountBookExchangeRate,
  getAccountBooks,
  getAnalysis,
  updateAccountBook,
  updateAccountBookBudget,
};
