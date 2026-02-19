import type { CurrencyType } from '@/types/currency';

import type {
  AccountBookDetail,
  AccountBookResponse,
  AccountBookSummary,
  AnalysisResponse,
  CreateAccountBookRequest,
  UpdateAccountBookRequest,
} from '@/api/account-books/type';
import { customFetch } from '@/api/config/client';
import { ENDPOINTS } from '@/api/config/endpoint';

export const createAccountBook = (
  data: CreateAccountBookRequest,
): Promise<AccountBookResponse> => {
  return customFetch({
    endpoint: ENDPOINTS.ACCOUNT_BOOKS.BASE,
    options: {
      method: 'POST',
      body: JSON.stringify(data),
    },
  });
};

export const getAccountBooks = (): Promise<AccountBookSummary[]> => {
  return customFetch({
    endpoint: ENDPOINTS.ACCOUNT_BOOKS.BASE,
    options: {
      method: 'GET',
    },
  });
};

export const getAccountBookDetail = (
  accountBookId: number,
): Promise<AccountBookDetail> => {
  return customFetch({
    endpoint: ENDPOINTS.ACCOUNT_BOOKS.DETAIL(accountBookId),
    options: {
      method: 'GET',
    },
  });
};

export const updateAccountBook = (
  accountBookId: number,
  data: UpdateAccountBookRequest,
): Promise<AccountBookDetail> => {
  return customFetch({
    endpoint: ENDPOINTS.ACCOUNT_BOOKS.DETAIL(accountBookId),
    options: {
      method: 'PATCH',
      body: JSON.stringify(data),
    },
  });
};

export const deleteAccountBook = (accountBookId: number): Promise<void> => {
  return customFetch({
    endpoint: ENDPOINTS.ACCOUNT_BOOKS.DETAIL(accountBookId),
    options: {
      method: 'DELETE',
    },
  });
};

export const setMainAccountBook = (accountBookId: number): Promise<void> => {
  return customFetch({
    endpoint: `${ENDPOINTS.ACCOUNT_BOOKS.DETAIL(accountBookId)}/main`,
    options: {
      method: 'PATCH',
    },
  });
};

export const getAnalysis = (
  accountBookId: number,
  year: number,
  month: number,
  currencyType: CurrencyType,
): Promise<AnalysisResponse> => {
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
