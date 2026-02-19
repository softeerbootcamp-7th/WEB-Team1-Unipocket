import type {
  AccountBookDetail,
  AccountBookResponse,
  AccountBookSummary,
  CreateAccountBookRequest,
} from '@/api/account-books/type';
import { customFetch } from '@/api/config/client';
import { ENDPOINTS } from '@/api/config/endpoint';

const createAccountBook = (
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

const updateAccountBook = (
  accountBookId: number,
  data: Partial<CreateAccountBookRequest>,
): Promise<AccountBookResponse> => {
  return customFetch({
    endpoint: ENDPOINTS.ACCOUNT_BOOKS.DETAIL(accountBookId),
    options: {
      method: 'PATCH',
      body: JSON.stringify(data),
    },
  });
};

const updateAccountBookBudget = (accountBookId: number, budget: number) => {
  return customFetch({
    endpoint: ENDPOINTS.ACCOUNT_BOOKS.BUDGET(accountBookId),
    options: {
      method: 'PATCH',
      body: JSON.stringify({ budget }),
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

const setMainAccountBook = (accountBookId: number): Promise<void> => {
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
  setMainAccountBook,
  updateAccountBook,
  updateAccountBookBudget,
};
