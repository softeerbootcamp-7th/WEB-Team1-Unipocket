import type {
  CreateAccountBookRequest,
  CreateAccountBookResponse,
  GetAccountBookDetailResponse,
  GetAccountBooksResponse,
  UpdateAccountBookBudgetResponse,
  UpdateAccountBookExchangeRateResponse,
  UpdateAccountBookRequest,
  UpdateAccountBookResponse,
} from '@/api/account-books/type';
import { customFetch } from '@/api/config/client';
import { ENDPOINTS } from '@/api/config/endpoint';

export const getAccountBooks = (): Promise<GetAccountBooksResponse> => {
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

export const getAccountBookDetail = (
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

const updateAccountBookExchangeRate = (
  accountBookId: number,
): Promise<UpdateAccountBookExchangeRateResponse> => {
  return customFetch({
    endpoint: `${ENDPOINTS.ACCOUNT_BOOKS.DETAIL(accountBookId)}/main`,
    options: {
      method: 'PATCH',
    },
  });
};

export {
  createAccountBook,
  deleteAccountBook,
  updateAccountBook,
  updateAccountBookBudget,
  updateAccountBookExchangeRate,
};
